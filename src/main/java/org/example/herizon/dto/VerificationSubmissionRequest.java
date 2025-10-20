package org.example.herizon.dto;

import lombok.Data;

import java.util.List;

/**
 * 身份认证提交请求
 * <p>
 * 前端在完成问卷与个人信息收集后，将提交该结构给后端。
 */
@Data
public class VerificationSubmissionRequest {

    /**
     * 基础资料填写（真实姓名、城市等）
     */
    private VerificationFormData formData;

    /**
     * 问卷题目作答情况
     */
    private List<QuizAnswer> quizAnswers;

    /**
     * 个人身份信息
     */
    private PersonalInfo personalInfo;

    /**
     * 前端计算的总得分（后端会再次校验）
     */
    private Integer totalScore;

    /**
     * 是否自动判定通过
     */
    private Boolean autoApproved;

    @Data
    public static class VerificationFormData {
        private String realName;
        private String genderIdentity;
        private String ageRange;
        private String city;
        private String selfIntroduction;
        private String industry;
        private String positionLevel;
        private String workYears;
        private String companySize;
        private List<String> purposes;
        private List<String> interests;
        private Boolean agreePrivacy;
    }

    @Data
    public static class QuizAnswer {
        private String questionId;
        private String question;
        private String selectedOption;
        private String answerLabel;
        private Integer score;
    }

    @Data
    public static class PersonalInfo {
        private String identityType;
        private String identityLabel;
        private ProfessionalDetail professional;
        private StudentDetail student;
    }

    @Data
    public static class ProfessionalDetail {
        private String field;
        private String fieldLabel;
        private String positionLevel;
        private String positionLabel;
        private String experience;
        private String experienceLabel;
        private String otherField;
        private String otherPosition;
    }

    @Data
    public static class StudentDetail {
        private String stage;
        private String stageLabel;
        private String major;
        private String majorLabel;
        private String status;
        private String statusLabel;
        private String otherMajor;
    }
}
