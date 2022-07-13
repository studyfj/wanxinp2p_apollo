package cn.itcast.wanxinp2p.depository.message;

import cn.itcast.wanxinp2p.api.depository.model.DepositoryConsumerResponse;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/12 20:31
 * @Description 致敬大师，致敬未来的自己
 */
@Component
public class GatewayMessageProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void personRegister(DepositoryConsumerResponse response) {
        rocketMQTemplate.convertAndSend("TP_GATEWAY_NOTIFY_AGENT:PERSONAL_REGISTER", response);
    }
}


