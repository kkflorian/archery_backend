-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(45) NOT NULL,
  `firstName` VARCHAR(45) NOT NULL,
  `lastName` VARCHAR(45) NOT NULL,
  `passwordHash` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE UNIQUE INDEX `username_UNIQUE` ON `user` (`username` ASC) ;


-- -----------------------------------------------------
-- Table `parkour`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `parkour` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(75) NOT NULL,
  `countAnimals` INT NOT NULL,
  `countryCode` VARCHAR(3) NOT NULL,
  `city` VARCHAR(75) NOT NULL,
  `street` VARCHAR(75) NOT NULL,
  `zip` VARCHAR(10) NOT NULL,
  `latitude` DOUBLE NULL,
  `longitude` DOUBLE NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `event`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `event` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `parkourId` INT NOT NULL,
  `timestamp` TIMESTAMP NOT NULL,
  `gamemodeId` INT NOT NULL,
  `userIdCreator` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `event_parkour`
    FOREIGN KEY (`parkourId`)
    REFERENCES `parkour` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_user`
    FOREIGN KEY (`userIdCreator`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `event_gamemode`
    FOREIGN KEY (`gamemodeId`)
    REFERENCES `gamemode` (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE INDEX `id_idx` ON `event` (`parkourId` ASC) ;

CREATE INDEX `id_idx1` ON `event` (`userIdCreator` ASC) ;


-- -----------------------------------------------------
-- Table `eventMember`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `eventMember` (
  `eventId` INT NOT NULL,
  `userId` INT NOT NULL,
  PRIMARY KEY (`eventId`, `userId`),
  CONSTRAINT `eventMember_event`
    FOREIGN KEY (`eventId`)
    REFERENCES `event` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `eventMember_user`
    FOREIGN KEY (`userId`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE INDEX `id_idx` ON `eventMember` (`userId` ASC) ;


-- -----------------------------------------------------
-- Table `shot`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `shot` (
  `id` INT NOT NULL,
  `eventId` INT NOT NULL,
  `userId` INT NOT NULL,
  `animalNumber` INT NOT NULL,
  `shotNumber` INT NOT NULL,
  `points` INT NOT NULL,
  `finished` INT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  CONSTRAINT `shot_event`
    FOREIGN KEY (`eventId`)
    REFERENCES `event` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `shot_user`
    FOREIGN KEY (`userId`)
    REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;

CREATE INDEX `id_idx` ON `shot` (`eventId` ASC) ;

CREATE INDEX `id_idx1` ON `shot` (`userId` ASC) ;


-- -----------------------------------------------------
-- Table `userSession`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `userSession` (
  `sessionId` VARCHAR(32) NOT NULL,
  `userId` INT NOT NULL,
  `expiryDate` TIMESTAMP NOT NULL,
  PRIMARY KEY (`sessionId`),
  CONSTRAINT `userSession_user`
    FOREIGN KEY (`userId`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

CREATE INDEX `userSession_user_idx` ON `userSession` (`userId` ASC) ;

-- -----------------------------------------------------
-- Table `gamemode`
-- -----------------------------------------------------

create table if not exists gamemode
(
    id int auto_increment,
    gamemode varchar(45) not null,
    constraint gamemode_pk
        primary key (id)
);

create unique index gamemode_gamemode_uindex
    on gamemode (gamemode);

alter table event add column timestampEnd timestamp;
alter table shot modify column id int NOT NULL AUTO_INCREMENT;
