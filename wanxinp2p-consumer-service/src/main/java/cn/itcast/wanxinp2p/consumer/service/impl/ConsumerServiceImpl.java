package cn.itcast.wanxinp2p.consumer.service.impl;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerDTO;
import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRegisterDTO;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.CodePrefixCode;
import cn.itcast.wanxinp2p.common.util.CodeNoUtil;
import cn.itcast.wanxinp2p.consumer.common.ConsumerErrorCode;
import cn.itcast.wanxinp2p.consumer.entity.Consumer;
import cn.itcast.wanxinp2p.consumer.mapper.ConsumerMapper;
import cn.itcast.wanxinp2p.consumer.service.ConsumerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 21:27
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class ConsumerServiceImpl extends ServiceImpl<ConsumerMapper, Consumer> implements ConsumerService {

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
    }
}
