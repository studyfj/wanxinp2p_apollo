package cn.itcast.wanxinp2p.repayment.message;

import cn.itcast.wanxinp2p.api.depository.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.repayment.service.RepaymentService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/20 20:35
 * @Description 致敬大师，致敬未来的自己
 */
@Component
@RocketMQMessageListener(topic = "TP_START_REPAYMENT", consumerGroup = "CID_START_REPAYMENT")
public class StartRepaymentMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private RepaymentService repaymentService;

    // TODO 一定要给repayment_plan表添加唯一索引，利用唯一索引实现幂等性 防止添加唯一数据
    @Override
    public void onMessage(String projectStr) {
        System.out.println("消费消息：" + projectStr);
        //1.解析消息
        JSONObject jsonObject = JSON.parseObject(projectStr);
        ProjectWithTendersDTO projectWithTendersDTO = JSONObject
                .parseObject(jsonObject.getString("projectWithTendersDTO"),
                        ProjectWithTendersDTO.class);
        //2.调用业务层，执行本地事务
        repaymentService.startRepayment(projectWithTendersDTO);
    }
}