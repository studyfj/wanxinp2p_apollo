package cn.itcast.wanxinp2p.transaction.controller;

import cn.itcast.wanxinp2p.api.transaction.TransactionAPI;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.domain.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Override
    @ApiOperation("借款人发标")
    @ApiImplicitParam(name = "project", value = "标的信息", required = true, dataType = "Project", paramType = "body")
    @PostMapping("/my/projects")
    public RestResponse<ProjectDTO> createProject(ProjectDTO projectDTO) {
        return null;
    }
}
