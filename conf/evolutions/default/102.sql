# --- !Ups

CREATE TABLE visited_page (
  id                        BIGINT NOT NULL,
  path                      VARCHAR(255),
  title                     VARCHAR(255),
  last_comment_added_time   BIGINT,
  CONSTRAINT uq_visited_page_path UNIQUE (path),
  CONSTRAINT pk_visited_page PRIMARY KEY (id))
;

CREATE TABLE user_visited_page (
  id                        BIGINT NOT NULL,
  visited_page_id           BIGINT NOT NULL,
  user_id                   BIGINT NOT NULL,
  last_visited_time         bigint not null,
  CONSTRAINT pk_user_visited_page PRIMARY KEY (id),
  CONSTRAINT uq_user_visited_page_01 UNIQUE(visited_page_id, user_id))
;

ALTER TABLE pull_request ADD COLUMN last_comment_added_time BIGINT;

CREATE SEQUENCE visited_page_seq;
CREATE SEQUENCE user_visited_page_seq;

ALTER TABLE user_visited_page ADD CONSTRAINT fk_user_visited_page_01 FOREIGN KEY (visited_page_id) REFERENCES visited_page (id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE user_visited_page ADD CONSTRAINT fk_user_visited_page_02 FOREIGN KEY (user_id) REFERENCES n4user (id) ON DELETE CASCADE ON UPDATE RESTRICT;
CREATE INDEX ix_user_visited_page_01 on user_visited_page (last_visited_time);


# --- !Downs

DROP TABLE IF EXISTS user_visited_page;
DROP TABLE IF EXISTS visited_page;
DROP SEQUENCE IF EXISTS visited_page_seq;
DROP SEQUENCE IF EXISTS user_visited_page_seq;
DROP INDEX IF EXISTS ix_uinque_user_visited_page_01;
DROP INDEX IF EXISTS ix_user_visited_page_01;

ALTER TABLE pull_request DROP COLUMN IF EXISTS last_comment_added_time;
