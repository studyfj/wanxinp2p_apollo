package cn.itcast.wanxinp2p.consumer.service;

import cn.itcast.wanxinp2p.api.depository.model.BankCardDTO;
import cn.itcast.wanxinp2p.consumer.entity.BankCard;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/11 21:04
 * @Description 致敬大师，致敬未来的自己
 */
public interface BankCardService extends IService<BankCard> {

    /**
     * 获取银行卡信息
     * @param consumerId 用户id
     * @return
     */
    BankCardDTO getByConsumerId(Long consumerId);
    /**
     * 获取银行卡信息
     * @param cardNumber 卡号
     * @return
     */
    BankCardDTO getByCardNumber(String cardNumber);

}
