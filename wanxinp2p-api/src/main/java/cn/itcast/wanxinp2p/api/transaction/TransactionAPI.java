package cn.itcast.wanxinp2p.api.transaction;

import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/13 20:54
 * @Description 致敬大师，致敬未来的自己
 */
public interface TransactionAPI {

    /**
     * 借款人发标
     * @param projectDTO
     * @return
     */
    RestResponse<ProjectDTO> createProject(ProjectDTO projectDTO);

}
