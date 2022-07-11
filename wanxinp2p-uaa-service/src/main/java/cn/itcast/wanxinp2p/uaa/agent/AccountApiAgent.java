package cn.itcast.wanxinp2p.uaa.agent;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/9 11:43
 * @Description 致敬大师，致敬未来的自己
 */
@FeignClient(value = "account-service")
public interface AccountApiAgent {


    @PostMapping(value = "/account/l/accounts/session")
    public RestResponse<AccountDTO> login(@RequestBody AccountLoginDTO accountLoginDTO);
}
