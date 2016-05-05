# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table courier (
  id                        bigint not null,
  courier_id                varchar(255),
  name                      varchar(255),
  working_area_id           bigint,
  phone                     varchar(255),
  current_lat               double,
  current_lon               double,
  access_token              varchar(255),
  refresh_token             varchar(255),
  expires_by                bigint,
  constraint uq_courier_courier_id unique (courier_id),
  constraint pk_courier primary key (id))
;

create table delivery (
  id                        bigint not null,
  order_id                  varchar(255),
  name                      varchar(255),
  phone                     varchar(255),
  appointment_date          timestamp,
  appointment_time_begin    integer,
  appointment_time_end      integer,
  sign_time                 integer,
  status                    integer,
  arrive_time               integer,
  wait_time                 integer,
  leave_time                integer,
  real_time                 bigint,
  msg                       varchar(255),
  address                   varchar(255),
  courier_id                bigint,
  constraint uq_delivery_order_id unique (order_id),
  constraint pk_delivery primary key (id))
;

create table working_area (
  id                        bigint not null,
  city                      varchar(255),
  dist                      varchar(255),
  hashcode                  integer,
  constraint uq_working_area_hashcode unique (hashcode),
  constraint pk_working_area primary key (id))
;

create sequence courier_seq;

create sequence delivery_seq;

create sequence working_area_seq;

alter table courier add constraint fk_courier_working_area_1 foreign key (working_area_id) references working_area (id) on delete restrict on update restrict;
create index ix_courier_working_area_1 on courier (working_area_id);
alter table delivery add constraint fk_delivery_courier_2 foreign key (courier_id) references courier (id) on delete restrict on update restrict;
create index ix_delivery_courier_2 on delivery (courier_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists courier;

drop table if exists delivery;

drop table if exists working_area;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists courier_seq;

drop sequence if exists delivery_seq;

drop sequence if exists working_area_seq;

