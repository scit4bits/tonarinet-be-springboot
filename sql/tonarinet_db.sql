CREATE TABLE `article` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category` varchar(20) DEFAULT NULL,
  `title` text NOT NULL,
  `contents` text NOT NULL,
  `created_by` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `board_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Article_Board_id_fk` (`board_id`),
  KEY `Article_User_id_fk` (`created_by`),
  CONSTRAINT `Article_Board_id_fk` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`),
  CONSTRAINT `Article_User_id_fk` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `board` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` text NOT NULL,
  `description` text,
  `country_code` varchar(5) DEFAULT NULL,
  `org_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Board_Country_country_code_fk` (`country_code`),
  KEY `Board_Organization_id_fk` (`org_id`),
  CONSTRAINT `Board_Country_country_code_fk` FOREIGN KEY (`country_code`) REFERENCES `country` (`country_code`),
  CONSTRAINT `Board_Organization_id_fk` FOREIGN KEY (`org_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `chatmessage` (
  `id` int NOT NULL AUTO_INCREMENT,
  `sender` int NOT NULL,
  `message` text NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `chatroom_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ChatMessage_ChatRoom_id_fk` (`chatroom_id`),
  KEY `ChatMessage_User_id_fk` (`sender`),
  CONSTRAINT `ChatMessage_ChatRoom_id_fk` FOREIGN KEY (`chatroom_id`) REFERENCES `chatroom` (`id`),
  CONSTRAINT `ChatMessage_User_id_fk` FOREIGN KEY (`sender`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `chatroom` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` text NOT NULL,
  `force_remain` tinyint(1) NOT NULL DEFAULT '0',
  `description` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `leader_user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ChatRoom_User_id_fk` (`leader_user_id`),
  CONSTRAINT `ChatRoom_User_id_fk` FOREIGN KEY (`leader_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `country` (
  `country_code` varchar(5) NOT NULL,
  `name` text NOT NULL,
  `description` text,
  PRIMARY KEY (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `livereport` (
  `id` int NOT NULL AUTO_INCREMENT,
  `contents` text NOT NULL,
  `like_count` int NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `created_by` int NOT NULL,
  `longitude` text NOT NULL,
  `latitude` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `LiveReport_User_id_fk` (`created_by`),
  CONSTRAINT `LiveReport_User_id_fk` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `contents` text NOT NULL,
  `link` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `Notification_User_id_fk` (`user_id`),
  CONSTRAINT `Notification_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `organization` (
  `id` int NOT NULL AUTO_INCREMENT,
  `kor_name` varchar(60) NOT NULL,
  `description` text,
  `country_code` varchar(5) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `Organization_pk_2` (`kor_name`),
  KEY `Organization_Country_country_code_fk` (`country_code`),
  CONSTRAINT `Organization_Country_country_code_fk` FOREIGN KEY (`country_code`) REFERENCES `country` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `party` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `leader_user_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Party_User_id_fk` (`leader_user_id`),
  CONSTRAINT `Party_User_id_fk` FOREIGN KEY (`leader_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `region` (
  `id` int NOT NULL AUTO_INCREMENT,
  `country_code` varchar(5) NOT NULL,
  `category1` varchar(20) DEFAULT NULL,
  `category2` varchar(20) DEFAULT NULL,
  `category3` varchar(20) DEFAULT NULL,
  `category4` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Region_Country_country_code_fk` (`country_code`),
  CONSTRAINT `Region_Country_country_code_fk` FOREIGN KEY (`country_code`) REFERENCES `country` (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `reply` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` int NOT NULL,
  `contents` text,
  `article_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Reply_Article_id_fk` (`article_id`),
  KEY `Reply_User_id_fk` (`created_by`),
  CONSTRAINT `Reply_Article_id_fk` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`) ON DELETE CASCADE,
  CONSTRAINT `Reply_User_id_fk` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `tag` (
  `article_id` int NOT NULL,
  `tag_name` varchar(20) NOT NULL,
  PRIMARY KEY (`article_id`,`tag_name`),
  CONSTRAINT `Tag_Article_id_fk` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `task` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `contents` text NOT NULL,
  `created_by` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `due_date` datetime DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `team_id` int DEFAULT NULL,
  `score` int DEFAULT NULL,
  `max_score` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Task_Team_id_fk` (`team_id`),
  KEY `Task_User_id_fk` (`user_id`),
  KEY `Task_User_id_fk_2` (`created_by`),
  CONSTRAINT `Task_Team_id_fk` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
  CONSTRAINT `Task_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `Task_User_id_fk_2` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `team` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` text NOT NULL,
  `leader_user_id` int NOT NULL,
  `org_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Group_User_id_fk` (`leader_user_id`),
  KEY `Team_Organization_id_fk` (`org_id`),
  CONSTRAINT `Group_User_id_fk` FOREIGN KEY (`leader_user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `Team_Organization_id_fk` FOREIGN KEY (`org_id`) REFERENCES `organization` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `townreview` (
  `id` int NOT NULL AUTO_INCREMENT,
  `contents` text NOT NULL,
  `created_by` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `transportation` int NOT NULL,
  `safety` int NOT NULL,
  `infra` int NOT NULL,
  `population` int NOT NULL,
  `education` int NOT NULL,
  `region_id` int NOT NULL,
  `country_code` varchar(5) NOT NULL,
  `like_count` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `TownReview_Country_country_code_fk` (`country_code`),
  KEY `TownReview_Region_id_fk` (`region_id`),
  KEY `TownReview___fk` (`created_by`),
  CONSTRAINT `TownReview___fk` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`),
  CONSTRAINT `TownReview_Country_country_code_fk` FOREIGN KEY (`country_code`) REFERENCES `country` (`country_code`),
  CONSTRAINT `TownReview_Region_id_fk` FOREIGN KEY (`region_id`) REFERENCES `region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `password` text NOT NULL,
  `name` text NOT NULL,
  `birth` date DEFAULT NULL,
  `nickname` varchar(10) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `description` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `provider` varchar(10) DEFAULT NULL,
  `oauth_id` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `User_pk_2` (`email`),
  UNIQUE KEY `User_pk` (`nickname`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `userchatroom` (
  `user_id` int NOT NULL,
  `chatroom_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`chatroom_id`),
  KEY `UserChatRoom_ChatRoom_id_fk` (`chatroom_id`),
  CONSTRAINT `UserChatRoom_ChatRoom_id_fk` FOREIGN KEY (`chatroom_id`) REFERENCES `chatroom` (`id`),
  CONSTRAINT `UserChatRoom_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `usercountry` (
  `user_id` int NOT NULL,
  `country_code` varchar(5) NOT NULL,
  `role` varchar(20) NOT NULL,
  PRIMARY KEY (`user_id`,`country_code`,`role`),
  KEY `UserCountry_Country_country_code_fk` (`country_code`),
  CONSTRAINT `UserCountry_Country_country_code_fk` FOREIGN KEY (`country_code`) REFERENCES `country` (`country_code`),
  CONSTRAINT `UserCountry_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `userparty` (
  `user_id` int NOT NULL,
  `party_id` int NOT NULL,
  KEY `UserParty_Party_id_fk` (`party_id`),
  KEY `UserParty_User_id_fk` (`user_id`),
  CONSTRAINT `UserParty_Party_id_fk` FOREIGN KEY (`party_id`) REFERENCES `party` (`id`),
  CONSTRAINT `UserParty_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `userrole` (
  `user_id` int NOT NULL,
  `org_id` int NOT NULL,
  `role` varchar(20) NOT NULL,
  PRIMARY KEY (`user_id`,`org_id`,`role`),
  KEY `UserRole_Organization_id_fk` (`org_id`),
  CONSTRAINT `UserRole_Organization_id_fk` FOREIGN KEY (`org_id`) REFERENCES `organization` (`id`),
  CONSTRAINT `UserRole_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `userteam` (
  `team_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`team_id`),
  KEY `UserTeam_Team_id_fk` (`team_id`),
  CONSTRAINT `UserTeam_Team_id_fk` FOREIGN KEY (`team_id`) REFERENCES `team` (`id`),
  CONSTRAINT `UserTeam_User_id_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

