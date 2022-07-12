package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.GatewayRequest;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.EncryptUtil;
import cn.itcast.wanxinp2p.common.util.RSAUtil;
import cn.itcast.wanxinp2p.depository.common.constant.DepositoryRequestTypeCode;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import cn.itcast.wanxinp2p.depository.mapper.DepositoryRecordMapper;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/12 11:11
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class DepositoryRecordServiceImpl extends ServiceImpl<DepositoryRecordMapper, DepositoryRecord> implements DepositoryRecordService {

    @Autowired
    private ConfigService configService;

    @Override
    public GatewayRequest createConsumer(ConsumerRequest consumerRequest) {
        // 保存交易记录
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        this.baseMapper.insert(saveDepositoryRecord(consumerRequest));
        // 生成签名数据并返会给用户中心
        String requestStr = JSON.toJSONString(consumerRequest);
        //RSAUtil.sign(requestStr, RSAUtil.p2p_privateKey, "utf-8");
        String sign = RSAUtil.sign(requestStr, configService.getP2pPrivateKey(), "utf-8");
        GatewayRequest gatewayRequest = new GatewayRequest();
        // 交易名称
        gatewayRequest.setServiceName("PERSONAL_REGISTER");
        gatewayRequest.setPlatformNo(configService.getP2pCode());
        gatewayRequest.setReqData(EncryptUtil.encodeURL(EncryptUtil.encodeUTF8StringBase64(requestStr)));
        gatewayRequest.setSignature(EncryptUtil.encodeURL(sign));
        // 银行存管系统的地址
        gatewayRequest.setDepositoryUrl(configService.getDepositoryUrl() + "/gateway");
        return gatewayRequest;
    }

    private DepositoryRecord saveDepositoryRecord(ConsumerRequest consumerRequest) {
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        depositoryRecord.setRequestNo(consumerRequest.getRequestNo());
        // 交易类型 开户或者什么
        depositoryRecord.setRequestType(DepositoryRequestTypeCode.CONSUMER_CREATE.getCode());
        depositoryRecord.setObjectType("Consumer");
        depositoryRecord.setObjectId(consumerRequest.getId());
        depositoryRecord.setCreateDate(LocalDateTime.now());
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        return depositoryRecord;
    }
}
