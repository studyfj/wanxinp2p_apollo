package cn.itcast.wanxinp2p.account.service.impl;

import cn.itcast.wanxinp2p.account.common.AccountErrorCode;
import cn.itcast.wanxinp2p.account.entity.Account;
import cn.itcast.wanxinp2p.account.mapper.AccountMapper;
import cn.itcast.wanxinp2p.account.service.AccountService;
import cn.itcast.wanxinp2p.account.service.SmsService;
import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountLoginDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.PasswordUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
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
@Slf4j
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
    @Hmily(confirmMethod = "confirmRegister", cancelMethod = "cancelRegister")
    public RestResponse<AccountDTO> register(AccountRegisterDTO accountRegisterDTO) {
        Account account = new Account();
        account.setUsername(accountRegisterDTO.getUsername());
        account.setMobile(accountRegisterDTO.getMobile());
        account.setPassword(PasswordUtil.md5Hex(accountRegisterDTO.getPassword()));
        if (smsEnable) {
            account.setPassword(PasswordUtil.md5Hex(accountRegisterDTO.getMobile()));
        }
        account.setDomain("c");
        this.baseMapper.insert(account);
        return RestResponse.success(convertAccountEntityToDTO(account));
    }

    public void confirmRegister(AccountRegisterDTO registerDTO) {
        log.info("execute confirmRegister");
    }

    public void cancelRegister(AccountRegisterDTO registerDTO) {
        log.info("execute cancelRegister");
        //删除账号
        remove(Wrappers.<Account>lambdaQuery().eq(Account::getUsername, registerDTO.getUsername()));
    }

    /**
     * entity转为dto
     *
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

    @Override
    public AccountDTO login(AccountLoginDTO accountLoginDTO) {
        // 1.根据用户名和密码进行一次性查找
        // 2.根据用户名查询用户，判断密码是否正确(密码加密，采用这种方式)
        String domain = accountLoginDTO.getDomain();
        Account account = null;
        // b端用户通过username去登录，c端用户通过mobile去登录
        if ("b".equalsIgnoreCase(domain)) {
            account = getAccountByUserName(accountLoginDTO.getUsername());
        } else {
            account = getAccountByMobile(accountLoginDTO.getMobile());
        }

        if (account == null) {
            throw new BusinessException(AccountErrorCode.E_130104);
        }
        AccountDTO accountDTO = convertAccountEntityToDTO(account);
        // 如果为true采用短信验证码登录,无需进行密码验证
        if (smsEnable) {
            return accountDTO;
        }
        // 将密码进行编码
        String password = PasswordUtil.md5Hex(accountLoginDTO.getPassword());
        if (account.getPassword().equals(password)) {
            return accountDTO;
        }
        throw new BusinessException(AccountErrorCode.E_130105);
    }

    private Account getAccountByMobile(String mobile) {
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile", mobile);
        Account account = this.baseMapper.selectOne(wrapper);
        return account;
    }

    private Account getAccountByUserName(String userName) {
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("username", userName);
        Account account = this.baseMapper.selectOne(wrapper);
        return account;
    }
}
