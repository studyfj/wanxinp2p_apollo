package cn.itcast.wanxinp2p.transaction.message;

import cn.itcast.wanxinp2p.api.depository.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.transaction.entity.Project;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/20 19:59
 * @Description 致敬大师，致敬未来的自己
 */
@Slf4j
@Component
public class P2pTransactionProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // 发送消息的代码
    public void updateProjectStatusAndStartRepayment(Project project, ProjectWithTendersDTO projectWithTendersDTO) {
        // 1.构造消息
        JSONObject object = new JSONObject();
        object.put("project", project);
        object.put("projectWithTendersDTO", projectWithTendersDTO);
        Message<String> msg = MessageBuilder.withPayload(object.toJSONString()).build();
        // 2.发送消息 组 目的地（和消费者一致）
        rocketMQTemplate.sendMessageInTransaction("PID_START_REPAYMENT", "TP_START_REPAYMENT", msg, null);
    }
}

