package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 21:26
 * @Description 致敬大师，致敬未来的自己
 */
public interface ConsumerService extends IService<Consumer> {

    /**
     * 检测用户是否存在
     * @param mobile
     * @return
     */
    Integer checkMobile(String mobile);

    /**
     * 用户注册
     * @param consumerRegisterDTO
     * @return
     */
    void register(ConsumerRegisterDTO consumerRegisterDTO);

}
