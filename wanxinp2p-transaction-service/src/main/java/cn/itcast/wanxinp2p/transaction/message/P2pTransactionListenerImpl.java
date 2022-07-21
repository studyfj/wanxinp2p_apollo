package cn.itcast.wanxinp2p.transaction.message;

import cn.itcast.wanxinp2p.transaction.entity.Project;
import cn.itcast.wanxinp2p.transaction.mapper.ProjectMapper;
import cn.itcast.wanxinp2p.transaction.service.ProjectCode;
import cn.itcast.wanxinp2p.transaction.service.ProjectService;
import cn.itcast.wanxinp2p.transaction.service.ProjectServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/20 20:22
 * @Description 致敬大师，致敬未来的自己
 */
@Component
@RocketMQTransactionListener(txProducerGroup = "PID_START_REPAYMENT")
public class P2pTransactionListenerImpl implements RocketMQLocalTransactionListener {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectMapper projectMapper;

    /**
     * 执行本地事务
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        // 解析消息
        // 执行本地事务
        // 返回执行结果
        //1. 解析消息
        final JSONObject jsonObject = JSON.parseObject(new String((byte[]) message.getPayload()));
        Project project = JSONObject.parseObject(jsonObject.getString("project"), Project.class);
        //2. 执行本地事务 可以将这个方法定义接口形式 这里展示不定义了
        Boolean result = ((ProjectServiceImpl) projectService).updateProjectStatusAndStartRepayment(project);
        //3. 返回执行结果
        if (result) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 执行事务回查
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        System.out.println("事务回查");
        //1. 解析消息
        final JSONObject jsonObject = JSON.parseObject(new String((byte[])
                message.getPayload()));
        Project project =
                JSONObject.parseObject(jsonObject.getString("project"),
                        Project.class);
        //2. 查询标的状态
        Project pro = projectMapper.selectById(project.getId());
        //3. 返回结果
        if (pro.getProjectStatus().equals(ProjectCode.REPAYING.getCode())) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.ROLLBACK;
        }

    }
}
