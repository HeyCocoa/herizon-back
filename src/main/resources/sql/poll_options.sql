create table poll_options
(
    id            bigint auto_increment
        primary key,
    post_id       bigint                             not null comment '帖子ID',
    option_text   varchar(200)                       not null comment '选项名称',
    display_order int                                not null comment '选项显示顺序',
    created_at    datetime default CURRENT_TIMESTAMP null comment '创建时间',
    deleted       tinyint  default 0                 null comment '逻辑删除，0=未删除，1=已删除'
)
    comment '投票选项表';

create index idx_deleted
    on poll_options (deleted);

create index idx_post_id
    on poll_options (post_id);

INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (1, 9, '选项A', 1, '2025-10-03 16:55:27', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (2, 9, '选项B', 2, '2025-10-03 16:55:28', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (3, 9, '选项C', 3, '2025-10-03 16:55:28', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (4, 10, '选项1', 1, '2025-10-03 16:55:28', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (5, 10, '选项2', 2, '2025-10-03 16:55:29', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (6, 12, '选项A', 1, '2025-10-03 16:57:01', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (7, 12, '选项B', 2, '2025-10-03 16:57:01', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (8, 12, '选项C', 3, '2025-10-03 16:57:01', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (9, 13, '选项1', 1, '2025-10-03 16:57:02', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (10, 13, '选项2', 2, '2025-10-03 16:57:02', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (11, 16, '选项A', 1, '2025-10-03 17:10:11', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (12, 16, '选项B', 2, '2025-10-03 17:10:11', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (13, 16, '选项C', 3, '2025-10-03 17:10:12', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (14, 18, '选项A', 1, '2025-10-03 17:11:20', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (15, 18, '选项B', 2, '2025-10-03 17:11:20', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (16, 18, '选项C', 3, '2025-10-03 17:11:20', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (17, 27, 'A', 1, '2025-10-06 01:59:25', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (18, 27, 'B', 2, '2025-10-06 01:59:25', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (19, 28, 'A', 1, '2025-10-06 02:02:17', 0);
INSERT INTO herizon.poll_options (id, post_id, option_text, display_order, created_at, deleted) VALUES (20, 28, 'B', 2, '2025-10-06 02:02:17', 0);
