--drop table if exists likes;
--drop table if exists  films;
--drop table if exists  mpa;
create table  if not  exists mpa
(
id int primary key ,
name char(10)
);

create table  if not  exists films
(
    id int auto_increment primary key ,
    name varchar(200) not null,
    description varchar(200) not null,
    release_date datetime not null,
    duration int not null,
    mpa_id int,
    foreign key(mpa_id) references mpa(id)
);

create table  if not  exists likes
(
    film_id int not null,
    user_id int not null,
    foreign key (film_id) references films(id),
--    todo:
--    foreign key (user_id) references users(id),
--    constraint LIKES_FILM_USER_UNIQUE unique (film_id, user_id)
    primary key  (film_id, user_id)
);
