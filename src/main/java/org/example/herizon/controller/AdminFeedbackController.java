package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herizon.common.Result;
import org.example.herizon.dto.FeedbackAdminViewDTO;
import org.example.herizon.dto.FeedbackReplyRequest;
import org.example.herizon.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端反馈处理接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/feedback")
@Tag(name = "反馈管理", description = "管理员处理用户反馈的接口")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 管理员查看反馈列表
     *
     * @param status  状态过滤
     * @param adminId 管理员ID
     * @return 反馈列表
     */
    @Operation(summary = "查询反馈列表", description = "管理员按状态查看所有反馈")
    @GetMapping
    public Result<List<FeedbackAdminViewDTO>> listFeedback(
            @Parameter(description = "反馈状态（pending/resolved）") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {
        try {
            List<FeedbackAdminViewDTO> data = feedbackService.listFeedbackForAdmin(adminId, status);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("管理员查询反馈失败", e);
            return Result.error("查询反馈失败，请稍后重试");
        }
    }

    /**
     * 管理员回复反馈
     *
     * @param feedbackId 反馈ID
     * @param request    回复内容
     * @param adminId    管理员ID
     * @return 操作结果
     */
    @Operation(summary = "回复反馈", description = "管理员回复反馈并标记为已解决")
    @PostMapping("/{feedbackId}/reply")
    public Result<Void> replyFeedback(
            @Parameter(description = "反馈ID") @PathVariable Long feedbackId,
            @Parameter(description = "回复请求") @RequestBody FeedbackReplyRequest request,
            @Parameter(description = "管理员ID") @RequestHeader("userId") Long adminId) {
        if (request == null || request.getReply() == null || request.getReply().trim().isEmpty()) {
            return Result.error(400, "回复内容不能为空");
        }

        try {
            feedbackService.replyFeedback(feedbackId, request.getReply(), adminId);
            Result<Void> result = Result.success();
            result.setMessage("回复成功，状态已更新为已解决");
            return result;
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("管理员回复反馈失败", e);
            return Result.error("回复失败，请稍后重试");
        }
    }
}
