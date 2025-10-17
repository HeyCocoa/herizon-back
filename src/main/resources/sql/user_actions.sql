create table user_actions
(
    id          bigint auto_increment comment '行为记录ID，主键自增'
        primary key,
    user_id     bigint                             not null comment '操作用户ID，关联users表',
    target_id   bigint                             not null comment '目标对象ID（帖子、评论等）',
    target_type varchar(20)                        not null comment '目标对象类型: post=帖子, comment=评论',
    action_type tinyint                            not null comment '行为类型: 0=点赞, 1=收藏, 2=分享, 3=举报',
    extra_data  json                               null comment '额外数据(JSON格式，如举报原因)',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    deleted     tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint uk_user_target_action
        unique (user_id, target_id, target_type, action_type, deleted),
    constraint user_actions_ibfk_1
        foreign key (user_id) references users (id)
            on delete cascade
)
    comment '用户行为表：统一记录点赞、收藏、分享、举报等行为';

create index idx_action_type
    on user_actions (action_type);

create index idx_created_at
    on user_actions (created_at);

create index idx_deleted
    on user_actions (deleted);

create index idx_target
    on user_actions (target_id, target_type);

create index idx_user_id
    on user_actions (user_id);

INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (1, 1007, 7, 'post', 0, null, '2025-10-03 16:42:09', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (2, 1007, 8, 'post', 0, null, '2025-10-03 16:55:29', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (3, 1007, 8, 'post', 1, null, '2025-10-03 16:55:30', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (4, 1007, 8, 'post', 2, null, '2025-10-03 16:55:31', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (5, 1007, 8, 'post', 3, '{"reason": "测试举报"}', '2025-10-03 16:55:31', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (6, 1007, 11, 'post', 0, null, '2025-10-03 16:57:04', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (7, 1007, 11, 'post', 1, null, '2025-10-03 16:57:05', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (8, 1007, 11, 'post', 2, null, '2025-10-03 16:57:06', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (9, 1007, 11, 'post', 3, '{"reason": "测试举报"}', '2025-10-03 16:57:06', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (10, 1007, 14, '0', 0, null, '2025-10-03 17:02:57', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (11, 1007, 15, '0', 0, null, '2025-10-03 17:10:13', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (12, 1007, 15, '0', 1, null, '2025-10-03 17:10:13', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (13, 1007, 15, '0', 2, null, '2025-10-03 17:10:13', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (14, 1007, 15, '0', 3, '{"reason": "测试举报：违规内容测试"}', '2025-10-03 17:10:14', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (15, 1007, 17, '0', 0, null, '2025-10-03 17:11:23', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (16, 1007, 17, '0', 1, null, '2025-10-03 17:11:24', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (17, 1007, 17, '0', 2, null, '2025-10-03 17:11:25', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (18, 1007, 17, '0', 3, '{"reason": "测试举报：违规内容测试"}', '2025-10-03 17:11:25', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (19, 1007, 1, '0', 0, null, '2025-10-03 17:36:27', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (20, 1007, 1, '0', 1, null, '2025-10-03 17:42:19', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (21, 1017, 1018, 'user', 4, null, '2025-10-04 03:20:35', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (22, 1017, 1019, 'user', 4, null, '2025-10-04 03:20:36', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (23, 1018, 1017, 'user', 4, null, '2025-10-04 03:20:38', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (24, 1020, 4, 'post', 0, null, '2025-10-05 00:10:33', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (25, 1023, 4, 'post', 0, null, '2025-10-05 01:13:35', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (26, 1024, 4, 'post', 0, null, '2025-10-05 01:14:18', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (27, 1024, 1004, 'user', 4, null, '2025-10-05 01:14:22', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (28, 1039, 27, 'post', 0, null, '2025-10-06 02:01:46', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (29, 1039, 4, 'post', 0, null, '2025-10-08 23:51:57', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (30, 1039, 4, 'post', 1, null, '2025-10-08 23:52:15', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (31, 1039, 1, 'post', 1, null, '2025-10-09 01:02:31', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (32, 1039, 2, 'post', 1, null, '2025-10-09 05:54:16', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (33, 1039, 5, 'post', 1, null, '2025-10-09 21:53:05', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (34, 1039, 5, 'post', 0, null, '2025-10-09 21:53:07', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (35, 1039, 2, 'post', 0, null, '2025-10-09 22:49:20', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (36, 1039, 31, 'post', 0, null, '2025-10-10 21:41:53', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (37, 1039, 11, 'comment', 0, null, '2025-10-11 02:51:00', 1);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (38, 1039, 33, 'post', 0, null, '2025-10-11 14:16:28', 0);
INSERT INTO herizon.user_actions (id, user_id, target_id, target_type, action_type, extra_data, created_at, deleted) VALUES (39, 1039, 14, 'comment', 0, null, '2025-10-11 15:06:23', 0);
