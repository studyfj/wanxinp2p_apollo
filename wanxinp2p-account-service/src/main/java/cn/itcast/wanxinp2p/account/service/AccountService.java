package cn.itcast.wanxinp2p.account.service;

import cn.itcast.wanxinp2p.common.domain.RestResponse;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 9:26
 * @Description 致敬大师，致敬未来的自己
 */
public interface AccountService {

    /**
     * 获取手机短信验证码
     * @param mobile 手机号
     * @return RestResponse 响应对象
     */
    RestResponse getSMSCode(String mobile);
}
