package cn.itcast.wanxinp2p.account.controller;

import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.api.account.AccountAPI;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("校验手机号和验证码")
    @ApiImplicitParams({@ApiImplicitParam(name = "mobile", value = "手机号", required = true, dataType = "String"), @ApiImplicitParam(name = "key", value = "校验标识", required = true, dataType = "String"), @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String")})
    @GetMapping(value = "/mobiles/{mobile}/key/{key}/code/{code}")
    @Override
    public RestResponse<Integer> checkMobile(@PathVariable String mobile, @PathVariable String key, @PathVariable String code) {
        return RestResponse.success(accountService.checkMobile(mobile, key, code));
    }

    @ApiOperation("用户注册")
    @ApiImplicitParam(name = "accountRegisterDTO", value = "账户注册信息", required = true, dataType = "AccountRegisterDTO", paramType = "body")
    @PostMapping(value = "/l/accounts")
    @Override
    public RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO) {

        return RestResponse.success(accountService.register(accountRegisterDTO));
    }
}
