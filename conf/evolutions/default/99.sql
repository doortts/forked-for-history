# --- !Ups

create table PULL_REQUEST_ONGOING_REVIEWER (
  PULL_REQUEST_ID                bigint not null,
  USER_ID                        bigint not null,
  constraint PK_PULL_REQUEST_ONGOING_REVIEWER primary key (PULL_REQUEST_ID, USER_ID))
;

alter table PULL_REQUEST_ONGOING_REVIEWER add constraint FK_PULL_REQUEST_ONGOING_REVIEWER_01 foreign key (PULL_REQUEST_ID) references PULL_REQUEST (ID) on delete restrict on update restrict;

alter table PULL_REQUEST_ONGOING_REVIEWER add constraint FK_PULL_REQUEST_ONGOING_REVIEWER_02 foreign key (USER_ID) references N4USER (ID) on delete restrict on update restrict;


# --- !Downs

drop table if exists PULL_REQUEST_ONGOING_REVIEWER;
