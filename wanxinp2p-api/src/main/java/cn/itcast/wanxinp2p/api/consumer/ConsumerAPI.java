package cn.itcast.wanxinp2p.api.consumer;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
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

    /**
     * 生成开户请求数据
     * @param consumerRequest 开户信息
     * @return
     */
    RestResponse<GatewayRequest> createConsumer(ConsumerRequest consumerRequest);

    /**
     * 获得当前登录用户
     * @return
     */
    RestResponse<ConsumerDTO> getCurrConsumer();



}
