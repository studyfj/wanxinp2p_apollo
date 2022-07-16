package cn.itcast.wanxinp2p.consumer.controller;

import cn.itcast.wanxinp2p.api.consumer.ConsumerAPI;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.EncryptUtil;
import cn.itcast.wanxinp2p.consumer.common.util.SecurityUtil;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import cn.itcast.wanxinp2p.consumer.service.impl.ConsumerServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public RestResponse register(@RequestBody ConsumerRegisterDTO consumerRegisterDTO) {
        consumerService.register(consumerRegisterDTO);
        return RestResponse.success();
    }

    @ApiOperation("过网关受保护资源，进行认证拦截测试")
    @ApiImplicitParam(name = "jsonToken", value = "访问令牌", required = true, dataType = "String")
    @GetMapping(value = "/m/consumers/test")
    public RestResponse<String> testResources(String jsonToken) {
        return RestResponse.success(EncryptUtil.decodeUTF8StringBase64(jsonToken));
    }

    @ApiOperation("生成开户请求数据")
    @ApiImplicitParam(name = "consumerRequest", value = "开户信息", required = true, dataType = "ConsumerRequest", paramType = "body")
    @PostMapping("/my/consumers")
    @Override
    public RestResponse<GatewayRequest> createConsumer(@RequestBody ConsumerRequest consumerRequest) {
        // 通过springSecurity获取
        String mobile = SecurityUtil.getUser().getMobile();
        consumerRequest.setMobile(mobile);
        return consumerService.createConsumer(consumerRequest);
    }

    @Override
    @ApiOperation("获取登录用户信息")
    @GetMapping("/l/currConsumer")
    public RestResponse<ConsumerDTO> getCurrConsumer() {
        // 这里取不到值，需要更改 day08-16显示
        ConsumerDTO consumerDTO = ((ConsumerServiceImpl)consumerService).getByMobile(SecurityUtil.getUser().getMobile());
        return RestResponse.success(consumerDTO);
    }

    @Override
    @ApiOperation("获取登录用户信息")
    @GetMapping("/my/consumers")
    public RestResponse<ConsumerDTO> getMyConsumer() {
        ConsumerDTO consumerDTO = ((ConsumerServiceImpl)consumerService).getByMobile(SecurityUtil.getUser().getMobile());
        return RestResponse.success(consumerDTO);
    }
}
