DROP TABLE IF EXISTS user;

CREATE TABLE user
(
	id BIGINT(20) NOT NULL COMMENT '主键ID' AUTO_INCREMENT,
	user_name VARCHAR(30) NULL DEFAULT NULL COMMENT '姓名',
	password VARCHAR(32) NULL DEFAULT NULL COMMENT '密码',
	email VARCHAR(50) NULL DEFAULT NULL COMMENT '邮箱',
	birthday DATE NOT NULL COMMENT '生日',

	create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,

	PRIMARY KEY (id)

);


INSERT INTO user (id, user_name, password, birthday, email) VALUES
(1, 'Jone', '123456', '1992-05-21', 'test1@baomidou.com'),
(2, 'Jack', '123456','1987-02-21', 'test2@baomidou.com'),
(3, 'Tom','123456', '1999-05-31', 'test3@baomidou.com'),
(4, 'Sandy', '123456','1995-09-12', 'test4@baomidou.com'),
(5, 'Billie', '123456','1998-07-01', 'test5@baomidou.com');


CREATE TABLE USER_ORDER (
	id BIGINT (20) NOT NULL COMMENT '主键ID' AUTO_INCREMENT,
	user_id BIGINT (20) NOT NULL COMMENT '用户id',
	name VARCHAR (64) COMMENT '商品名称',
	num INTEGER COMMENT '商品数量',
	price DECIMAL (11, 2) COMMENT '单价',

	create_by VARCHAR (32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR (32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,
	PRIMARY KEY (id)
);


drop table if exists log_channel_info;

/*==============================================================*/
/* Table: log_channel_info                                      */
/*==============================================================*/
create table log_channel_info
(
    id                   bigint not null auto_increment,
    device_id            varchar(64) comment '设备id',
    remote_address       varchar(32) comment '客户端远程地址',
    status               tinyint comment '连接状态 1 注册  2 在线 3 掉线',
    registry_time        datetime comment '注册时间',
    change_time          datetime comment '最后一次状态变化时间',
    create_date          datetime default CURRENT_TIMESTAMP,
    update_date          datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id       bigint default 0,
    update_user_id       bigint default 0,
    is_del               tinyint default 0,
    primary key (id)
);

alter table log_channel_info comment '设备连接到服务端的连接通道信息';


drop table if exists message_bank_sms;

/*==============================================================*/
/* Table: message_bank_sms                                      */
/*==============================================================*/
create table message_bank_sms
(
    id                   bigint not null auto_increment,
    device_id            varchar(64) comment '短信的设备id',
    remote_address       varchar(32) comment '设备的远程ip地址',
    sender               varchar(5) comment '发送方号码，暂定拿5位号码验证，必须是5位，或者维护所有银行的客服号码，做一个校验；
            与订单服务对接时，这个值也需要；
            订单服务需要校验收件号码和设备id和金额同时满足同一个人',
    receiver             varchar(11) comment '收件方号码',
    receive_time         datetime comment '收件时间，以安卓设备短信时间为准',
    content              varchar(128) comment '短信内容',
    create_date          datetime default CURRENT_TIMESTAMP,
    update_date          datetime default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id       bigint default 0,
    update_user_id       bigint default 0,
    is_del               tinyint default 0,
    primary key (id)
);

alter table message_bank_sms comment 'app推送给服务端的银行收款短信';

