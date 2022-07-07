package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.account.common.AccountErrorCode;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.CommonErrorCode;
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

    /**
     * 发送并获取短信验证码
     *
     * @param mobile
     * @return
     */
    public RestResponse getSmsCode(String mobile) {
        // 如果开启发送短信
        if (smsEnable) {
            return OkHttpUtil.post(smsURL + "generate?effectiveTime=300&name=sms", "{\"mobile\":" + mobile + "}");
        }
        // 不开启直接返回
        return RestResponse.success();
    }

    /**
     * 校验验证码
     *
     * @param code 验证码
     * @param key  redis中的key标识
     */
    public void verifySmsCode(String key, String code) {
        // 开启短信验证码进行校验
        if (smsEnable) {
            StringBuilder sb = new StringBuilder("/verify?name=sms");
            sb.append("&verificationKey=").append(key);
            sb.append("&verificationCode=").append(code);
            RestResponse restResponse = OkHttpUtil.post(smsURL + sb, "");
            // 0 或者 false 是失败情况
            if (restResponse.getCode() != CommonErrorCode.SUCCESS.getCode() || restResponse.getResult().toString().equalsIgnoreCase("false")) {
                throw new BusinessException(AccountErrorCode.E_140152);
            }

        }
    }

}
