package cn.itcast.wanxinp2p.consumer.service.impl;

import cn.itcast.wanxinp2p.api.account.model.AccountDTO;
import cn.itcast.wanxinp2p.api.account.model.AccountRegisterDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.CodePrefixCode;
import cn.itcast.wanxinp2p.common.domain.CommonErrorCode;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.consumer.agent.AccountApiAgent;
import cn.itcast.wanxinp2p.consumer.common.ConsumerErrorCode;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import cn.itcast.wanxinp2p.consumer.mapper.ConsumerMapper;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 21:27
 * @Description 致敬大师，致敬未来的自己
 */
@Service
@Slf4j
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements ConsumerService {

    /**
     * 远程调用类
     */
    @Autowired
    private AccountApiAgent accountApiAgent;

    @Override
    public Integer checkMobile(String mobile) {
        return getByMobile(mobile) != null ? 1 : 0;
    }

    private ConsumerDTO getByMobile(String mobile) {
        QueryWrapper<Consumer> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile", mobile);
        Consumer consumer = this.baseMapper.selectOne(wrapper);
        if (consumer == null) {
            return null;
        }
        return convertConsumerEntityToDTO(consumer);
    }

    /**
     * entity转为dto
     *
     * @param entity
     * @return
     */
    private ConsumerDTO convertConsumerEntityToDTO(Consumer entity) {
        ConsumerDTO dto = new ConsumerDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    @Hmily(confirmMethod = "confirmRegister", cancelMethod = "cancelRegister")
    public void register(ConsumerRegisterDTO consumerRegisterDTO) {
        // 先确定是否为新用户
        Integer flag = this.checkMobile(consumerRegisterDTO.getMobile());
        if (flag.intValue() == 1) {
            // 已注册过
            throw new BusinessException(ConsumerErrorCode.E_140107);
        }
        // 进行保存操作
        Consumer consumer = new Consumer();
        consumer.setUsername(consumerRegisterDTO.getUsername());
        BeanUtils.copyProperties(consumerRegisterDTO, consumer);
        consumer.setUserNo(CodeNoUtil.getNo(CodePrefixCode.CODE_REQUEST_PREFIX));
        // 注册没有用户名
        consumer.setUsername(CodeNoUtil.getNo(CodePrefixCode.CODE_NO_PREFIX));
        consumer.setIsBindCard(0);
        this.baseMapper.insert(consumer);

        // 进行远程调用统一账户中心
        AccountRegisterDTO accountRegisterDTO = new AccountRegisterDTO();
        BeanUtils.copyProperties(consumerRegisterDTO, accountRegisterDTO);
        accountRegisterDTO.setUsername(consumer.getUsername());
        RestResponse<AccountDTO> accountDTO = accountApiAgent.register(accountRegisterDTO);
        if (accountDTO.getCode() != CommonErrorCode.SUCCESS.getCode()) {
            throw new BusinessException(ConsumerErrorCode.E_140106);
        }
    }
    public void confirmRegister(ConsumerRegisterDTO consumerRegisterDTO) {
        log.info("execute confirmRegister");
    }
    public void cancelRegister(ConsumerRegisterDTO consumerRegisterDTO) {
        log.info("execute cancelRegister");
        remove(Wrappers.<Consumer>lambdaQuery().eq(Consumer::getMobile,
                consumerRegisterDTO.getMobile()));
    }

}
