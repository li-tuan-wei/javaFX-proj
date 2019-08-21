create table tree (
  id int auto_increment,
  name varchar(200) not null,
  desc varchar(1024),
  type varchar(50) not null,
  parent_id int not null,
  index int not null,
  path varchar(1024) null,
  enable BOOLEAN not null,
  PRIMARY key (id)
);
create index idx_tree_pid on tree(parent_id);

create table param (
  id int auto_increment,
  name varchar(200) not null,
  tree_node_id int not null,
  category varchar(50) not null,
  index int not null,
  class_name varchar(256) not null,
  value varchar(1024) not null,
  desc varchar(1024) not null,
  PRIMARY key(id)
);
create index idx_param on param(tree_node_id);
create index udx_param on param(name, tree_node_id);

create table test_method (
  id int auto_increment,
  tree_node_id int not null,
  param_id int not null,
  class_name varchar(256) not null,
  method_name varchar(512) not null,
  instance_class_name varchar(100) not null,
  PRIMARY key(id)
);
create index idx_test_method on test_method(tree_node_id);

create table test_method_data (
  id int auto_increment,
  name varchar(200) not null,
  test_method_id int not null,
  data text not null,
  var_name varchar(200),
  PRIMARY key(id)
);
create index idx_test_method_data on test_method_data(test_method_id);

create table increment_var (
  id int auto_increment,
  name varchar(200) not null,
  value bigint not null,
  step int not null,
  PRIMARY key(id)
);
create UNIQUE index udx_name on increment_var(name);

create table bean_var (
  id int auto_increment,
  index int not null,
  tree_node_id int not null,
  name varchar(200) not null,
  type varchar(50) not null,
  database_param_id int,
  sql varchar(1024),
  args varchar(1024),
  package_param_id int,
  class_name varchar(512),
  instance_class_type varchar(100),
  method_name varchar(2048),
  PRIMARY key(id)
);
create index idx_tid_bean_var on bean_var(tree_node_id);
create index idx_name_bean_var on bean_var(name);

create table sql_check (
  id int auto_increment,
  tree_node_id int not null,
  name varchar(200) not null,
  sql varchar(1024) not null,
  database_param_id int not null,
  sql_struct varchar(1024) not null,
  args varchar(256) not null,
  PRIMARY key(id)
);
create index idx_tid_sql_check on sql_check(tree_node_id);
create index idx_name_sql_check on sql_check(name);

create table sql_check_data (
  id int auto_increment,
  sql_check_id int not null,
  content varchar(2048) not null,
  index int not null,
  PRIMARY key(id)
);
create index idx_scid_scd on sql_check_data(sql_check_id);

create table test_http (
  id int auto_increment,
  url varchar(1024) not null,
  method varchar(50) not null,
  tree_node_id int not null,
  PRIMARY key(id)
);
create index idx_tnid_test_http on test_http(tree_node_id);

create table test_http_param (
  id int auto_increment,
  test_http_id int not null,
  name varchar(100),
  content varchar(1024),
  type varchar(100),
  PRIMARY  key (id)
);
create index idx_thid_test_http_param on test_http_param(test_http_id);

create table test_http_body (
  id int auto_increment,
  test_http_id int not null,
  body varchar(2048),
  content_type varchar(100),
  PRIMARY  key (id)
);
create index idx_thid_test_http_body on test_http_body(test_http_id);

create table test_http_result (
  id int auto_increment,
  test_http_id int not null,
  result varchar(2048),
  var_name varchar(100),
  PRIMARY  key (id)
);
create index idx_thid_test_http_result on test_http_result(test_http_id);

create table test_log (
  id          INT AUTO_INCREMENT,
  name        VARCHAR(512) NOT NULL,
  create_time TIMESTAMP    NOT NULL,
  success     int      NOT NULL,
  type        VARCHAR(50) not null,
  parent_id  int not null,
  success_num INT          NOT NULL,
  failure_num INT          NOT NULL
);
create index idx_tl_parent_id on test_log(parent_id);

create table test_log_data (
  id int auto_increment,
  test_log_id int not null,
  name varchar(512) not null,
  type VARCHAR(50) not null,
  content clob not null
);
create index idx_tld_log_id on test_log_data(test_log_id);

create table bean_check (
  id int auto_increment,
  tree_node_id int not null,
  check_name varchar(128) not null,
  other_info varchar(256),
  bean_type varchar(64) not null,
  columns varchar(512) not null,
  content clob not null
);
create unique index idx_bc_tnid on bean_check(tree_node_id);

create table common_fun (
  id int auto_increment,
  tree_node_id int not null,
  name varchar(200) not null,
  package_param_id int,
  class_name varchar(512),
  desc varchar(512),
  PRIMARY key(id)
);
create index idx_tid_common_fun on common_fun(tree_node_id);

create table sql_data (
  id int auto_increment,
  tree_node_id int not null,
  database_param_id int not null,
  handle_type varchar(56) not null,
  data_type varchar(56) not null,
  data clob not null
);
create index idx_tid_sql_data on sql_data(tree_node_id);

create table data_export (
  id int auto_increment,
  tree_node_id int not null,
  database_param_id int not null,
  dir varchar(256) not null,
  name varchar(256) not null,
  data clob not null
);
create index idx_tid_data_export on data_export(tree_node_id);