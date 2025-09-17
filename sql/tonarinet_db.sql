create table tonarinet.country
(
    country_code varchar(5) not null
        primary key,
    name         text       not null,
    description  text       null
);

create table tonarinet.organization
(
    id           int auto_increment
        primary key,
    name         varchar(100)               not null,
    description  text                       null,
    country_code varchar(5)                 not null,
    type         enum ('SCHOOL', 'COMPANY') not null,
    constraint Organization_pk_2
        unique (name),
    constraint Organization_Country_country_code_fk
        foreign key (country_code) references tonarinet.country (country_code)
);

create table tonarinet.board
(
    id           int auto_increment
        primary key,
    title        text       not null,
    description  text       null,
    country_code varchar(5) null,
    org_id       int        null,
    constraint Board_Country_country_code_fk
        foreign key (country_code) references tonarinet.country (country_code),
    constraint Board_Organization_id_fk
        foreign key (org_id) references tonarinet.organization (id)
            on delete cascade
);

create table tonarinet.article
(
    id         int auto_increment
        primary key,
    category   varchar(20)                        null,
    title      text                               not null,
    contents   text                               not null,
    created_by int                                not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    updated_at datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    board_id   int                                not null,
    views      int      default 0                 not null,
    constraint Article_Board_id_fk
        foreign key (board_id) references tonarinet.board (id)
);

create table tonarinet.fileattachment
(
    filepath          text                                 not null,
    original_filename text                                 not null,
    is_private        tinyint(1) default 0                 not null,
    id                int auto_increment
        primary key,
    uploaded_by       int                                  not null,
    type              enum ('IMAGE', 'ATTACHMENT')         null,
    uploaded_at       datetime   default CURRENT_TIMESTAMP not null,
    article_id        int                                  null,
    filesize          int                                  not null comment 'in byte',
    submission_id     int                                  null,
    constraint fileattachment_article_id_fk
        foreign key (article_id) references tonarinet.article (id)
            on delete set null
);

create table tonarinet.region
(
    id           int auto_increment
        primary key,
    country_code varchar(5)  not null,
    category1    varchar(20) null,
    category2    varchar(20) null,
    category3    varchar(20) null,
    category4    varchar(20) null,
    longitude    double      not null,
    latitude     double      not null,
    radius       int         not null,
    constraint Region_Country_country_code_fk
        foreign key (country_code) references tonarinet.country (country_code)
);

create table tonarinet.reply
(
    id         int auto_increment
        primary key,
    created_at datetime default CURRENT_TIMESTAMP not null,
    created_by int                                not null,
    contents   text                               null,
    article_id int                                null,
    constraint Reply_Article_id_fk
        foreign key (article_id) references tonarinet.article (id)
            on delete cascade
);

create index Reply_User_id_fk
    on tonarinet.reply (created_by);

create table tonarinet.tag
(
    article_id int         not null,
    tag_name   varchar(20) not null,
    primary key (article_id, tag_name),
    constraint Tag_Article_id_fk
        foreign key (article_id) references tonarinet.article (id)
);

create table tonarinet.taskgroup
(
    id         int auto_increment
        primary key,
    title      varchar(100)                       null,
    contents   text                               not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    due_date   datetime                           null,
    max_score  int                                null,
    org_id     int                                not null,
    constraint taskgroup_organization_id_fk
        foreign key (org_id) references tonarinet.organization (id)
);

create table tonarinet.user
(
    id              int auto_increment
        primary key,
    email           varchar(50)                          not null,
    password        text                                 not null,
    name            text                                 not null,
    birth           date                                 null,
    nickname        varchar(10)                          not null,
    phone           varchar(20)                          null,
    description     text                                 null,
    created_at      datetime   default CURRENT_TIMESTAMP null,
    provider        varchar(10)                          null,
    oauth_id        text                                 null,
    is_admin        tinyint(1) default 0                 not null,
    gender          varchar(10)                          null,
    nationality     varchar(5) default 'kor'             not null,
    reset_token     text                                 null,
    profile_file_id int                                  null,
    constraint User_pk
        unique (nickname),
    constraint User_pk_2
        unique (email),
    constraint user_country_country_code_fk
        foreign key (nationality) references tonarinet.country (country_code),
    constraint user_fileattachment_id_fk
        foreign key (profile_file_id) references tonarinet.fileattachment (id)
);

alter table tonarinet.article
    add constraint Article_User_id_fk
        foreign key (created_by) references tonarinet.user (id);

create table tonarinet.chatmessage
(
    id          int auto_increment
        primary key,
    sender      int                                  not null,
    message     text                                 not null,
    created_at  datetime   default CURRENT_TIMESTAMP null,
    is_read     tinyint(1) default 0                 not null,
    chatroom_id int                                  not null,
    constraint ChatMessage_User_id_fk
        foreign key (sender) references tonarinet.user (id)
);

create index ChatMessage_ChatRoom_id_fk
    on tonarinet.chatmessage (chatroom_id);

create table tonarinet.chatroom
(
    id             int auto_increment
        primary key,
    title          text                                 not null,
    force_remain   tinyint(1) default 0                 not null,
    description    text                                 null,
    created_at     datetime   default CURRENT_TIMESTAMP null,
    leader_user_id int                                  not null,
    constraint ChatRoom_User_id_fk
        foreign key (leader_user_id) references tonarinet.user (id)
);

alter table tonarinet.fileattachment
    add constraint fileattachment_user_id_fk
        foreign key (uploaded_by) references tonarinet.user (id);

create table tonarinet.livereport
(
    id         int auto_increment
        primary key,
    contents   text                               not null,
    like_count int      default 0                 not null,
    created_at datetime default CURRENT_TIMESTAMP null,
    created_by int                                not null,
    longitude  double                             not null,
    latitude   double                             not null,
    constraint LiveReport_User_id_fk
        foreign key (created_by) references tonarinet.user (id)
);

create table tonarinet.notification
(
    id         int auto_increment
        primary key,
    user_id    int                                  not null,
    contents   text                                 not null,
    link       text                                 null,
    created_at datetime   default CURRENT_TIMESTAMP not null,
    is_read    tinyint(1) default 0                 not null,
    constraint Notification_User_id_fk
        foreign key (user_id) references tonarinet.user (id)
);

create table tonarinet.party
(
    id             int auto_increment
        primary key,
    name           text                 not null,
    leader_user_id int                  not null,
    is_finished    tinyint(1) default 0 not null,
    constraint Party_User_id_fk
        foreign key (leader_user_id) references tonarinet.user (id)
);

create table tonarinet.team
(
    id             int auto_increment
        primary key,
    name           text not null,
    leader_user_id int  not null,
    org_id         int  not null,
    constraint Group_User_id_fk
        foreign key (leader_user_id) references tonarinet.user (id),
    constraint Team_Organization_id_fk
        foreign key (org_id) references tonarinet.organization (id)
);

create table tonarinet.task
(
    id           int auto_increment
        primary key,
    name         text                               not null,
    taskgroup_Id int                                not null,
    contents     text                               not null,
    created_by   int                                not null,
    created_at   datetime default CURRENT_TIMESTAMP null,
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    due_date     datetime                           null,
    user_id      int                                null,
    team_id      int                                null,
    score        int                                null,
    max_score    int                                null,
    feedback     text                               null,
    constraint Task_Team_id_fk
        foreign key (team_id) references tonarinet.team (id),
    constraint Task_User_id_fk
        foreign key (user_id) references tonarinet.user (id),
    constraint Task_User_id_fk_2
        foreign key (created_by) references tonarinet.user (id),
    constraint task_taskgroup_id_fk
        foreign key (taskgroup_Id) references tonarinet.taskgroup (id)
);

create table tonarinet.submission
(
    id         int auto_increment
        primary key,
    created_at datetime default CURRENT_TIMESTAMP not null,
    created_by int                                not null,
    contents   text                               null,
    task_id    int                                not null,
    constraint submission___fk
        foreign key (task_id) references tonarinet.task (id),
    constraint submission_user_id_fk
        foreign key (created_by) references tonarinet.user (id)
);

alter table tonarinet.fileattachment
    add constraint fileattachment_submission_id_fk
        foreign key (submission_id) references tonarinet.submission (id)
            on delete set null;

create table tonarinet.townreview
(
    id             int auto_increment
        primary key,
    contents       text                               not null,
    created_by     int                                not null,
    created_at     datetime default CURRENT_TIMESTAMP null,
    transportation int                                not null,
    safety         int                                not null,
    infra          int                                not null,
    population     int                                not null,
    education      int                                not null,
    region_id      int                                not null,
    country_code   varchar(5)                         not null,
    like_count     int      default 0                 not null,
    constraint TownReview_Country_country_code_fk
        foreign key (country_code) references tonarinet.country (country_code),
    constraint TownReview_Region_id_fk
        foreign key (region_id) references tonarinet.region (id),
    constraint TownReview___fk
        foreign key (created_by) references tonarinet.user (id)
);

create table tonarinet.userchatroom
(
    user_id     int not null,
    chatroom_id int not null,
    primary key (user_id, chatroom_id),
    constraint UserChatRoom_ChatRoom_id_fk
        foreign key (chatroom_id) references tonarinet.chatroom (id),
    constraint UserChatRoom_User_id_fk
        foreign key (user_id) references tonarinet.user (id)
);

create table tonarinet.usercountry
(
    user_id      int                        not null,
    country_code varchar(5)                 not null,
    role         varchar(20) default 'user' not null,
    primary key (user_id, country_code),
    constraint UserCountry_Country_country_code_fk
        foreign key (country_code) references tonarinet.country (country_code),
    constraint UserCountry_User_id_fk
        foreign key (user_id) references tonarinet.user (id)
            on delete cascade
);

create table tonarinet.userlikearticle
(
    user_id    int not null,
    article_id int not null,
    primary key (user_id, article_id),
    constraint userlikearticle_article_id_fk
        foreign key (article_id) references tonarinet.article (id),
    constraint userlikearticle_user_id_fk
        foreign key (user_id) references tonarinet.user (id)
);

create table tonarinet.userparty
(
    user_id       int                  not null,
    party_id      int                  not null,
    entry_message text                 null,
    is_granted    tinyint(1) default 0 null,
    constraint UserParty_Party_id_fk
        foreign key (party_id) references tonarinet.party (id)
            on delete cascade,
    constraint UserParty_User_id_fk
        foreign key (user_id) references tonarinet.user (id)
);

create table tonarinet.userrole
(
    user_id       int                                  not null,
    org_id        int                                  not null,
    role          varchar(20)                          not null,
    is_granted    tinyint(1) default 0                 not null,
    entry_message text                                 null,
    created_at    datetime   default CURRENT_TIMESTAMP not null,
    approved_at   datetime                             null,
    primary key (user_id, org_id),
    constraint UserRole_Organization_id_fk
        foreign key (org_id) references tonarinet.organization (id),
    constraint UserRole_User_id_fk
        foreign key (user_id) references tonarinet.user (id)
);

create table tonarinet.userteam
(
    team_id int not null,
    user_id int not null,
    primary key (user_id, team_id),
    constraint UserTeam_Team_id_fk
        foreign key (team_id) references tonarinet.team (id)
            on delete cascade,
    constraint UserTeam_User_id_fk
        foreign key (user_id) references tonarinet.user (id)
);

