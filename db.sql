DROP TABLE IF EXISTS `HTML`;
DROP TABLE IF EXISTS `XSLT`;
DROP TABLE IF EXISTS `XSD`;
DROP TABLE IF EXISTS `XML`;

CREATE TABLE `HTML` (
  `uuid` char(36) NOT NULL,
  `content` text,

  PRIMARY KEY (`uuid`)
);

CREATE TABLE `XSD` (
  `uuid` char(36) NOT NULL,
  `content` text,

  PRIMARY KEY (`uuid`)
);

CREATE TABLE `XML` (
  `uuid` char(36) NOT NULL,
  `content` text,

  PRIMARY KEY (`uuid`)
);

CREATE TABLE `XSLT` (
  `uuid` char(36) NOT NULL,
  `content` text,
  `xsd`  char(36) NOT NULL,

  FOREIGN KEY(`xsd`) REFERENCES `XSD`(`uuid`) ON DELETE CASCADE,
  PRIMARY KEY (`uuid`)
);

