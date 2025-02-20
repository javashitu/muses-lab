create table user_id_mapping(
  id bigint not null auto_increment,
  user_id varchar(128),
  primary key (id),
  unique key (user_id)
)

