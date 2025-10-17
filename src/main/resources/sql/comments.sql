create table comments
(
    id         bigint auto_increment comment '评论ID，主键自增'
        primary key,
    post_id    bigint                             not null comment '所属帖子ID，关联posts表',
    user_id    bigint                             not null comment '评论者用户ID，关联users表',
    parent_id  bigint                             null comment '父评论ID，用于实现嵌套评论，顶级评论为null',
    content    text                               not null comment '评论内容',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    status     tinyint  default 0                 not null comment '状态: 0=正常, 1=删除',
    deleted    tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint comments_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint comments_ibfk_2
        foreign key (user_id) references users (id)
            on delete cascade,
    constraint comments_ibfk_3
        foreign key (parent_id) references comments (id)
            on delete cascade
)
    comment '评论表：支持嵌套回复的评论系统';

create fulltext index ft_content
    on comments (content);

create index idx_created_at
    on comments (created_at);

create index idx_deleted
    on comments (deleted);

create index idx_parent_id
    on comments (parent_id);

create index idx_post_id
    on comments (post_id);

create index idx_status
    on comments (status);

create index idx_user_id
    on comments (user_id);

INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (1, 6, 1008, null, 'This is a test comment!', '2025-10-03 00:54:51', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (2, 6, 1008, 1, 'This is a reply to the first comment', '2025-10-03 00:55:11', 0, 1);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (3, 7, 1007, null, '这是一条自动化测试评论', '2025-10-03 16:42:10', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (4, 8, 1007, null, '这是一条完整测试的评论', '2025-10-03 16:55:31', 0, 1);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (5, 11, 1007, null, '这是一条完整测试的评论', '2025-10-03 16:57:06', 0, 1);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (6, 14, 1007, null, '这是一条通过集成测试脚本发表的评论，用于验证前后端交互。', '2025-10-03 17:02:57', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (7, 15, 1007, null, '集成测试评论_1759482613191', '2025-10-03 17:10:13', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (8, 17, 1007, null, '集成测试评论_1759482683986', '2025-10-03 17:11:24', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (9, 1, 1007, null, '自愈测试评论_1759484187872', '2025-10-03 17:36:28', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (10, 1, 1007, null, '评论_1759484539624', '2025-10-03 17:42:20', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (11, 4, 1039, null, '1111', '2025-10-08 21:29:03', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (12, 4, 1039, 11, '222', '2025-10-08 21:29:08', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (13, 4, 1039, 11, '333', '2025-10-08 23:35:41', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (14, 2, 1039, null, '111', '2025-10-08 23:52:28', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (15, 2, 1039, 14, '222', '2025-10-09 05:54:13', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (16, 4, 1039, 11, '333', '2025-10-09 20:43:58', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (17, 2, 1039, 14, '123456', '2025-10-09 20:57:38', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (18, 11, 1039, null, '1', '2025-10-09 21:22:26', 0, 0);
INSERT INTO herizon.comments (id, post_id, user_id, parent_id, content, created_at, status, deleted) VALUES (19, 4, 1039, null, '2', '2025-10-11 18:25:32', 0, 0);
