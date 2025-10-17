create table post_tags
(
    id      bigint auto_increment comment '关联记录ID，主键自增'
        primary key,
    post_id bigint            not null comment '帖子ID，关联posts表',
    tag_id  bigint            not null comment '标签ID，关联tags表',
    deleted tinyint default 0 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint uk_post_tag
        unique (post_id, tag_id),
    constraint post_tags_ibfk_1
        foreign key (post_id) references posts (id)
            on delete cascade,
    constraint post_tags_ibfk_2
        foreign key (tag_id) references tags (id)
            on delete cascade
)
    comment '帖子标签关联表：实现帖子与标签的多对多关系';

create index idx_deleted
    on post_tags (deleted);

create index idx_post_id
    on post_tags (post_id);

create index idx_tag_id
    on post_tags (tag_id);

INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (1, 1, 9, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (2, 1, 10, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (3, 1, 11, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (4, 2, 14, 1);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (5, 2, 13, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (6, 2, 12, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (7, 3, 17, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (8, 3, 16, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (9, 3, 15, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (10, 4, 18, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (11, 4, 20, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (12, 4, 19, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (13, 5, 22, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (14, 5, 23, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (15, 5, 21, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (16, 6, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (17, 7, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (18, 8, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (19, 9, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (20, 10, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (21, 11, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (22, 12, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (23, 13, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (24, 14, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (25, 15, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (26, 15, 2, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (27, 16, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (28, 17, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (29, 17, 2, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (30, 18, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (31, 19, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (32, 20, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (33, 21, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (34, 22, 12, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (35, 23, 12, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (36, 24, 12, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (37, 25, 12, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (38, 26, 12, 1);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (39, 26, 23, 1);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (40, 29, 1, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (41, 29, 2, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (42, 31, 2, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (43, 32, 21, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (44, 32, 9, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (45, 33, 2, 0);
INSERT INTO herizon.post_tags (id, post_id, tag_id, deleted) VALUES (46, 33, 21, 0);
