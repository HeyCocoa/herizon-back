create table tags
(
    id          bigint auto_increment comment '标签ID，主键自增'
        primary key,
    name        varchar(50)                        not null comment '标签名称，唯一',
    description varchar(200)                       null comment '标签描述',
    post_count  int      default 0                 not null comment '使用该标签的帖子数量',
    created_at  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    deleted     tinyint  default 0                 not null comment '逻辑删除标记: 0=正常, 1=删除',
    constraint uk_name
        unique (name)
)
    comment '标签表：内容主题分类管理';

create index idx_created_at
    on tags (created_at);

create index idx_deleted
    on tags (deleted);

create index idx_post_count
    on tags (post_count);

create index idx_tag_search
    on tags (deleted asc, name asc, post_count desc);

INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (1, '职场经验', '分享职场生活和工作经验', 1, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (2, '技能提升', '讨论技能学习和能力提升', 3, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (3, '创业分享', '创业经历和商业思考', 0, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (4, '生活平衡', '工作与生活平衡的智慧', 0, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (5, '行业动态', '各行业最新动态和趋势', 0, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (6, '求职指导', '面试技巧和求职经验', 0, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (7, '领导力', '管理和领导力相关话题', 0, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (8, '女性力量', '女性职场权益和发展', 0, '2025-09-21 00:46:17', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (9, 'AIGC', 'AI生成内容技术相关话题', 1, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (10, '职业证书', '职业技能认证和证书考试相关', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (11, '薪资增长', '薪资谈判、涨薪、收入提升相关', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (12, '职场妈妈', '职场母亲工作生活平衡话题', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (13, '数字化转型', '数字化工具和技术转型', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (14, '工作生活平衡', '工作与生活协调平衡', 0, '2025-09-29 21:18:19', 1);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (15, '职业转型', '转行转岗职业发展', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (16, '终身学习', '持续学习和自我提升', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (17, '择业意愿', '职业选择和就业意向', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (18, '升职信心', '职场晋升和自信心建设', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (19, '职场晋升', '升职加薪和职业发展', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (20, '管理建议', '管理经验和职场指导', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (21, '时间管理', '时间规划和效率提升', 2, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (22, 'AI工具', '人工智能工具应用', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (23, '效率提升', '工作效率和生产力提升', 0, '2025-09-29 21:18:19', 0);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (24, 'UpdatedAdminTag', 'Updated tag description by admin', 0, '2025-10-03 01:30:11', 1);
INSERT INTO herizon.tags (id, name, description, post_count, created_at, deleted) VALUES (25, '求职建议', null, 0, '2025-10-09 21:54:32', 0);
