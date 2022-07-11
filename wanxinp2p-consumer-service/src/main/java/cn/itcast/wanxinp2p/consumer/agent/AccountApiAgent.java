package cn.itcast.wanxinp2p.consumer.agent;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/7 19:02
 * @Description 致敬大师，致敬未来的自己
 */
@FeignClient("account-service")
public interface AccountApiAgent {

    /**
     * 用户中心远程调用账户中心
     * @param accountRegisterDTO
     * @return
     */
    @PostMapping(value = "/account/l/accounts")
    @Hmily // 进行事务控制，事务发起方
    RestResponse<AccountDTO> register(@RequestBody AccountRegisterDTO accountRegisterDTO);
}
