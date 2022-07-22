package cn.itcast.wanxinp2p.repayment.sms;

import com.github.qcloudsms.SmsSingleSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/22 12:40
 * @Description 致敬大师，致敬未来的自己
 */
@Service
@Slf4j
public class QCloudSmsServiceImpl implements SmsService {

    @Value("${sms.qcloud.appId}")
    private int appId;
    @Value("${sms.qcloud.appKey}")
    private String appKey;
    @Value("${sms.qcloud.templateId}")
    private int templateId;
    @Value("${sms.qcloud.sign}")
    private String sign;


    @Override
    public void sendRepaymentNotify(String mobile, String date, BigDecimal amount) {
        log.info("给手机号{}，发送还款提醒：{},金额:{}",mobile,date,amount);
        SmsSingleSender ssender = new SmsSingleSender(appId, appKey);
        try {
            // 参数4： 短信模板中的占位符
            ssender.sendWithParam("86", mobile, templateId, new String[]{date, amount.toString()}, sign, "", "");
        }catch (Exception ex){
            log.error("发送失败：{}",ex .getMessage());
        }
    }
}
