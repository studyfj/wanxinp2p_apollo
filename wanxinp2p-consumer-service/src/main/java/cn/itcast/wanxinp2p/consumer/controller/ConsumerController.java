package cn.itcast.wanxinp2p.consumer.controller;

import cn.itcast.wanxinp2p.api.consumer.ConsumerAPI;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author FengJun
 */
@RestController
@Api(value = "用户服务的Controller", tags = "Consumer")
public class ConsumerController implements ConsumerAPI {

    @Autowired
    private ConsumerService consumerService;

    @ApiOperation("用户注册")
    @ApiImplicitParam(name = "consumerRegisterDTO", value = "注册信息", required = true, dataType = "AccountRegisterDTO", paramType = "body")
    @PostMapping(value = "/consumers")
    @Override
    public RestResponse register(ConsumerRegisterDTO consumerRegisterDTO) {
        consumerService.register(consumerRegisterDTO);
        return RestResponse.success();
    }
}
