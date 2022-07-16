package cn.itcast.wanxinp2p.api.transaction;

import cn.itcast.wanxinp2p.api.transaction.model.PageVO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;
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
     *
     * @param projectDTO
     * @return
     */
    RestResponse<ProjectDTO> createProject(ProjectDTO projectDTO);

    /**
     * 检索标的信息
     *
     * @param projectQueryDTO 封装查询条件
     * @param order
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO,
                                                   String order, Integer pageNo,
                                                   Integer pageSize, String sortBy);

    /**
     * 管理员审核标的信息
     *
     * @param id
     * @param approveStatus
     * @return
     */
    RestResponse<String> projectsApprovalStatus(Long id, String approveStatus);

}
