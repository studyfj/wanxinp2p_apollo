package cn.itcast.wanxinp2p.transaction.agent;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * C端服务代理
 */
@FeignClient(value = "consumer-service")
public interface ConsumerApiAgent {

    @GetMapping("/consumer/l/currConsumer")
    public RestResponse<ConsumerDTO> getCurrConsumer();
}