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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 审批控制器
 */
@Controller
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
    @Operation(summary = "创建审批",
            parameters = {
                    @Parameter(description = "创建审批请求参数", required = true,
                            content = @Content(mediaType = "application/json"), schema = @Schema(implementation = CreateStreamParam.class))
            },
            responses = {
                    @ApiResponse(description = "审批结果", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Result.class))),
            })
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
    @Operation(summary = "获取审批列表",
            parameters = {
                    @Parameter(name = "companyId", description = "公司id", required = true, schema = @Schema(implementation = Long.class), example = "1"),
                    @Parameter(name = "current", description = "页码", required = true, schema = @Schema(implementation = Integer.class), example = "1"),
                    @Parameter(name = "size", description = "条数", required = true, schema = @Schema(implementation = Integer.class), example = "10"),
                    @Parameter(name = "status", description = "审批状态：0.已审批;1.待审批;2.被驳回", schema = @Schema(implementation = Integer.class), example = "1"),
                    @Parameter(name = "type", description = "审批类型: 1.请假;", required = false, allowEmptyValue = true, schema = @Schema(implementation = Integer.class), example = "1")
            },
            responses = {
                    @ApiResponse(description = "审批列表", content = @Content(mediaType = "application/json", schema = @Schema(allOf = {Page.class, StreamDto.class}))),
            })
    @GetMapping("/page")
    public Result<Page<StreamDto>> page(Long companyId, Integer current, Integer size, Integer status, Integer type) {
        Page<StreamDto> page = streamService.list(companyId, current, size, status, type);
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
        entity.setLeadr(ThreadLocalUtil.getCurrentUserId());

        boolean isDone = streamService.updateById(entity);
        if (isDone)
            return Result.success();
        else
            return Result.failed("update fail");
    }
}