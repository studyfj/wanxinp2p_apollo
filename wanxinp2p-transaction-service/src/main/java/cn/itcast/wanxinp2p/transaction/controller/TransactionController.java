package cn.itcast.wanxinp2p.transaction.controller;

import cn.itcast.wanxinp2p.api.transaction.TransactionAPI;
import cn.itcast.wanxinp2p.api.transaction.model.PageVO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectQueryDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import cn.itcast.wanxinp2p.transaction.service.ProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/13 20:56
 * @Description 致敬大师，致敬未来的自己
 */
@Api(value = "交易中心服务", tags = "transaction")
@RestController
public class TransactionController implements TransactionAPI {

    @Autowired
    private ProjectService projectService;

    @Override
    @ApiOperation("借款人发标")
    @ApiImplicitParam(name = "project", value = "标的信息", required = true, dataType = "Project", paramType = "body")
    @PostMapping("/my/projects")
    public RestResponse<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        return RestResponse.success(projectService.createProject(projectDTO));
    }

    @Override
    @ApiOperation("检索标的信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectQueryDTO", value = "标的信息查询对象",
                    required = true, dataType = "ProjectQueryDTO", paramType = "body"),
            @ApiImplicitParam(name = "order", value = "顺序", required = false,
                    dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "pageNo", value = "页码", required = true,
                    dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", required = true,
                    dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "sortBy", value = "排序字段", required = true, dataType = "string", paramType = "query")})
    @PostMapping("/projects/q")
    public RestResponse<PageVO<ProjectDTO>> queryProjects(ProjectQueryDTO projectQueryDTO, String order, Integer pageNo, Integer pageSize, String sortBy) {
        return RestResponse.success(projectService.queryProjectsByQueryDTO(projectQueryDTO, order, pageNo, pageSize, sortBy));
    }

    @Override
    @ApiOperation("管理员审核标的信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "标的id", required = true, dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "approveStatus", value = "审批状态", required = true, dataType = "ref", paramType = "path")
    })
    @PutMapping("/m/projects/{id}/projectStatus/{approveStatus}")
    public RestResponse<String> projectsApprovalStatus(
            @PathVariable("id") Long id,
            @PathVariable("approveStatus") String approveStatus) {
        String result = projectService.projectsApprovalStatus(id, approveStatus);
        return RestResponse.success(result);


    }

}
