package cn.itcast.wanxinp2p.account.controller;

import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.api.account.AccountAPI;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 8:42
 * @Description 致敬大师，致敬未来的自己
 */
@Slf4j
@RestController
@Api(value = "统一账户服务", tags = "account")
public class AccountController implements AccountAPI {

    @Autowired
    private AccountService accountService;

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name = "mobile", value = "手机号", dataType = "string")
    @GetMapping("/sms/{mobile}")
    @Override
    public RestResponse getSMSCode(@PathVariable String mobile) {
        return accountService.getSMSCode(mobile);
    }
}
