package cn.itcast.wanxinp2p.repayment.message;

import cn.itcast.wanxinp2p.api.depository.model.RepaymentRequest;
import cn.itcast.wanxinp2p.repayment.entity.RepaymentPlan;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/21 12:25
 * @Description 致敬大师，致敬未来的自己
 */
@Component
public class RepaymentProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest) {
        // 构造消息
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("repaymentPlan", repaymentPlan);
        jsonObject.put("repaymentRequest", repaymentRequest);
        Message<String> build = MessageBuilder.withPayload(jsonObject.toJSONString()).build();
        // 发送消息
        rocketMQTemplate.sendMessageInTransaction("PID_CONFIRM_REPAYMENT",
                "TP_CONFIRM_REPAYMENT", build, null);
    }
}