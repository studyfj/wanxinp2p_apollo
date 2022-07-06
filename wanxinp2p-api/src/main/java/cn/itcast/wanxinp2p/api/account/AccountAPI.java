package cn.itcast.wanxinp2p.api.account;

import cn.itcast.wanxinp2p.common.domain.RestResponse;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/6 9:16
 * @Description 致敬大师，致敬未来的自己
 */
public interface AccountAPI {

    /**
     * 获取手机验证码
     * @param mobile 手机号
     * @return
     */
    RestResponse getSMSCode(String mobile);

}
