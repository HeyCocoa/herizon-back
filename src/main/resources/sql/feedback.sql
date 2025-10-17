create table feedback
(
    id          bigint auto_increment comment '反馈ID，主键自增'
        primary key,
    user_id     bigint                                null comment '提交用户ID，关联users表（可为空，支持匿名反馈）',
    type        varchar(20)                           not null comment '反馈类型: suggestion=建议, bug=问题, complaint=投诉',
    title       varchar(200)                          not null comment '反馈标题',
    content     text                                  not null comment '反馈详细内容',
    contact     varchar(100)                          null comment '联系方式（邮箱或手机号，可选）',
    user_agent  text                                  null comment '用户环境信息（浏览器、系统版本等）',
    status      varchar(20) default 'submitted'       not null comment '处理状态: submitted=已提交, processing=处理中, resolved=已解决, closed=已关闭',
    admin_reply text                                  null comment '管理员回复内容',
    admin_id    bigint                                null comment '处理管理员ID，关联users表',
    priority    tinyint     default 1                 not null comment '优先级: 1=低, 2=中, 3=高, 4=紧急',
    created_at  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint     default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint feedback_ibfk_1
        foreign key (user_id) references users (id)
            on delete set null,
    constraint feedback_ibfk_2
        foreign key (admin_id) references users (id)
            on delete set null
)
    comment '反馈表：用户意见反馈和问题报告管理';

create fulltext index ft_title_content
    on feedback (title, content);

create index idx_admin_id
    on feedback (admin_id);

create index idx_created_at
    on feedback (created_at);

create index idx_deleted
    on feedback (deleted);

create index idx_priority
    on feedback (priority);

create index idx_status
    on feedback (status);

create index idx_type
    on feedback (type);

create index idx_updated_at
    on feedback (updated_at);

create index idx_user_id
    on feedback (user_id);

INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (1, null, 'suggestion', '建议增加夜间模式', '希望应用能够增加夜间模式功能，减少眼部疲劳', 'user@example.com', null, 'submitted', null, null, 2, '2025-09-30 19:40:49', '2025-09-30 19:40:49', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (2, null, 'bug', '登录页面加载缓慢', '登录页面在网络较慢时加载时间过长，影响用户体验', null, null, 'processing', null, null, 3, '2025-09-30 19:40:49', '2025-09-30 19:40:49', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (3, null, 'complaint', '内容审核过于严格', '发布的正常内容被误判为违规，希望改进审核机制', 'feedback@test.com', null, 'submitted', null, null, 2, '2025-09-30 19:40:49', '2025-09-30 19:40:49', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (4, 1007, 'suggestion', '测试反馈', '这是自动化测试提交的反馈', 'test@example.com', null, 'submitted', null, null, 2, '2025-10-03 16:55:33', '2025-10-03 16:55:33', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (5, 1007, 'suggestion', '测试反馈', '这是自动化测试提交的反馈', 'test@example.com', null, 'submitted', null, null, 2, '2025-10-03 16:57:08', '2025-10-03 16:57:08', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (6, 1007, 'suggestion', '集成测试反馈', '这是一条通过集成测试提交的反馈_1759482690809', 'test@example.com', null, 'submitted', null, null, 2, '2025-10-03 17:11:31', '2025-10-03 17:11:31', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (7, 1007, 'suggestion', '测试反馈', '内容', 'test@test.com', null, 'submitted', null, null, 2, '2025-10-03 17:42:21', '2025-10-03 17:42:21', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (8, 1015, 'suggestion', '测试反馈', '这是一条测试反馈内容', 'test@example.com', null, 'submitted', null, null, 2, '2025-10-03 17:48:26', '2025-10-03 17:48:26', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (9, 1016, 'suggestion', '测试反馈', '这是一条测试反馈内容', 'test@example.com', null, 'submitted', null, null, 2, '2025-10-03 17:50:30', '2025-10-03 17:50:30', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (10, null, 'bug', 'Test Feedback', 'This is a test feedback to verify API integration', 'test@example.com', '{"platform":"web"}', 'submitted', null, null, 3, '2025-10-04 04:18:21', '2025-10-04 04:18:21', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (11, null, 'suggestion', 'Feature Request', 'Add dark mode feature', 'email: user@example.com', '{}', 'submitted', null, null, 2, '2025-10-04 04:18:48', '2025-10-04 04:18:48', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (12, null, 'complaint', 'Service Issue', 'Report inappropriate content', 'test@test.com', null, 'submitted', null, null, 3, '2025-10-04 04:19:04', '2025-10-04 04:19:04', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (13, null, 'suggestion', 'Final Test Feedback', 'Testing feedback API after cleanup', 'test@example.com', null, 'submitted', null, null, 2, '2025-10-05 00:53:53', '2025-10-05 00:53:53', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (14, 1023, 'suggestion', 'Test Feedback', 'Automated test feedback submission', null, null, 'submitted', null, null, 2, '2025-10-05 01:13:36', '2025-10-05 01:13:36', 0);
INSERT INTO herizon.feedback (id, user_id, type, title, content, contact, user_agent, status, admin_reply, admin_id, priority, created_at, updated_at, deleted) VALUES (15, 1024, 'suggestion', 'Test Feedback', 'Automated test feedback submission', null, null, 'resolved', '1111', 1039, 2, '2025-10-05 01:14:21', '2025-10-10 22:48:05', 0);
