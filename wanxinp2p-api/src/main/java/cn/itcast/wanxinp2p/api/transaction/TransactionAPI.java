package cn.itcast.wanxinp2p.api.transaction;

import cn.itcast.wanxinp2p.api.transaction.model.*;
import cn.itcast.wanxinp2p.common.domain.RestResponse;

import java.util.List;

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

    /**
     * 标的信息快速检索
     * @param projectQueryDTO
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @param order
     * @return
     */
    RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO,
                                                   Integer pageNo, Integer pageSize, String sortBy,String order);

    /**
     * 通过ids获取多个标的
     * @param ids
     * @return
     */
    RestResponse<List<ProjectDTO>> queryProjectsIds(String ids);

    /**
     * 根据标的id查询投标记录
     * @param id
     * @return
     */
    RestResponse<List<TenderOverviewDTO>> queryTendersByProjectId(Long id);

    /**
     * 用户投标
     * @param projectInvestDTO
     * @return
     */
    RestResponse<TenderDTO> createTender(ProjectInvestDTO projectInvestDTO);


}
