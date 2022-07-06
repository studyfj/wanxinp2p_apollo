package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.common.util.OkHttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 9:29
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class SmsService {

    @Value("${sms.url}")
    private String smsURL;

    @Value("${sms.enable}")
    private Boolean smsEnable;

    public RestResponse getSmsCode(String mobile) {
        // 如果开启发送短信
        if (smsEnable) {
            return OkHttpUtil.post(smsURL + "generate?effectiveTime=300&name=sms", "{\"mobile\":" + mobile + "}");
        }
        // 不开启直接返回
        return RestResponse.success();
    }
}
