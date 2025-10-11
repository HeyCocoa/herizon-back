package org.example.herizon.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.herizon.common.Result;
import org.example.herizon.dto.FeedbackRequest;
import org.example.herizon.dto.FeedbackSummaryDTO;
import org.example.herizon.entity.Feedback;
import org.example.herizon.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈控制器
 * <p>
 * 提供用户和管理员的反馈接口（用户端入口）
 */
@Slf4j
@Tag(name = "意见反馈", description = "用户意见反馈接口")
@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 用户提交反馈
     *
     * @param request 反馈请求体
     * @param userId  当前用户ID
     * @return 提交结果
     */
    @Operation(summary = "提交反馈", description = "仅正式用户可提交反馈，管理员会在后台处理")
    @PostMapping
    public Result<FeedbackSummaryDTO> submitFeedback(
            @Parameter(description = "反馈请求") @RequestBody FeedbackRequest request,
            @Parameter(description = "当前用户ID") @RequestHeader(value = "userId", required = false) Long userId) {

        if (request == null || !request.isValid()) {
            return Result.error(400, "请填写反馈类型和内容");
        }

        try {
            Feedback feedback = feedbackService.submitFeedback(request, userId);
            FeedbackSummaryDTO dto = feedbackService.toSummaryView(feedback);

            Result<FeedbackSummaryDTO> result = Result.success(dto);
            result.setMessage("反馈提交成功");
            return result;
        } catch (IllegalArgumentException e) {
            return Result.error(401, e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(403, e.getMessage());
        } catch (Exception e) {
            log.error("反馈提交失败", e);
            return Result.error("反馈提交失败，请稍后重试");
        }
    }

    /**
     * 查询当前用户的反馈记录
     *
     * @param userId 当前用户ID
     * @return 反馈列表
     */
    @Operation(summary = "查询我的反馈记录", description = "查看已提交的反馈状态和管理员回复")
    @GetMapping("/me")
    public Result<List<FeedbackSummaryDTO>> listMyFeedback(
            @Parameter(description = "当前用户ID") @RequestHeader(value = "userId", required = false) Long userId) {
        try {
            List<FeedbackSummaryDTO> data = feedbackService.listUserFeedback(userId);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(401, e.getMessage());
        } catch (Exception e) {
            log.error("查询反馈记录失败", e);
            return Result.error("查询反馈记录失败，请稍后重试");
        }
    }
}
