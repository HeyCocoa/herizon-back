package org.example.herizon.service;

import org.example.herizon.dto.FeedbackRequest;
import org.example.herizon.entity.Feedback;
import org.example.herizon.entity.User;
import org.example.herizon.mapper.FeedbackMapper;
import org.example.herizon.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTests {

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void submitFeedback_requiresOfficialUser() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("bug");
        request.setContent("App 页面崩溃");

        User trialUser = new User();
        trialUser.setId(1L);
        trialUser.setRole(0);

        when(userMapper.selectOneById(1L)).thenReturn(trialUser);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> feedbackService.submitFeedback(request, 1L));

        assertTrue(exception.getMessage().contains("身份认证"));
        verify(feedbackMapper, never()).insert(any(Feedback.class));
    }

    @Test
    void submitFeedback_successSetsPendingStatus() {
        FeedbackRequest request = new FeedbackRequest();
        request.setType("bug");
        request.setContent("应用启动时闪退");
        request.setContact("user@example.com");

        User officialUser = new User();
        officialUser.setId(2L);
        officialUser.setRole(1);

        when(userMapper.selectOneById(2L)).thenReturn(officialUser);

        Feedback feedback = feedbackService.submitFeedback(request, 2L);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(captor.capture());

        Feedback saved = captor.getValue();
        assertEquals("pending", saved.getStatus());
        assertEquals("应用启动时闪退", saved.getContent());
        assertEquals(officialUser.getId(), saved.getUserId());
        assertEquals(saved, feedback);
    }

    @Test
    void replyFeedback_setsResolvedStatusAndReply() {
        User admin = new User();
        admin.setId(8L);
        admin.setRole(2);

        when(userMapper.selectOneById(8L)).thenReturn(admin);

        Feedback feedback = new Feedback();
        feedback.setId(99L);
        feedback.setUserId(5L);
        feedback.setStatus("pending");
        feedback.setDeleted(0);

        when(feedbackMapper.selectOneById(99L)).thenReturn(feedback);

        feedbackService.replyFeedback(99L, "问题已修复，请重试", 8L);

        assertEquals("resolved", feedback.getStatus());
        assertEquals("问题已修复，请重试", feedback.getAdminReply());
        assertEquals(admin.getId(), feedback.getAdminId());
        verify(feedbackMapper).update(feedback);
    }
}
