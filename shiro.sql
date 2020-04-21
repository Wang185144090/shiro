drop table if exists e_permission;

drop table if exists e_role;

drop table if exists e_user;

drop table if exists r_role_permission;

drop table if exists r_user_role;

/*==============================================================*/
/* Table: e_permission                                          */
/*==============================================================*/
create table e_permission
(
   id                   bigint not null auto_increment comment '权限id',
   permission           varchar(20) comment '权限',
   permission_desc      varchar(50) comment '权限描述',
   primary key (id)
);

alter table e_permission comment '权限表';

/*==============================================================*/
/* Table: e_role                                                */
/*==============================================================*/
create table e_role
(
   id                   bigint not null auto_increment comment '角色id',
   role_name            varchar(20) comment '角色名',
   primary key (id)
);

alter table e_role comment '角色表';

/*==============================================================*/
/* Table: e_user                                                */
/*==============================================================*/
create table e_user
(
   id                   bigint not null auto_increment comment '用户id',
   user_name            varchar(20) comment '用户名',
   user_password        varchar(100) comment '用户密码',
   salt                 char(32) comment '盐',
   primary key (id)
);

alter table e_user comment '用户数据存储的表';

/*==============================================================*/
/* Table: r_role_permission                                     */
/*==============================================================*/
create table r_role_permission
(
   id                   bigint not null auto_increment comment '关系id',
   role_id              bigint comment '角色id',
   permission_id        bigint comment '权限id',
   primary key (id)
);

alter table r_role_permission comment '角色权限关系表';

/*==============================================================*/
/* Table: r_user_role                                           */
/*==============================================================*/
create table r_user_role
(
   id                   bigint not null auto_increment comment '关系id',
   user_id              bigint comment '用户id',
   role_id              bigint comment '角色id',
   primary key (id)
);

alter table r_user_role comment '用户角色关系表';
