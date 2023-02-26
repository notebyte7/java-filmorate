drop table if exists FILM_LIKES;

drop table if exists FILM_GENRES;

drop table if exists FILMS;

drop table if exists GENRES;

drop table if exists MPA;

drop table if exists USER_FRIENDS;

drop table if exists USERS;





create table if not exists MPA
(
    ID   INTEGER           not null,
    NAME CHARACTER VARYING not null,
    constraint "MPA_pk"
        primary key (ID)
);

create table if not exists FILMS
(
    ID           INTEGER auto_increment,
    NAME         VARCHAR not null,
    DESCRIPTION  VARCHAR(200),
    RELEASE_DATE DATE              not null,
    DURATION     INTEGER           not null,
    RATING       INTEGER           ,
    constraint "FILMS_pk"
        primary key (ID),
    constraint FILMS_MPA_ID_FK
        foreign key (RATING) references MPA
);

create table if not exists GENRES
(
    ID   INTEGER               not null,
    NAME VARCHAR not null,
    constraint GENRES_PK
        primary key (ID)
);

create table if not exists FILM_GENRES
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint "FILM_GENRES_GENRE_ID_fk"
        foreign key (GENRE_ID) references GENRES,
    constraint "FILM_GENRES_FILMS_ID_fk"
        foreign key (FILM_ID) references FILMS
);

create table if not exists USERS
(
    ID       INTEGER auto_increment,
    EMAIL    VARCHAR not null,
    LOGIN    VARCHAR not null,
    NAME     VARCHAR,
    BIRTHDAY DATE              not null,
    constraint "USERS_pk"
        primary key (ID)
);

create table if not exists FILM_LIKES
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint "FILM_LIKES_pk"
        primary key (FILM_ID, USER_ID),
    constraint "FILM_LIKES_USERS_ID_fk"
        foreign key (USER_ID) references USERS,
    constraint "film_likes_FILMS_ID_fk"
        foreign key (FILM_ID) references FILMS
);

create table if not exists USER_FRIENDS
(
    USER_ID      INTEGER not null,
    FRIEND_ID    INTEGER not null,
    CONFIRMATION BOOLEAN,
    constraint "USER_LIKES_pk"
        primary key (USER_ID, FRIEND_ID),
    constraint "user_friends_USERS_ID_fk"
        foreign key (USER_ID) references USERS,
    constraint "user_friends_FRIENDS_ID_fk"
        foreign key (FRIEND_ID) references USERS
);

