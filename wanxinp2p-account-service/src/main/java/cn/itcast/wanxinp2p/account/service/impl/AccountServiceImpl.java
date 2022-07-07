package cn.itcast.wanxinp2p.account.service.impl;

import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.account.mapper.AccountMapper;
import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.account.service.SmsService;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 9:28
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    private SmsService smsService;

    @Value("${sms.enable}")
    private Boolean smsEnable;

    @Override
    public RestResponse getSMSCode(String mobile) {
        return smsService.getSmsCode(mobile);
    }

    @Override
    public Integer checkMobile(String mobile, String key, String code) {
        // 校验验证码
        smsService.verifySmsCode(key, code);

        // 校验手机号
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile", mobile);
        Integer integer = this.baseMapper.selectCount(wrapper);
        return integer;
    }

    @Override
    public AccountDTO register(AccountRegisterDTO accountRegisterDTO) {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUsername(accountRegisterDTO.getUsername());
        accountDTO.setMobile(accountRegisterDTO.getMobile());
        Account account = new Account();
        account.setUsername(accountRegisterDTO.getUsername());
        account.setMobile(accountRegisterDTO.getMobile());
        account.setPassword(PasswordUtil.md5Hex(accountRegisterDTO.getPassword()));
        if (smsEnable) {
            account.setPassword(PasswordUtil.md5Hex(accountRegisterDTO.getMobile()));
        }
        account.setDomain("c");
        this.baseMapper.insert(account);
        return convertAccountEntityToDTO(account);
    }

    /**
     * entity转为dto
     * @param entity
     * @return
     */
    private AccountDTO convertAccountEntityToDTO(Account entity) {
        if (entity == null) {
            return null;
        }
        AccountDTO dto = new AccountDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

}
