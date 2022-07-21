package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.depository.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.api.depository.model.RepaymentDetailRequest;
import cn.itcast.wanxinp2p.api.depository.model.RepaymentRequest;
import cn.itcast.wanxinp2p.api.depository.model.UserAutoPreTransactionRequest;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.TenderDTO;
import cn.itcast.wanxinp2p.common.domain.*;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.common.util.DateUtil;
import cn.itcast.wanxinp2p.repayment.agent.DepositoryAgentApiAgent;
import cn.itcast.wanxinp2p.repayment.entity.ReceivableDetail;
import cn.itcast.wanxinp2p.repayment.entity.ReceivablePlan;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentDetail;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import cn.itcast.wanxinp2p.repayment.mapper.PlanMapper;
import cn.itcast.wanxinp2p.repayment.mapper.ReceivableDetailMapper;
import cn.itcast.wanxinp2p.repayment.mapper.ReceivablePlanMapper;
import cn.itcast.wanxinp2p.repayment.mapper.RepaymentDetailMapper;
import cn.itcast.wanxinp2p.repayment.message.RepaymentProducer;
import cn.itcast.wanxinp2p.repayment.model.EqualInterestRepayment;
import cn.itcast.wanxinp2p.repayment.util.RepaymentUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/19 14:34
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class RepaymentServiceImpl implements RepaymentService {

    @Autowired
    private PlanMapper planMapper;

    @Autowired
    private ReceivablePlanMapper receivablePlanMapper;

    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public String startRepayment(ProjectWithTendersDTO projectWithTendersDTO) {
        // 生成借款人还款计划
        // 获取标的信息 投标信息 计算还款月数 还款方式(只针对等额本利息)
        ProjectDTO project = projectWithTendersDTO.getProject();
        List<TenderDTO> tenders = projectWithTendersDTO.getTenders();
        Double ceil = Math.ceil(project.getPeriod() / 30.0);
        Integer month = ceil.intValue();
        String repaymentWay = project.getRepaymentWay();
        if (repaymentWay.equals(RepaymentWayCode.FIXED_REPAYMENT.getCode())) {
            // 生成还款计划
            EqualInterestRepayment equalInterestRepayment = RepaymentUtil.fixedRepayment(project.getAmount(), project.getBorrowerAnnualRate(), month, project.getCommissionAnnualRate());
            // 保存数据库 返回值用做投资人应收明细
            List<RepaymentPlan> repaymentPlans = saveRepaymentPlan(project, equalInterestRepayment);
            // 生成投资人应收明细
            // 根据投标信息生成应受明细
            tenders.forEach(tenderDTO -> {
                // 生成每个投资人的明细
                EqualInterestRepayment equalInterestRepayment1 = RepaymentUtil.fixedRepayment(tenderDTO.getAmount(), tenderDTO.getProjectAnnualRate(), month, projectWithTendersDTO.getCommissionInvestorAnnualRate());
                // 保存到数据库
                repaymentPlans.forEach(repaymentPlan -> {
                    saveRreceivablePlan(repaymentPlan, tenderDTO, equalInterestRepayment1);
                });
            });
        } else {
            return "-1";
        }
        return "00000";
    }

    //保存应收明细到数据库
    private void saveRreceivablePlan(RepaymentPlan repaymentPlan, TenderDTO tender, EqualInterestRepayment receipt) {
        // 应收本金
        final Map<Integer, BigDecimal> principalMap = receipt.getPrincipalMap();
        // 应收利息
        final Map<Integer, BigDecimal> interestMap = receipt.getInterestMap();
        // 平台收取利息
        final Map<Integer, BigDecimal> commissionMap = receipt.getCommissionMap();
        // 封装投资人应收明细
        ReceivablePlan receivablePlan = new ReceivablePlan();
        // 投标信息标识
        receivablePlan.setTenderId(tender.getId());
        // 设置期数
        receivablePlan.setNumberOfPeriods(repaymentPlan.getNumberOfPeriods());
        // 投标人用户标识
        receivablePlan.setConsumerId(tender.getConsumerId());
        // 投标人用户编码
        receivablePlan.setUserNo(tender.getUserNo());
        // 还款计划项标识
        receivablePlan.setRepaymentId(repaymentPlan.getId());
        // 应收利息
        receivablePlan.setInterest(interestMap.get(repaymentPlan.getNumberOfPeriods()));
        // 应收本金
        receivablePlan.setPrincipal(principalMap.get(repaymentPlan.getNumberOfPeriods()));
        // 应收本息 = 应收本金 + 应收利息
        receivablePlan.setAmount(receivablePlan.getInterest().add(receivablePlan.getPrincipal()));
        // 应收时间
        receivablePlan.setShouldReceivableDate(repaymentPlan.getShouldRepaymentDate());
        // 应收状态, 当前业务为未收
        receivablePlan.setReceivableStatus(0);
        // 创建时间
        receivablePlan.setCreateDate(DateUtil.now());
        // 设置投资人让利, 注意这个地方是具体: 佣金
        receivablePlan.setCommission(commissionMap.get(repaymentPlan.getNumberOfPeriods()));
        // 保存到数据库
        receivablePlanMapper.insert(receivablePlan);
    }


    //保存还款计划到数据库
    public List<RepaymentPlan> saveRepaymentPlan(ProjectDTO projectDTO, EqualInterestRepayment fixedRepayment) {
        List<RepaymentPlan> repaymentPlanList = new ArrayList<>();
        // 获取每期利息 第几期-利息数
        Map<Integer, BigDecimal> interestMap = fixedRepayment.getInterestMap();

        // 平台收取利息 按期收取
        Map<Integer, BigDecimal> commissionMap = fixedRepayment.getCommissionMap();

        // 收取每期本金
        fixedRepayment.getPrincipalMap().forEach((k, v) -> {
            // 还款计划封装数据
            final RepaymentPlan repaymentPlan = new RepaymentPlan();
            // 标的id
            repaymentPlan.setProjectId(projectDTO.getId());
            // 发标人用户标识
            repaymentPlan.setConsumerId(projectDTO.getConsumerId());
            // 发标人用户编码
            repaymentPlan.setUserNo(projectDTO.getUserNo());
            // 标的编码
            repaymentPlan.setProjectNo(projectDTO.getProjectNo());
            // 期数
            repaymentPlan.setNumberOfPeriods(k);
            // 当期还款利息
            repaymentPlan.setInterest(interestMap.get(k));
            // 还款本金
            repaymentPlan.setPrincipal(v);
            // 本息 = 本金 + 利息
            repaymentPlan.setAmount(repaymentPlan.getPrincipal()
                    .add(repaymentPlan.getInterest()));
            // 应还时间 = 当前时间 + 期数( 单位月 )
            repaymentPlan.setShouldRepaymentDate(DateUtil
                    .localDateTimeAddMonth(DateUtil.now(), k));
            // 应还状态, 当前业务为待还
            repaymentPlan.setRepaymentStatus("0");
            // 计划创建时间
            repaymentPlan.setCreateDate(DateUtil.now());
            // 设置平台佣金( 借款人让利 ) 注意这个地方是 具体佣金
            repaymentPlan.setCommission(commissionMap.get(k));
            // 保存到数据库
            planMapper.insert(repaymentPlan);
            repaymentPlanList.add(repaymentPlan);

        });

        return repaymentPlanList;
    }

    @Override
    public List<RepaymentPlan> selectDueRepayment(String date) {
        // 查询还款计划
        List<RepaymentPlan> repaymentPlans = planMapper.selectDueRepayment(date);

        return repaymentPlans;
    }

    @Resource
    private RepaymentDetailMapper repaymentDetailMapper;

    @Override
    public RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan) {
        // 进行查询
        RepaymentDetail repaymentDetail = repaymentDetailMapper.selectOne(Wrappers.<RepaymentDetail>lambdaQuery().eq(RepaymentDetail::getRepaymentPlanId, repaymentPlan.getId()));
        // 查不到数据进行保存
        if (repaymentDetail != null) {
            return repaymentDetail;
        }
        repaymentDetail = new RepaymentDetail();
        // 还款计划项标识
        repaymentDetail.setRepaymentPlanId(repaymentPlan.getId());
        // 实还本息
        repaymentDetail.setAmount(repaymentPlan.getAmount());
        // 实际还款时间
        repaymentDetail.setRepaymentDate(LocalDateTime.now());
        // 请求流水号
        repaymentDetail.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        // 未同步
        repaymentDetail.setStatus(StatusCode.STATUS_OUT.getCode());
        // 保存数据
        repaymentDetailMapper.insert(repaymentDetail);
        return repaymentDetail;
    }

    @Resource
    private DepositoryAgentApiAgent depositoryAgentApiAgent;

    @Override
    public Boolean preRepayment(RepaymentPlan repaymentPlan, String preRequestNo) {
        // 构造请求数据
        UserAutoPreTransactionRequest request = generateUserAutoPreTransactionRequest(repaymentPlan, preRequestNo);

        // feign发起请求
        RestResponse<String> stringRestResponse = depositoryAgentApiAgent.userAutoPreTransaction(request);
        // 返回结果
        return stringRestResponse.getResult().equals(DepositoryReturnCode.RETURN_CODE_00000.getCode());
    }

    /**
     * 构造存管代理服务预处理请求数据
     *
     * @param repaymentPlan
     * @param preRequestNo
     * @return
     */
    private UserAutoPreTransactionRequest generateUserAutoPreTransactionRequest(RepaymentPlan repaymentPlan, String preRequestNo) {
        // 构造请求数据
        UserAutoPreTransactionRequest userAutoPreTransactionRequest = new UserAutoPreTransactionRequest();
        // 冻结金额
        userAutoPreTransactionRequest.setAmount(repaymentPlan.getAmount());
        // 预处理业务类型
        userAutoPreTransactionRequest.setBizType(PreprocessBusinessTypeCode.REPAYMENT.getCode());
        // 标的号
        userAutoPreTransactionRequest.setProjectNo(repaymentPlan.getProjectNo());
        // 请求流水号
        userAutoPreTransactionRequest.setRequestNo(preRequestNo);
        // 标的用户编码
        userAutoPreTransactionRequest.setUserNo(repaymentPlan.getUserNo());
        // 关联业务实体标识
        userAutoPreTransactionRequest.setId(repaymentPlan.getId());
        // 返回结果
        return userAutoPreTransactionRequest;
    }

    @Resource
    private RepaymentProducer repaymentProducer;

    @Override
    public void executeRepayment(String date) {
        //查询所有到期的还款计划
        List<RepaymentPlan> repaymentPlanList = selectDueRepayment(date);
        repaymentPlanList.forEach(repaymentPlan -> {
            //生成还款明细（未同步）
            RepaymentDetail repaymentDetail = saveRepaymentDetail(repaymentPlan);
            //1.3 冻结预处理
            Boolean aBoolean = preRepayment(repaymentPlan, repaymentDetail.getRequestNo());
            System.out.println(aBoolean);
            if (aBoolean) {
                // 第三阶段 修改状态
                RepaymentRequest repaymentRequest = generateRepaymentRequest(repaymentPlan, repaymentDetail.getRequestNo());
                repaymentProducer.confirmRepayment(repaymentPlan, repaymentRequest);
            }

        });

    }

    // 封装请求数据

    /**
     * 构造还款信息请求数据
     */
    private RepaymentRequest generateRepaymentRequest(RepaymentPlan repaymentPlan, String preRequestNo) {
        //根据还款计划id, 获取应收计划
        final List<ReceivablePlan> receivablePlanList =
                receivablePlanMapper.selectList(Wrappers.<ReceivablePlan>lambdaQuery().eq(ReceivablePlan::getRepaymentId, repaymentPlan.getId()));
        //封装请求数据
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        // 还款总额
        repaymentRequest.setAmount(repaymentPlan.getAmount());
        // 业务实体id
        repaymentRequest.setId(repaymentPlan.getId());
        // 向借款人收取的佣金
        repaymentRequest.setCommission(repaymentPlan.getCommission());
        // 标的编码
        repaymentRequest.setProjectNo(repaymentPlan.getProjectNo());
        // 请求流水号
        repaymentRequest.setRequestNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        // 预处理业务流水号
        repaymentRequest.setPreRequestNo(preRequestNo);
        // 放款明细
        List<RepaymentDetailRequest> detailRequests = new ArrayList<>();
        receivablePlanList.forEach(receivablePlan -> {
            RepaymentDetailRequest detailRequest = new RepaymentDetailRequest();
            // 投资人用户编码
            detailRequest.setUserNo(receivablePlan.getUserNo());
            // 向投资人收取的佣金
            detailRequest.setCommission(receivablePlan.getCommission());
            // 派息 - 无
            // 投资人应得本金
            detailRequest.setAmount(receivablePlan.getPrincipal());
            // 投资人应得利息
            detailRequest.setInterest(receivablePlan.getInterest());
            // 添加到集合
            detailRequests.add(detailRequest);
        });
        // 还款明细请求信息
        repaymentRequest.setDetails(detailRequests);
        return repaymentRequest;
    }


    @Resource
    private ReceivableDetailMapper receivableDetailMapper;

    @Transactional
    @Override
    public Boolean confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        // 更新还款明细 已同步
        repaymentDetailMapper.update(null, Wrappers.<RepaymentDetail>lambdaUpdate().set(RepaymentDetail::getStatus, StatusCode.STATUS_IN.getCode()).eq(RepaymentDetail::getRequestNo, repaymentRequest.getPreRequestNo()));
        // 更新应收明细 已收
        // 根据还款计划id，查询应收计划
        List<ReceivablePlan> receivablePlans = receivablePlanMapper.selectList(Wrappers.<ReceivablePlan>lambdaQuery().eq(ReceivablePlan::getRepaymentId, repaymentPlan.getId()));
        receivablePlans.forEach(item -> {
            item.setReceivableStatus(1);
            receivablePlanMapper.updateById(item);
            // 保存数据到receive_detail
            //2.2 保存数据到receivable_detail
            // 构造应收明细
            ReceivableDetail receivableDetail = new ReceivableDetail();
            // 应收项标识
            receivableDetail.setReceivableId(item.getId());
            // 实收本息
            receivableDetail.setAmount(item.getAmount());
            // 实收时间
            receivableDetail.setReceivableDate(DateUtil.now());
            // 保存投资人应收明细
            receivableDetailMapper.insert(receivableDetail);

        });

        // 更新还款计划 已还款
        repaymentPlan.setRepaymentStatus("1");
        int i = planMapper.updateById(repaymentPlan);
        return i > 0;
    }

    @Override
    public void invokeConfirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        // feign发起远程请求
        RestResponse<String> repaymentResponse = depositoryAgentApiAgent.confirmRepayment(repaymentRequest);
        if (!DepositoryReturnCode.RETURN_CODE_00000.getCode().equals(repaymentResponse.getResult())) {
            throw new RuntimeException("还款失败");
        }

    }
}