package cn.itcast.wanxinp2p.api.consumer;

import cn.itcast.wanxinp2p.api.consumer.model.*;
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
     * 获得当前登录用户,这个是微服务使用的
     * @return
     */
    RestResponse<ConsumerDTO> getCurrConsumer();


    /**
     * 获取当前登录用户,给前端使用
     * @return
     */
    RestResponse<ConsumerDTO> getMyConsumer();

    /**
     * 获取借款人用户信息
     * @param id
     * @return
     */
    RestResponse<BorrowerDTO> getBorrower(Long id);

    /**
     获取当前登录用户余额信息
     @param userNo 用户编码
     @return
     */
    RestResponse<BalanceDetailsDTO> getBalance(String userNo);

    /**
     * 获取当前登录用户余额信息
     * @return
     */
    RestResponse<BalanceDetailsDTO> getMyBalance();

    /**
     * 获取借款人用户信息-供微服务访问
     * @param id 用户标识
     * @return
     */
    RestResponse<BorrowerDTO> getBorrowerMobile(Long id);

}
