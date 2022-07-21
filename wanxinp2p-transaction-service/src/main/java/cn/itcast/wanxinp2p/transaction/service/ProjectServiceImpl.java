package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.consumer.model.BalanceDetailsDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.depository.model.*;
import cn.itcast.wanxinp2p.api.transaction.model.*;
import cn.itcast.wanxinp2p.common.domain.*;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.transaction.agent.ConsumerApiAgent;
import cn.itcast.wanxinp2p.transaction.agent.ContentSearchApiAgent;
import cn.itcast.wanxinp2p.transaction.agent.DepositoryAgentApiAgent;
import cn.itcast.wanxinp2p.transaction.common.constant.TradingCode;
import cn.itcast.wanxinp2p.transaction.common.constant.TransactionErrorCode;
import cn.itcast.wanxinp2p.transaction.common.utils.IncomeCalcUtil;
import cn.itcast.wanxinp2p.transaction.common.utils.SecurityUtil;
import cn.itcast.wanxinp2p.transaction.entity.Project;
import cn.itcast.wanxinp2p.transaction.entity.Tender;
import cn.itcast.wanxinp2p.transaction.mapper.ProjectMapper;
import cn.itcast.wanxinp2p.transaction.mapper.TenderMapper;
import cn.itcast.wanxinp2p.transaction.message.P2pTransactionProducer;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/13 21:00
 * @Description 致敬大师，致敬未来的自己
 */
@Service
@Slf4j
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Autowired
    private ConsumerApiAgent consumerApiAgent;

    @Autowired
    private ConfigService configService;

    @Autowired
    private DepositoryAgentApiAgent depositoryAgentApiAgent;

    @Autowired
    private P2pTransactionProducer p2pTransactionProducer;

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        RestResponse<ConsumerDTO> currConsumer = consumerApiAgent.getCurrConsumer();
        // 保存标的信息
        projectDTO.setConsumerId(currConsumer.getResult().getId());
        projectDTO.setUserNo(currConsumer.getResult().getUserNo());
        projectDTO.setProjectNo(CodeNoUtil.getNo(CodePrefixCode.CODE_PROJECT_PREFIX));
        // 标的状态修改
        projectDTO.setProjectStatus(ProjectCode.COLLECTING.getCode());
        // 标的可用状态修改, 未同步
        projectDTO.setStatus(StatusCode.STATUS_OUT.getCode());
        // 设置标的创建时间
        projectDTO.setCreateDate(LocalDateTime.now());
        // 设置还款方式
        projectDTO.setRepaymentWay(RepaymentWayCode.FIXED_REPAYMENT.getCode());
        // 设置标的类型
        projectDTO.setType("NEW");
        // 把dto转换为entity
        final Project project = convertProjectDTOToEntity(projectDTO);
        // 设置利率(需要在Apollo上进行配置)
        // 年化利率(借款人视图)
        project.setBorrowerAnnualRate(configService.getBorrowerAnnualRate());
        // 年化利率(投资人视图)
        project.setAnnualRate(configService.getAnnualRate());
        // 年化利率(平台佣金，利差)
        project.setCommissionAnnualRate(configService.getCommissionAnnualRate());
        // 债权转让
        project.setIsAssignment(0);
        // 设置标的名字, 姓名+性别+第N次借款
        // 判断男女
        String sex = Integer.parseInt(currConsumer.getResult().getIdNumber()
                .substring(16, 17)) % 2 == 0 ? "女士" : "先生";
        // 构造借款次数查询条件
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Project::getConsumerId, currConsumer.getResult().getId());
        project.setName(currConsumer.getResult().getFullname() + sex + "第" + (count(queryWrapper) + 1) + "次借款");
        // 保存到数据库
        save(project);
        // 设置主键
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        return projectDTO;

    }

    private Project convertProjectDTOToEntity(ProjectDTO projectDTO) {
        if (projectDTO == null) {
            return null;
        }
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        return project;
    }

    @Override
    public PageVO<ProjectDTO> queryProjectsByQueryDTO(ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy) {
        // 条件构造器
        QueryWrapper<Project> queryWrapper = new QueryWrapper();
        // 标的类型
        if (StringUtils.isNotBlank(projectQueryDTO.getType())) {
            queryWrapper.lambda().eq(Project::getType, projectQueryDTO.getType());
        }
        // 起止年化利率(投资人) -- 区间
        if (null != projectQueryDTO.getStartAnnualRate()) {
            queryWrapper.lambda().ge(Project::getAnnualRate,
                    projectQueryDTO.getStartAnnualRate());
        }
        if (null != projectQueryDTO.getEndAnnualRate()) {
            queryWrapper.lambda().le(Project::getAnnualRate,
                    projectQueryDTO.getStartAnnualRate());
        }
        // 借款期限 -- 区间
        if (null != projectQueryDTO.getStartPeriod()) {
            queryWrapper.lambda().ge(Project::getPeriod,
                    projectQueryDTO.getStartPeriod());
        }
        if (null != projectQueryDTO.getEndPeriod()) {
            queryWrapper.lambda().le(Project::getPeriod,
                    projectQueryDTO.getEndPeriod());
        }
        // 标的状态 募集中 满标 方标
        if (StringUtils.isNotBlank(projectQueryDTO.getProjectStatus())) {
            queryWrapper.lambda().eq(Project::getProjectStatus,
                    projectQueryDTO.getProjectStatus());
        }
        // 构造分页对象
        Page<Project> page = new Page<>(pageNo, pageSize);
        // 处理排序 order值: desc 或者 asc
        if (StringUtils.isNotBlank(order) && StringUtils.isNotBlank(sortBy)) {
            if (order.equalsIgnoreCase("asc")) {
                queryWrapper.orderByAsc(sortBy);
            } else if (order.equalsIgnoreCase("desc")) {
                queryWrapper.orderByDesc(sortBy);
            }
        } else {
            //默认按发标时间倒序排序
            queryWrapper.lambda().orderByDesc(Project::getCreateDate);
        }
        // 执行查询
        IPage<Project> projectIPage = page(page, queryWrapper);
        // ENTITY转换为DTO, 不向外部暴露ENTITY
        List<ProjectDTO> dtoList =
                convertProjectEntityListToDTOList(projectIPage.getRecords());
        // 封装结果集
        return new PageVO<>(dtoList, projectIPage.getTotal(), pageNo, pageSize);
    }

    private List<ProjectDTO> convertProjectEntityListToDTOList(List<Project> projectList) {
        if (projectList == null) {
            return null;
        }
        List<ProjectDTO> dtoList = new ArrayList<>();
        projectList.forEach(project -> {
            ProjectDTO projectDTO = new ProjectDTO();
            BeanUtils.copyProperties(project, projectDTO);
            dtoList.add(projectDTO);
        });
        return dtoList;
    }

    @Override
    public String projectsApprovalStatus(Long id, String approveStatus) {
        // 1.根据id查询出标的信息并转换为DTO对象
        final Project project = getById(id);
        final ProjectDTO projectDTO = convertProjectEntityToDTO(project);
        // 2.生成请求流水号
        //2.生成流水号(不存在才生成)
        if (StringUtils.isBlank(project.getRequestNo())) {
            projectDTO.setRequestNo(CodeNoUtil.getNo(CodePrefixCode
                    .CODE_REQUEST_PREFIX));
            update(Wrappers.<Project>lambdaUpdate().set(Project::getRequestNo,
                    projectDTO.getRequestNo()).eq(Project::getId, id));
        }

        //projectDTO.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        // 3.调用存管代理服务同步标的信息
        final RestResponse<String> restResponse = depositoryAgentApiAgent.createProject(projectDTO);
        if (DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(restResponse.getResult())) {
            // 4.修改状态为: 已发布
            update(Wrappers.<Project>lambdaUpdate().set(Project::getStatus, Integer.parseInt(approveStatus)).eq(Project::getId, id));
            return "success";
        }
        // 5.失败抛出一个业务异常
        throw new BusinessException(TransactionErrorCode.E_150113);
    }

    private ProjectDTO convertProjectEntityToDTO(Project project) {
        if (project == null) {
            return null;
        }
        ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);
        return projectDTO;
    }

    @Autowired
    private ContentSearchApiAgent contentSearchApiAgent;

    @Override
    public PageVO<ProjectDTO> queryProjects(ProjectQueryDTO projectQueryDTO,
                                            String order, Integer pageNo, Integer pageSize, String sortBy) {
        RestResponse<PageVO<ProjectDTO>> esResponse =
                contentSearchApiAgent.queryProjectIndex(projectQueryDTO, pageNo, pageSize, sortBy, order);
        if (!esResponse.isSuccessful()) {
            throw new BusinessException(CommonErrorCode.UNKOWN);
        }
        return esResponse.getResult();
    }


    @Autowired
    private TenderMapper tenderMapper;

    @Override
    public List<ProjectDTO> queryProjectsIds(String ids) {
        // 根据id获取标的信息
        List<Long> longs = Collections.emptyList();
        Arrays.asList(ids.split(",")).forEach(data -> {
            longs.add(Long.parseLong(data));

        });
        List<Project> projects = baseMapper.selectBatchIds(longs);
        List<ProjectDTO> collect = projects.stream().map(project -> {
            ProjectDTO projectDTO = new ProjectDTO();
            BeanUtils.copyProperties(project, projectDTO);
            // 查询出借人数,和剩余额度,页面需要
            projectDTO.setRemainingAmount(getProjectRemainingAmount(project));
            Integer count = tenderMapper.selectCount(Wrappers.<Tender>lambdaQuery().eq(Tender::getProjectId, project.getId()));
            projectDTO.setTenderCount(count);
            return projectDTO;
        }).collect(Collectors.toList());

        return null;
    }

    @Override
    public List<TenderOverviewDTO> queryTendersByProjectId(Long id) {
        // 通过标的id获取投资记录即可
        List<Tender> tenders = tenderMapper.selectList(Wrappers.<Tender>lambdaQuery().eq(Tender::getProjectId, id));
        List<TenderOverviewDTO> collect = tenders.stream().map(new Function<Tender, TenderOverviewDTO>() {
            @Override
            public TenderOverviewDTO apply(Tender tender) {
                TenderOverviewDTO tenderOverviewDTO = new TenderOverviewDTO();
                BeanUtils.copyProperties(tender, tenderOverviewDTO);
                return tenderOverviewDTO;
            }
        }).collect(Collectors.toList());
        return collect;
    }
    //
    //@Value("${min.amount}")
    //private double minAmount;
    @Override
    public TenderDTO createTender(ProjectInvestDTO projectInvestDTO) {
        // 前置条件判断准备工作(投标金额是否大于最小投标金额、账户余额是否足够)
        String amount = projectInvestDTO.getAmount();
        BigDecimal bigDecimal = new BigDecimal(amount);
        BigDecimal miniInvestmentAmount = configService.getMiniInvestmentAmount();
        if (bigDecimal.compareTo(miniInvestmentAmount) < 0) {
            throw new BusinessException(TransactionErrorCode.E_150109);
        }
        String mobile = SecurityUtil.getUser().getMobile();
        // 应该通过手机号 这里就不写了
        RestResponse<ConsumerDTO> currConsumer = consumerApiAgent.getCurrConsumer();
        String userNo = currConsumer.getResult().getUserNo();
        RestResponse<BalanceDetailsDTO> balanceDetails = consumerApiAgent.getBalance(userNo);
        BigDecimal myBalance = balanceDetails.getResult().getBalance();
        if (myBalance.compareTo(bigDecimal) < 0) {
            throw new BusinessException(TransactionErrorCode.E_150112);
        }
        // 1）接受用户填写的投标信息
        // 2）交易中心校验投资金额是否符合平台允许最小投资金额
        // 3）校验用户余额是否大于投资金额

        // 判断是否满标
        Long id = projectInvestDTO.getId();
        Project project = this.baseMapper.selectById(id);
        if ("FULLY".equalsIgnoreCase(project.getProjectStatus())) {
            throw new BusinessException(TransactionErrorCode.E_150114);
        }
        // 4）校验投资金额是否小于等于标的可投金额
        BigDecimal remainingAmount = getProjectRemainingAmount(project);
        if (bigDecimal.compareTo(remainingAmount) <= 0) {
            // 5）校验此次投标后的剩余金额是否满足最小投资金额
            // 公式:
            BigDecimal subtract = remainingAmount.subtract(bigDecimal);
            if (miniInvestmentAmount.compareTo(subtract) > 0) {
                if (subtract.compareTo(new BigDecimal(0.0)) != 0) {
                    throw new BusinessException(TransactionErrorCode.E_150111);
                }
            }
        }else {
            throw new BusinessException(TransactionErrorCode.E_150110);
        }

        // 6）以上条件都满足后，保存投标信息tender
        // 封装投标信息
        final Tender tender = new Tender();
        // 投资人投标金额( 投标冻结金额 )
        tender.setAmount(bigDecimal);
        // 投标人用户标识
        tender.setConsumerId(currConsumer.getResult().getId());
        tender.setConsumerUsername(currConsumer.getResult().getUsername());
        // 投标人用户编码
        tender.setUserNo(currConsumer.getResult().getUserNo());
        // 标的标识
        tender.setProjectId(projectInvestDTO.getId());
        // 标的编码
        tender.setProjectNo(project.getProjectNo());
        // 投标状态
        tender.setTenderStatus(TradingCode.FROZEN.getCode());
        // 创建时间
        tender.setCreateDate(LocalDateTime.now());
        // 请求流水号
        tender.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        // 可用状态
        tender.setStatus(0);
        tender.setProjectName(project.getName());
        // 标的期限(单位:天)
        tender.setProjectPeriod(project.getPeriod());
         // 年化利率(投资人视图)
        tender.setProjectAnnualRate(project.getAnnualRate());
        // 保存到数据库
        tenderMapper.insert(tender);

        // 7）请求存管代理服务进行投标预处理冻结
        // 构造请求数据
        UserAutoPreTransactionRequest userAutoPreTransactionRequest = new UserAutoPreTransactionRequest();
        // 冻结金额
        userAutoPreTransactionRequest.setAmount(bigDecimal);
        // 预处理业务类型
        userAutoPreTransactionRequest.setBizType(PreprocessBusinessTypeCode.TENDER.getCode());
        // 标的号
        userAutoPreTransactionRequest.setProjectNo(project.getProjectNo());
        // 请求流水号
        userAutoPreTransactionRequest.setRequestNo(tender.getRequestNo());
        // 投资人用户编码
        userAutoPreTransactionRequest.setUserNo(currConsumer.getResult().getUserNo());
        // 设置 关联业务实体标识
        userAutoPreTransactionRequest.setId(tender.getId());
        // 远程调用存管代理服务
        RestResponse<String> response = depositoryAgentApiAgent.userAutoPreTransaction(userAutoPreTransactionRequest);
        TenderDTO tenderDTO = null;
        // 8）存管代理服务返回处理结果给交易中心，交易中心计算此次投标预期收益
        // 判断结果 修改状态
        if (response.getResult().equalsIgnoreCase(DepositoryReturnCode.RETURN_CODE_00000.getCode())) {
            // 判断当前标的是否满标,更改状态
            tender.setStatus(1);// 1已同步
            // 投标
            tenderMapper.updateById(tender);
            BigDecimal projectRemainingAmount = this.getProjectRemainingAmount(project);
            if (projectRemainingAmount.compareTo(new BigDecimal(0)) == 0) {
                project.setProjectStatus(ProjectCode.FULLY.getCode());
            }
            // 根据标的期限计算还款月数预期
            final Double ceil = Math.ceil(project.getPeriod() / 30.0);
            Integer month = ceil.intValue();
            // 标的
            updateById(project);
            // 转换dto，封装数据
            tenderDTO = convertTenderEntityToDTO(tender);
            project.setRepaymentWay(RepaymentWayCode.FIXED_REPAYMENT.getDesc());
            tenderDTO.setProject(convertProjectEntityToDTO(project));
            // 9）返回预期收益给前端
            // 投多少钱，利息,
            BigDecimal incomeTotalInterest = IncomeCalcUtil.getIncomeTotalInterest(bigDecimal, configService.getAnnualRate(), month);
            tenderDTO.setExpectedIncome(incomeTotalInterest);
        }else {
            // 失败
            throw new BusinessException(TransactionErrorCode.E_150113);
        }

        return tenderDTO;
    }

    private TenderDTO convertTenderEntityToDTO(Tender tender) {
        if (tender == null) {
            return null;
        }
        TenderDTO tenderDTO = new TenderDTO();
        BeanUtils.copyProperties(tender, tenderDTO);
        return tenderDTO;
    }

    /**
     * 获取标的剩余可投额度
     *
     * @param project
     * @return
     */
    private BigDecimal getProjectRemainingAmount(Project project) {
        // 根据标的id在投标表查询已投金额
        List<BigDecimal> decimalList =
                tenderMapper.selectAmountInvestedByProjectId(project.getId());
        // 求和结果集
        BigDecimal amountInvested = new BigDecimal("0.0");
        for (BigDecimal d : decimalList) {
            amountInvested = amountInvested.add(d);
        }
        // 得到剩余额度
        return project.getAmount().subtract(amountInvested);
    }

    @Override
    public String loansApprovalStatus(Long id, String approveStatus, String commission) {
        // 生成放款明细
        // 标的信息，投标信息
        Project project = this.baseMapper.selectById(id); // 标的信息
        List<Tender> tenders = tenderMapper.selectList(Wrappers.<Tender>lambdaQuery().eq(Tender::getProjectId, id));// 投标人信息多个投标人
        LoanRequest loanRequest = generateLoanRequest(project, tenders, commission);

        // 放款,请求存管代理服务
        RestResponse<String> responseOne = depositoryAgentApiAgent.confirmLoan(loanRequest);
        if (responseOne.getResult().equalsIgnoreCase(DepositoryReturnCode.RETURN_CODE_00000.getCode())) {
            // 更新投标状态
            this.modifyStatus(tenders);
            // 再次请求存管代理，修改状态
            ModifyProjectStatusDTO modifyProjectStatusDTO = new ModifyProjectStatusDTO();
            modifyProjectStatusDTO.setId(project.getId());
            modifyProjectStatusDTO.setRequestNo(loanRequest.getRequestNo());
            modifyProjectStatusDTO.setProjectStatus(ProjectCode.REPAYING.getCode());
            modifyProjectStatusDTO.setProjectNo(project.getProjectNo());
            RestResponse<String> responseTwo = depositoryAgentApiAgent.modifyProjectStatus(modifyProjectStatusDTO);
            if (responseTwo.getResult().equalsIgnoreCase(DepositoryReturnCode.RETURN_CODE_00000.getCode())) {
                // 更改状态
                project.setProjectStatus(ProjectCode.REPAYING.getCode());
                updateById(project);
                // 启动还款
                // 准备数据 标的信息
                ProjectWithTendersDTO projectWithTendersDTO = new ProjectWithTendersDTO();
                ProjectDTO projectDTO = convertProjectEntityToDTO(project);
                projectWithTendersDTO.setProject(projectDTO);
                List<TenderDTO> tenderDTOS = convertTenderEntityListToDTOList(tenders);
                projectWithTendersDTO.setTenders(tenderDTOS);
                // 投资人让利和借款人让利
                BigDecimal commissionInvestorAnnualRate = configService.getCommissionInvestorAnnualRate();
                BigDecimal borrowerAnnualRate = configService.getBorrowerAnnualRate();
                projectWithTendersDTO.setCommissionBorrowerAnnualRate(borrowerAnnualRate);
                projectWithTendersDTO.setCommissionInvestorAnnualRate(commissionInvestorAnnualRate);
                // 向还款微服务进行发送请求,涉及到分布式事务问题,用rocketMq进行发送
                p2pTransactionProducer.updateProjectStatusAndStartRepayment(project, projectWithTendersDTO);
                return "审核成功";
            }else {
                throw new BusinessException(TransactionErrorCode.E_150113);
            }
        }else {
            throw new BusinessException(TransactionErrorCode.E_150113);
        }
    }

    @Transactional(rollbackFor = BusinessException.class)
    public Boolean updateProjectStatusAndStartRepayment(Project project) {
        project.setProjectStatus(ProjectCode.REPAYING.getCode());
        return updateById(project);
    }

    private List<TenderDTO> convertTenderEntityListToDTOList(List<Tender> records) {
        if (records == null) {
            return null;
        }
        List<TenderDTO> dtoList = new ArrayList<>();
        records.forEach(tender -> {
            TenderDTO tenderDTO = new TenderDTO();
            BeanUtils.copyProperties(tender, tenderDTO);
            dtoList.add(tenderDTO);
        });
        return dtoList;
    }

    // 更新投标状态
    public void modifyStatus(List<Tender> tenders) {
        tenders.forEach(item -> {
            item.setTenderStatus(TradingCode.LOAN.getCode());
            tenderMapper.updateById(item);
        });
    }

    // 根据标的和投标信息生成明细
    public LoanRequest generateLoanRequest(Project project, List<Tender> tenders, String commission) {
        LoanRequest loanRequest = new LoanRequest();
        // 考虑健壮性 应该判断一下
        loanRequest.setCommission(new BigDecimal(commission));
        loanRequest.setId(project.getId());
        loanRequest.setProjectNo(project.getProjectNo());
        // 生成请求流水号
        loanRequest.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        List<LoanDetailRequest> loanDetailRequests = new ArrayList<>();
        // 封装放款明细
        tenders.forEach(item -> {
            LoanDetailRequest loanDetailRequest = new LoanDetailRequest();
            loanDetailRequest.setPreRequestNo(item.getRequestNo());
            loanDetailRequest.setAmount(item.getAmount());
            loanDetailRequests.add(loanDetailRequest);
        });
        loanRequest.setDetails(loanDetailRequests);
        return loanRequest;
    }
}
