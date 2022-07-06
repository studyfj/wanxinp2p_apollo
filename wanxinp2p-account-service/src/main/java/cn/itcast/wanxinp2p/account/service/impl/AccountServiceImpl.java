package cn.itcast.wanxinp2p.account.service.impl;

import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.account.service.SmsService;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 9:28
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private SmsService smsService;

    @Override
    public RestResponse getSMSCode(String mobile) {
        return smsService.getSmsCode(mobile);
    }
}
