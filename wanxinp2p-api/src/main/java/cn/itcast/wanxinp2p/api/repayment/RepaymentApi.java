package cn.itcast.wanxinp2p.api.repayment;

import cn.itcast.wanxinp2p.api.depository.model.ProjectWithTendersDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/18 20:32
 * @Description 致敬大师，致敬未来的自己
 */
public interface RepaymentApi {

    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */
    public RestResponse<String> startRepayment(ProjectWithTendersDTO projectWithTendersDTO);

}
