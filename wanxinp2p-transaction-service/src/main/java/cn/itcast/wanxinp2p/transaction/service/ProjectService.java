package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.transaction.model.PageVO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/13 21:00
 * @Description 致敬大师，致敬未来的自己
 */
public interface ProjectService {

    /**
     * 创建标的
     *
     * @param project
     * @return ProjectDTO
     */
    ProjectDTO createProject(ProjectDTO project);

    /**
     * 根据分页条件检索标的信息
     *
     * @param projectQueryDTO
     * @param order
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return
     */
    PageVO<ProjectDTO> queryProjectsByQueryDTO(ProjectQueryDTO projectQueryDTO,
                                               String order, Integer pageNo, Integer pageSize, String
                                                       sortBy);

    /**
     * 管理员审核标的信息
     *
     * @param id
     * @param approveStatus
     * @return String
     */
    String projectsApprovalStatus(Long id, String approveStatus);

    PageVO<ProjectDTO> queryProjects(ProjectQueryDTO projectQueryDTO, String order,
                                     Integer pageNo, Integer pageSize, String sortBy);

}
