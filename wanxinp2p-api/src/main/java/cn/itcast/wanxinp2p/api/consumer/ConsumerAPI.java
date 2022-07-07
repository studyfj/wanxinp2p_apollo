package cn.itcast.wanxinp2p.api.consumer;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 21:21
 * @Description 用户中心接口API
 */
public interface ConsumerAPI {

    /**
     * 保存用户信息
     * @param consumerRegisterDTO 接受的实体
     * @return
     */
    RestResponse register(ConsumerRegisterDTO consumerRegisterDTO);

}
