DROP TABLE IF EXISTS boot_user;

CREATE TABLE boot_user
(
	id BIGINT(20) NOT NULL COMMENT '主键ID' AUTO_INCREMENT,
	user_name VARCHAR(30) NOT NULL COMMENT '姓名',
	password VARCHAR(32) NOT NULL COMMENT '密码',
	email VARCHAR(50) NULL DEFAULT NULL COMMENT '邮箱',
	birthday DATE NOT NULL COMMENT '生日',
    last_modify_password bigint NOT NULL COMMENT '最后一次修改密码的时间',
    is_enable TINYINT(1) NOT NULL DEFAULT 1 COMMENT '用户是否有效， 0否1是',
    org_code VARCHAR(32) NULL COMMENT '用户所属组织代码',

	create_by VARCHAR(32) NULL,
	create_time TIMESTAMP NULL,
	modify_by VARCHAR(32) NULL,
	modify_time TIMESTAMP NULL,
	removed INT NOT NULL DEFAULT 0,
	version INT NOT NULL DEFAULT 1,

	PRIMARY KEY (id)

);


INSERT INTO boot_user (id, user_name, password, birthday, email) VALUES
(1, 'Jone', '123456', '1992-05-21', 'test1@baomidou.com'),
(2, 'Jack', '123456','1987-02-21', 'test2@baomidou.com'),
(3, 'Tom','123456', '1999-05-31', 'test3@baomidou.com'),
(4, 'Sandy', '123456','1995-09-12', 'test4@baomidou.com'),
(5, 'Billie', '123456','1998-07-01', 'test5@baomidou.com');

drop table if exists `boot_user_order`;
CREATE TABLE boot_user_order (
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


drop table if exists boot_log_channel_info;

/*==============================================================*/
/* Table: log_channel_info                                      */
/*==============================================================*/
CREATE TABLE `boot_base_channel_info` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT,
     `device_no` varchar(64) NOT NULL DEFAULT '' COMMENT '设备号',
     `server_address` varchar(64) NOT NULL DEFAULT '' COMMENT '服务端地址',
     `client_address` varchar(64) DEFAULT '' COMMENT '客户端远程地址',
     `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '连接状态 1 注册  2 在线 3 掉线',
     `registry_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
     `change_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后一次状态变化时间',
     create_by VARCHAR (32) NULL,
     create_time TIMESTAMP NULL,
     modify_by VARCHAR (32) NULL,
     modify_time TIMESTAMP NULL,
     removed INT NOT NULL DEFAULT 0,
     version INT NOT NULL DEFAULT 1,
     `remark` varchar(255) DEFAULT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=373 DEFAULT CHARSET=utf8 COMMENT='设备连接到服务端的连接通道信息';


drop table if exists `boot_message_bank_sms`;

CREATE TABLE `boot_message_bank_sms` (
        `id` bigint(20) NOT NULL AUTO_INCREMENT,
        `device_no` varchar(64) NOT NULL COMMENT '短信的设备号',
        `message_id` varchar(64) NOT NULL COMMENT '短信的唯一标识符',
        `client_address` varchar(64) NOT NULL DEFAULT '' COMMENT '设备的远程ip地址',
        `sender` varchar(5) NOT NULL COMMENT '发送方号码，暂定拿5位号码验证，必须是5位，或者维护所有银行的客服号码，做一个校验；\r\n            与订单服务对接时，这个值也需要；\r\n            订单服务需要校验收件号码和设备id和金额同时满足同一个人',
        `receiver` varchar(11) DEFAULT '' COMMENT '收件方号码',
        `receive_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收件时间，以安卓设备短信时间为准',
        `send_status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '发送状态 1 未发送 2 发送并消费成功',
        `content` varchar(256) NOT NULL COMMENT '短信内容',
        create_by VARCHAR (32) NULL,
        create_time TIMESTAMP NULL,
        modify_by VARCHAR (32) NULL,
        modify_time TIMESTAMP NULL,
        removed INT NOT NULL DEFAULT 0,
        version INT NOT NULL DEFAULT 1,
        PRIMARY KEY (`id`),
        UNIQUE KEY `message_id_unique` (`message_id`) USING BTREE,
        KEY `send_status_index` (`send_status`,`device_no`,`message_id`,`sender`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8 COMMENT='app推送给服务端的银行收款短信';

drop table if exists `boot_log_channel_transfer`;

CREATE TABLE `boot_log_channel_transfer` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `request_id` varchar(64) NOT NULL COMMENT '请求id，用来响应和判断客户端是否重复请求',
    `device_no` varchar(64) NOT NULL DEFAULT '' COMMENT '安卓设备号',
    `server_address` varchar(32) NOT NULL DEFAULT '' COMMENT '服务端地址',
    `client_address` varchar(32) NOT NULL DEFAULT '' COMMENT '客户端地址',
    `content` text NOT NULL COMMENT '传输内容',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0 未验签通过 1 验签通过未处理 2  验签通过并上传成功',
    create_by VARCHAR (32) NULL,
    create_time TIMESTAMP NULL,
    modify_by VARCHAR (32) NULL,
    modify_time TIMESTAMP NULL,
    removed INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=385 DEFAULT CHARSET=utf8 COMMENT='通道传输报文日志记录';



