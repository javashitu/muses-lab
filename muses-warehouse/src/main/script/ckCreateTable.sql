--muses_warehouse--
CREATE TABLE muses_warehouse.video_tag_bitmap
(
    `label_name` String COMMENT '标签名',
    `label_value` String COMMENT '标签值',
    `video_ids` AggregateFunction(groupBitmap, UInt64) COMMENT 'bitmap格式video id'
)
ENGINE = AggregatingMergeTree
PARTITION BY label_name
ORDER BY (label_name, label_value)
SETTINGS index_granularity = 8192

insert into video_tag_bitmap(label_name,label_value,video_ids) SELECT 'resolution','360',groupBitmapState(toUInt64(808321));
insert into video_tag_bitmap(label_name,label_value,video_ids) SELECT 'resolution','480',groupBitmapState(toUInt64(808322));
insert into video_tag_bitmap(label_name,label_value,video_ids) SELECT 'resolution','480',groupBitmapState(toUInt64(808323));
insert into video_tag_bitmap(label_name,label_value,video_ids) SELECT 'frame_rate','25',groupBitmapState(toUInt64(808321));
insert into video_tag_bitmap(label_name,label_value,video_ids) SELECT 'frame_rate','25',groupBitmapState(toUInt64(808323));
insert into video_tag_bitmap(label_name,label_value,video_ids) SELECT 'frame_rate','50',groupBitmapState(toUInt64(808322));


CREATE TABLE muses_warehouse.user_tag_bitmap
(
    `label_name` String COMMENT '标签名',
    `label_value` String COMMENT '标签值',
    `user_ids` AggregateFunction(groupBitmap, UInt64) COMMENT 'bitmap格式user id'
)
ENGINE = AggregatingMergeTree
PARTITION BY label_name
ORDER BY (label_name, label_value)
SETTINGS index_granularity = 8192


CREATE TABLE muses_warehouse.video_program_statistics
(
    `video_id` String,
    `video_name` String,
    `pub_time` Int64 COMMENT '发布时间,时间戳',
    `pub_date` String COMMENT '发布日期,yyyy-MM-dd',
    `user_id` String,
    `user_name` String,
    `play` Int32 COMMENT '播放次数',
    `likes` Int32 COMMENT '点赞次数',
    INDEX idx_video_id video_id TYPE minmax GRANULARITY 8,
    INDEX idx_user_id user_id TYPE minmax GRANULARITY 8
)
ENGINE = SummingMergeTree((play, likes))
PARTITION BY pub_date
PRIMARY KEY (video_id)
ORDER BY (video_id, user_id)
SETTINGS index_granularity = 8192;
insert into video_program_statistics values('808321','母猪产后护理', 1739618762221, '2025-1-1', '9527', '9527', 10, 3);
insert into video_program_statistics values('808321','母猪产后护理', 1739618762221, '2025-1-1', '9527', '9527', 20, 5);


CREATE TABLE muses_warehouse.video_event
(
    `video_id` String,
    `user_id` String,
    `event` String COMMENT 'view click like , 后面三个事件暂时不用share collect coin',
    `operate_time` Int64,
    `operate_date` FixedString(10) COMMENT '操作日期，格式化 2025-01-01'
)
ENGINE = MergeTree
PARTITION BY operate_date
PRIMARY KEY video_id
ORDER BY (video_id, user_id)
SETTINGS index_granularity = 8192;


create table video_tag(
`video_id` String,
`resolution` Int8 COMMENT '360p - 0, 480p - 1, 540p -2, 720p - 3, 1080p - 4, 2k(1440) - 5,4k(2160) -6, 以上7',
`frame_rate` Int8 COMMENT '25 - 0, 30 - 1, 50 - 2, 60 - 3, 以上4',
`bit_rate` Int8 COMMENT '512kb -0 , 1mb - 1, 2.5mb -2, 5mb-3, 10mb- 4, 15mb-5, 25mb -6, 50mb -7, 75mb - 8,以上9',
`duration` Int8 COMMENT '30s - 0, 1min - 1, 2min - 2, 5min - 3, 10min -4, 30min - 5, 60min -6,以上7 ',
`topic_tag` String COMMENT '["动漫","热血"]',
`create_date` String,
)engine = MergeTree
PARTITION BY create_date
PRIMARY KEY video_id
ORDER BY video_id

insert into muses_warehouse.video_tag values('764802', 0,0,0,0,'["热血","动漫"]','2025-01-01')
insert into muses_warehouse.video_tag values('764803', 0,0,0,0,'["恋爱","校园"]','2025-01-01')
insert into muses_warehouse.video_tag values('764804', 7,4,9,7,'["中二","魔幻"]','2025-01-01')


CREATE TABLE user_info(
    `user_id` String,
    `age` Int16,
    `sex` String,
    `city` String,
    `create_date` String,
)engine=MergeTree
PARTITION BY create_date
PRIMARY KEY user_id
ORDER BY user_id

insert into muses_warehouse.user_info values('9527', 30,'男','上海','2025-01-01' )
insert into muses_warehouse.user_info values('9528', 31,'女','上海','2025-01-01' )
insert into muses_warehouse.user_info values('9529', 33,'女','北京','2025-01-01' )

--muses_warehouse--
--muses_lab--

CREATE TABLE video_id_mapping (
    id Int64,
    video_id String
) ENGINE = MergeTree
order by video_id


CREATE TABLE video_itermcf(
    user_id String,
    video_id String,
    score String,
)ENGINE = MergeTree
ORDER BY user_id

CREATE TABLE video_als(
    user_id String,
    video_id String,
    rating Double,
)ENGINE=MergeTree
ORDER BY user_id

CREATE TABLE video_feature_vector(
    video_id String,
    features String,
)ENGINE = MergeTree
ORDER BY video_id

CREATE TABLE video_embedding(
    video_id String,
    embedding String,
    create_date String,
)ENGINE = MergeTree
PARTITION BY create_date
ORDER BY video_id

CREATE TABLE user_feature_vector(
    user_id String,
    features String,
)ENGINE=MergeTree
ORDER BY user_id


CREATE TABLE video_user_label(
    user_id String,
    video_id String,
    label Int32,
)ENGINE=MergeTree
ORDER BY video_id

insert into video_user_label
 with t1 as (select * from video_event where event = 'click'), t2 as (select * from video_event where event = 'collect'),t3 as (select t1.video_id, t1.user_id,t1.event as click,t2.event as collect , case when t2.event = '' then 0 else 1 end as label from t1 left join t2 on t1.video_id = t2.video_id)
 select user_id,video_id, label from t3
--muses_lab--
