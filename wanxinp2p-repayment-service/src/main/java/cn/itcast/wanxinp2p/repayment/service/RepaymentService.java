package cn.itcast.wanxinp2p.repayment.service;

import cn.itcast.wanxinp2p.api.depository.model.ProjectWithTendersDTO;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/19 14:33
 * @Description 致敬大师，致敬未来的自己
 */
public interface RepaymentService {

    /**
     * 启动还款
     * @param projectWithTendersDTO
     * @return
     */
    String startRepayment(ProjectWithTendersDTO projectWithTendersDTO);

}
