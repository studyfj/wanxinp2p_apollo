package cn.itcast.wanxinp2p.transaction.agent;

import cn.itcast.wanxinp2p.api.consumer.model.BalanceDetailsDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * C端服务代理
 */
@FeignClient(value = "consumer-service")
public interface ConsumerApiAgent {

    @GetMapping("/consumer/l/currConsumer")
    public RestResponse<ConsumerDTO> getCurrConsumer();

    @GetMapping("/consumer/l/balances/{userNo}")
    public RestResponse<BalanceDetailsDTO> getBalance(@PathVariable("userNo")
                                                              String userNo);

}