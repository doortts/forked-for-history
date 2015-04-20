# --- !Ups

create table visited_page (
  id                        bigint not null,
  path                      varchar(255),
  title                     varchar(255),
  constraint uq_visited_page_path unique (path),
  constraint pk_visited_page primary key (id))
;

create table visited_page_user (
  visited_page_id                bigint not null,
  user_id                        bigint not null,
  constraint pk_visited_page_user primary key (visited_page_id, user_id))
;

create sequence visited_page_seq;

alter table visited_page_user add constraint fk_visited_page_user_visited__01 foreign key (visited_page_id) references visited_page (id) on delete restrict on update restrict;

alter table visited_page_user add constraint fk_visited_page_user_n4user_02 foreign key (user_id) references n4user (id) on delete restrict on update restrict;


# --- !Downs

drop table if exists visited_page_user;
drop table if exists visited_page;
drop sequence if exists visited_page_seq;
