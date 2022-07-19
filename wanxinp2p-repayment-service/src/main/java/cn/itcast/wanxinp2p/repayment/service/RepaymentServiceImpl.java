package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.depository.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.TenderDTO;
import cn.itcast.wanxinp2p.common.domain.RepaymentWayCode;
import cn.itcast.wanxinp2p.common.util.DateUtil;
import cn.itcast.wanxinp2p.repayment.entity.ReceivablePlan;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import cn.itcast.wanxinp2p.repayment.mapper.PlanMapper;
import cn.itcast.wanxinp2p.repayment.mapper.ReceivablePlanMapper;
import cn.itcast.wanxinp2p.repayment.model.EqualInterestRepayment;
import cn.itcast.wanxinp2p.repayment.util.RepaymentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        }else {
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
}