package cn.itcast.wanxinp2p.transaction.service;

import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;

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
}
