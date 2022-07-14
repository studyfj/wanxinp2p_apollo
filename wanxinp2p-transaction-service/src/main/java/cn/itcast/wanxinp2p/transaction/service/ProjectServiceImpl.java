package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.CodePrefixCode;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.transaction.agent.ConsumerApiAgent;
import cn.itcast.wanxinp2p.transaction.entity.Project;
import cn.itcast.wanxinp2p.transaction.mapper.ProjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

}
