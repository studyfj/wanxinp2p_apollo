package cn.itcast.wanxinp2p.repayment.mapper;

import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/19 14:28
 * @Description 致敬大师，致敬未来的自己
 */
public interface PlanMapper extends BaseMapper<RepaymentPlan> {

    // 此需求涉及日期的转换，自定义更灵活
    @Select("select * from repayment_plan where date_format(SHOULD_REPAYMENT_DATE, '%Y-%m-%d') = #{date} and REPAYMENT_STATUS = '0'")
    List<RepaymentPlan> selectDueRepayment(String date);

}
