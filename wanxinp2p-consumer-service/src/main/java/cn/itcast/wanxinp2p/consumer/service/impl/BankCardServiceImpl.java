package cn.itcast.wanxinp2p.consumer.service.impl;

import cn.itcast.wanxinp2p.api.depository.model.BankCardDTO;
import cn.itcast.wanxinp2p.consumer.entity.BankCard;
import cn.itcast.wanxinp2p.consumer.mapper.BankCardMapper;
import cn.itcast.wanxinp2p.consumer.service.BankCardService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/11 21:05
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class BankCardServiceImpl extends ServiceImpl<BankCardMapper, BankCard> implements BankCardService {

    @Override
    public BankCardDTO getByConsumerId(Long consumerId) {
        BankCard bankCard = this.baseMapper.selectOne(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getConsumerId, consumerId));
        return this.convertBankCardEntityToDTO(bankCard);
    }

    private BankCardDTO convertBankCardEntityToDTO(BankCard bankCard) {
        if (bankCard == null) {
            return null;
        }
        BankCardDTO bankCardDTO = new BankCardDTO();
        BeanUtils.copyProperties(bankCard, bankCardDTO);
        return bankCardDTO;
    }
    @Override
    public BankCardDTO getByCardNumber(String cardNumber) {
        BankCard bankCard = this.baseMapper.selectOne(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getCardNumber, cardNumber));
        return convertBankCardEntityToDTO(bankCard);
    }
}
