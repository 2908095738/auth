package com.bbs.stream.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bbs.Result;
import com.bbs.stream.dto.StreamDto;
import com.bbs.stream.dto.param.CreateStreamParam;
import com.bbs.stream.entity.Stream;
import com.bbs.stream.service.StreamService;
import com.bbs.stream.util.ThreadLocalUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 审批控制器
 */
@Controller
//@Api(tags = "审批控制器")
@Tag(name = "StreamController", description = "审批控制器")
@RequestMapping("/stream")
public class StreamController {

//    @Resource
//    private StreamConverter streamConverter;

    @Autowired
    private StreamService streamService;

    /**
     * 创建审批
     *
     * @param param 创建审批请求参数
     * @return
     */
    @ResponseBody
    @ApiOperation(value = "创建审批", httpMethod = "PUT", consumes = "application/json", produces = "application/json")
    @PutMapping("/create")
    public Result create(@RequestBody CreateStreamParam param) {
        Long nowUid = ThreadLocalUtil.getCurrentUserId();

        Stream inDB = new Stream();
        inDB.setCreateUid(nowUid);
        inDB.setType(param.getType());
        inDB.setContent(param.getContent());

        return streamService.create(param.getCompanyId(), inDB);
    }

    /**
     * 获取审批列表
     *
     * @param current 页码
     * @param size    条数
     * @param status  审批状态：0.已审批;1.待审批;2.被驳回
     * @param type    审批类型: 1.请假;
     * @return
     */
    @ResponseBody
    @ApiOperation(value = "获取审批列表", httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "页码", defaultValue = "1", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "size", value = "条数", defaultValue = "10", allowableValues = "[1,infinity]", required = true, dataTypeClass = Integer.class, example = "10"),
            @ApiImplicitParam(name = "status", value = "审批状态：0.已审批;1.待审批;2.被驳回", allowableValues = "[0,2]", required = false, dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(name = "type", value = "审批类型: 1.请假;", allowableValues = "[1,1]", required = false, dataTypeClass = Integer.class, example = "1"),
    })
    @GetMapping("/page")
    public Result<Page<StreamDto>> page(Integer current, Integer size, Integer status, Integer type) {
        Long nowUid = ThreadLocalUtil.getCurrentUserId();
        Page<StreamDto> page = streamService.list(current, size, status, type, nowUid);
        return Result.success(page);
    }

    /**
     * 修改审批状态
     *
     * @param id     审批id
     * @param status 审批状态：0.已审批;1.待审批;2.被驳回
     * @return
     */
    @ResponseBody
    @ApiOperation(value = "修改审批状态", httpMethod = "GET", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "审批id", allowableValues = "[1,infinity]", required = true, dataTypeClass = Long.class, example = "1"),
            @ApiImplicitParam(name = "status", value = "审批状态：0.已审批;1.待审批;2.被驳回", allowableValues = "[0,2]", required = true, dataTypeClass = Integer.class, example = "0"),
    })
    @GetMapping("/update")
    public Result update(Long id, Integer status) {
        Stream entity = new Stream();
        entity.setId(id);
        entity.setStatus(status);

        boolean isDone = streamService.updateById(entity);
        if (isDone)
            return Result.success();
        else
            return Result.failed("update fail");
    }
}