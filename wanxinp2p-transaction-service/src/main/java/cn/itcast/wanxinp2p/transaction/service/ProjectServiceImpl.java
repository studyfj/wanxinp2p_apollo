package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.transaction.model.PageVO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;
import cn.itcast.wanxinp2p.common.domain.*;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.transaction.agent.ConsumerApiAgent;
import cn.itcast.wanxinp2p.transaction.agent.ContentSearchApiAgent;
import cn.itcast.wanxinp2p.transaction.agent.DepositoryAgentApiAgent;
import cn.itcast.wanxinp2p.transaction.common.constant.TransactionErrorCode;
import cn.itcast.wanxinp2p.transaction.entity.Project;
import cn.itcast.wanxinp2p.transaction.mapper.ProjectMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        if(StringUtils.isBlank(project.getRequestNo())){
            projectDTO.setRequestNo(CodeNoUtil.getNo(CodePrefixCode
                    .CODE_REQUEST_PREFIX));
            update(Wrappers.<Project>lambdaUpdate().set(Project::getRequestNo,
                    projectDTO.getRequestNo()).eq(Project::getId,id));
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
                contentSearchApiAgent.queryProjectIndex(projectQueryDTO,
                        pageNo, pageSize, sortBy,
                        order);
        if (!esResponse.isSuccessful()) {
            throw new BusinessException(CommonErrorCode.UNKOWN);
        }
        return esResponse.getResult();
    }
}
