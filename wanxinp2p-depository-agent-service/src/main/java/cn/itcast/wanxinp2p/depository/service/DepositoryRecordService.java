package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/12 11:11
 * @Description 致敬大师，致敬未来的自己
 */
public interface DepositoryRecordService extends IService<DepositoryRecord> {

    /**
     * 开通存管账户
     * @param consumerRequest 开户信息
     * @return
     */
    GatewayRequest createConsumer(ConsumerRequest consumerRequest);
}