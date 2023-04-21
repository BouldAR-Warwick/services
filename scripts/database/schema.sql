DROP TABLE IF EXISTS `user_in_gym`;
DROP TABLE IF EXISTS `routes`;
DROP TABLE IF EXISTS `walls`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `gyms`;

CREATE TABLE users(
    UID         INTEGER       NOT NULL      AUTO_INCREMENT,
    Username    VARCHAR(20)   UNIQUE NOT NULL,
    Email       VARCHAR(320)  UNIQUE NOT NULL,
    Password    VARCHAR(64)   NOT NULL,
    PRIMARY KEY (UID)
)AUTO_INCREMENT = 1;
 
CREATE TABLE gyms(
    GID            INTEGER       NOT NULL   AUTO_INCREMENT,
    GymLocation    VARCHAR(32)   NOT NULL,
    GymName        VARCHAR(64)   UNIQUE NOT NULL,
    PRIMARY KEY (GID)
)AUTO_INCREMENT = 1;

CREATE TABLE walls(
    WID             INTEGER         NOT NULL    AUTO_INCREMENT,
    GID             INTEGER         NOT NULL,
    WallDescription VARCHAR(100),
    WallContent     MEDIUMTEXT      NOT NULL,
    image_file_name VARCHAR(255)    NOT NULL,
    PRIMARY KEY (WID),
    FOREIGN KEY (GID) REFERENCES gyms(GID)
)AUTO_INCREMENT = 1;

CREATE TABLE routes(
    RID             INTEGER     NOT NULL    AUTO_INCREMENT,
    WID             INTEGER     NOT NULL,
    creator_user_id INTEGER     NOT NULL,
    Difficulty      INTEGER     NOT NULL,
    route_content    MEDIUMTEXT  NOT NULL,
    image_file_name VARCHAR(255),
    PRIMARY KEY (RID),
    FOREIGN KEY (WID)
        REFERENCES walls(WID),
    FOREIGN KEY (creator_user_id)
        REFERENCES users(UID)
)AUTO_INCREMENT = 1;

CREATE TABLE user_in_gym(
    UID         INTEGER       NOT NULL,
    GID         INTEGER       NOT NULL,
    PRIMARY KEY (UID),
    FOREIGN KEY (UID) 
        REFERENCES users(UID),
    FOREIGN KEY (GID) 
        REFERENCES gyms(GID)
);

INSERT INTO `users` (Username, Email, Password)
VALUES ('Testuser', '123@mail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO `users` (Username, Email, Password)
VALUES ('Yang', '456@mail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO `users` (Username, Email, Password)
VALUES ('Tom', '789@mail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');
INSERT INTO `users` (Username, Email, Password)
VALUES ('Tian', '321@mail.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92');

INSERT INTO `gyms` (GymLocation, GymName)
VALUES ('CV4', 'Warwick Sport Centre');
INSERT INTO `gyms` (GymLocation, GymName)
VALUES ('CV5', 'Test Gym1');
INSERT INTO `gyms` (GymLocation, GymName)
VALUES ('CV6', 'Test Gym2');
INSERT INTO `gyms` (GymLocation, GymName)
VALUES ('Birmingham', 'Test Gym3');

INSERT INTO `walls` (WID, GID, WallDescription, WallContent, image_file_name)
VALUES (1, 1, 'main wall at Warwick Sport Centre', 'so many holds...', 'wall1.jpg');

INSERT INTO `walls` (WID, GID, WallDescription, WallContent, image_file_name)
VALUES (2, 2, 'main wall at Yang Home', 'so many holds...', 'wall2.jpeg');

INSERT INTO `walls` (WID, GID, WallDescription, WallContent, image_file_name)
VALUES (3, 3, 'main wall at Tom Home', 'so many holds...', 'wall3.png');

INSERT INTO `routes` (RID, WID, creator_user_id, Difficulty, route_content, image_file_name)
VALUES (1, 1, 1, 5, '[{\"x\":10,\"y\":20},{\"x\":30,\"y\":40},{\"x\":50,\"y\":60}]', 'route1.jpg');
INSERT INTO `routes` (RID, WID, creator_user_id, Difficulty, route_content, image_file_name)
VALUES (2, 1, 1, 6, '(x1, y1, ...), (x2, y2, ...), ...', 'route1.jpg');
INSERT INTO `routes` (WID, creator_user_id, Difficulty, route_content, image_file_name)
VALUES (1, 1, 7, '(x1, y1, ...), (x2, y2, ...), ...', 'route1.jpg');
INSERT INTO `user_in_gym` (UID, GID)
VALUES (1, 2);
INSERT INTO `user_in_gym` (UID, GID)
VALUES (2, 2);
INSERT INTO `user_in_gym` (UID, GID)
VALUES (3, 3);
INSERT INTO `user_in_gym` (UID, GID)
VALUES (4, 4);

SELECT * FROM `users`;
SELECT * FROM `walls`;
SELECT * FROM `routes`;
