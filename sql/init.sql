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


drop table if exists yk_pay_channel_info;

/*==============================================================*/
/* Table: yk_pay_channel_info                                   */
/*==============================================================*/
create table yk_pay_channel_info
(
    id                   bigint not null auto_increment,
    device_id            varchar(64) comment '设备id',
    remote_address       varchar(32) comment '客户端远程地址',
    status               tinyint comment '连接状态 1 注册  2 在线 3 掉线',
    registry_time        datetime comment '注册时间',
    change_time          datetime comment '最后一次状态变化时间',
    create_by            varchar(64) comment '创建人',
    create_time          datetime comment '创建时间',
    modify_by            varchar(64) comment '修改人',
    modify_time          datetime comment '修改时间',
    removed              tinyint default 0 comment '是否删除
            0-未删除/1-已删除',
    version              int default 0 comment '版本号',
    primary key (id)
);
