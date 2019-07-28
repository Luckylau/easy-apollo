# Create Database
# ------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS ApolloConfigDB
  DEFAULT CHARACTER SET = utf8mb4;

Use ApolloConfigDB;

# Dump of table app
# ------------------------------------------------------------

DROP TABLE IF EXISTS `App`;

CREATE TABLE `App` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `AppId`                     varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'AppID',
  `Name`                      varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT '应用名',
  `OrgId`                     varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '部门Id',
  `OrgName`                   varchar(64)      NOT NULL DEFAULT 'default'
  COMMENT '部门名字',
  `OwnerName`                 varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'ownerName',
  `OwnerEmail`                varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'ownerEmail',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `AppId` (`AppId`(191)),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `IX_Name` (`Name`(191))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='应用表';

# Dump of table appnamespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `AppNamespace`;

CREATE TABLE `AppNamespace` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增主键',
  `Name`                      varchar(32)      NOT NULL DEFAULT ''
  COMMENT 'namespace名字，注意，需要全局唯一',
  `AppId`                     varchar(32)      NOT NULL DEFAULT ''
  COMMENT 'app id',
  `Format`                    varchar(32)      NOT NULL DEFAULT 'properties'
  COMMENT 'namespace的format类型',
  `IsPublic`                  bit(1)           NOT NULL DEFAULT b'0'
  COMMENT 'namespace是否为公共',
  `Comment`                   varchar(64)      NOT NULL DEFAULT ''
  COMMENT '注释',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT ''
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `IX_AppId` (`AppId`),
  KEY `Name_AppId` (`Name`, `AppId`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='应用namespace定义';

# Dump of table audit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Audit`;

CREATE TABLE `Audit` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `EntityName`                varchar(50)      NOT NULL DEFAULT 'default'
  COMMENT '表名',
  `EntityId`                  int(10) unsigned          DEFAULT NULL
  COMMENT '记录ID',
  `OpName`                    varchar(50)      NOT NULL DEFAULT 'default'
  COMMENT '操作类型',
  `Comment`                   varchar(500)              DEFAULT NULL
  COMMENT '备注',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='日志审计表';

# Dump of table cluster
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Cluster`;

CREATE TABLE `Cluster` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增主键',
  `Name`                      varchar(32)      NOT NULL DEFAULT ''
  COMMENT '集群名字',
  `AppId`                     varchar(32)      NOT NULL DEFAULT ''
  COMMENT 'App id',
  `ParentClusterId`           int(10) unsigned NOT NULL DEFAULT '0'
  COMMENT '父cluster',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT ''
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `IX_AppId_Name` (`AppId`, `Name`),
  KEY `IX_ParentClusterId` (`ParentClusterId`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='集群';

# Dump of table commit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Commit`;

CREATE TABLE `Commit` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `ChangeSets`                longtext         NOT NULL
  COMMENT '修改变更集',
  `AppId`                     varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'AppID',
  `ClusterName`               varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'ClusterName',
  `NamespaceName`             varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'namespaceName',
  `Comment`                   varchar(500)              DEFAULT NULL
  COMMENT '备注',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `AppId` (`AppId`(191)),
  KEY `ClusterName` (`ClusterName`(191)),
  KEY `NamespaceName` (`NamespaceName`(191))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='commit 历史表';

# Dump of table grayreleaserule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `GrayReleaseRule`;

CREATE TABLE `GrayReleaseRule` (
  `Id`                        int(11) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `AppId`                     varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'AppID',
  `ClusterName`               varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'Cluster Name',
  `NamespaceName`             varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'Namespace Name',
  `BranchName`                varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'branch name',
  `Rules`                     varchar(16000)            DEFAULT '[]'
  COMMENT '灰度规则',
  `ReleaseId`                 int(11) unsigned NOT NULL DEFAULT '0'
  COMMENT '灰度对应的release',
  `BranchStatus`              tinyint(2)                DEFAULT '1'
  COMMENT '灰度分支状态: 0:删除分支,1:正在使用的规则 2：全量发布',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `IX_Namespace` (`AppId`, `ClusterName`, `NamespaceName`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='灰度规则表';

# Dump of table instance
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Instance`;

CREATE TABLE `Instance` (
  `Id`                     int(11) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增Id',
  `AppId`                  varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'AppID',
  `ClusterName`            varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'ClusterName',
  `DataCenter`             varchar(64)      NOT NULL DEFAULT 'default'
  COMMENT 'Data Center Name',
  `Ip`                     varchar(32)      NOT NULL DEFAULT ''
  COMMENT 'instance ip',
  `DataChange_CreatedTime` timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `IX_UNIQUE_KEY` (`AppId`, `ClusterName`, `Ip`, `DataCenter`),
  KEY `IX_IP` (`Ip`),
  KEY `IX_DataChange_LastTime` (`DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='使用配置的应用实例';

# Dump of table instanceconfig
# ------------------------------------------------------------

DROP TABLE IF EXISTS `InstanceConfig`;

CREATE TABLE `InstanceConfig` (
  `Id`                     int(11) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增Id',
  `InstanceId`             int(11) unsigned          DEFAULT NULL
  COMMENT 'Instance Id',
  `ConfigAppId`            varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'Config App Id',
  `ConfigClusterName`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'Config Cluster Name',
  `ConfigNamespaceName`    varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT 'Config Namespace Name',
  `ReleaseKey`             varchar(64)      NOT NULL DEFAULT ''
  COMMENT '发布的Key',
  `ReleaseDeliveryTime`    timestamp        NULL     DEFAULT NULL
  COMMENT '配置获取时间',
  `DataChange_CreatedTime` timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `IX_UNIQUE_KEY` (`InstanceId`, `ConfigAppId`, `ConfigNamespaceName`),
  KEY `IX_ReleaseKey` (`ReleaseKey`),
  KEY `IX_DataChange_LastTime` (`DataChange_LastTime`),
  KEY `IX_Valid_Namespace` (`ConfigAppId`, `ConfigClusterName`, `ConfigNamespaceName`, `DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='应用实例的配置信息';

# Dump of table item
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Item`;

CREATE TABLE `Item` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增Id',
  `NamespaceId`               int(10) unsigned NOT NULL DEFAULT '0'
  COMMENT '集群NamespaceId',
  `Key`                       varchar(128)     NOT NULL DEFAULT 'default'
  COMMENT '配置项Key',
  `Value`                     longtext         NOT NULL
  COMMENT '配置项值',
  `Comment`                   varchar(1024)             DEFAULT ''
  COMMENT '注释',
  `LineNum`                   int(10) unsigned          DEFAULT '0'
  COMMENT '行号',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `IX_GroupId` (`NamespaceId`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='配置项目';

# Dump of table namespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Namespace`;

CREATE TABLE `Namespace` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增主键',
  `AppId`                     varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'AppID',
  `ClusterName`               varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'Cluster Name',
  `NamespaceName`             varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'Namespace Name',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `AppId_ClusterName_NamespaceName` (`AppId`(191), `ClusterName`(191), `NamespaceName`(191)),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `IX_NamespaceName` (`NamespaceName`(191))
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='命名空间';

# Dump of table namespacelock
# ------------------------------------------------------------

DROP TABLE IF EXISTS `NamespaceLock`;

CREATE TABLE `NamespaceLock` (
  `Id`                        int(11) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增id',
  `NamespaceId`               int(10) unsigned NOT NULL DEFAULT '0'
  COMMENT '集群NamespaceId',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT 'default'
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  `IsDeleted`                 bit(1)                    DEFAULT b'0'
  COMMENT '软删除',
  PRIMARY KEY (`Id`),
  UNIQUE KEY `IX_NamespaceId` (`NamespaceId`),
  KEY `DataChange_LastTime` (`DataChange_LastTime`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='namespace的编辑锁';

# Dump of table release
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Release`;

CREATE TABLE `Release` (
  `Id`                        int(10) unsigned NOT NULL AUTO_INCREMENT
  COMMENT '自增主键',
  `ReleaseKey`                varchar(64)      NOT NULL DEFAULT ''
  COMMENT '发布的Key',
  `Name`                      varchar(64)      NOT NULL DEFAULT 'default'
  COMMENT '发布名字',
  `Comment`                   varchar(256)              DEFAULT NULL
  COMMENT '发布说明',
  `AppId`                     varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'AppID',
  `ClusterName`               varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'ClusterName',
  `NamespaceName`             varchar(500)     NOT NULL DEFAULT 'default'
  COMMENT 'namespaceName',
  `Configurations`            longtext         NOT NULL
  COMMENT '发布配置',
  `IsAbandoned`               bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '是否废弃',
  `IsDeleted`                 bit(1)           NOT NULL DEFAULT b'0'
  COMMENT '1: deleted, 0: normal',
  `DataChange_CreatedBy`      varchar(32)      NOT NULL DEFAULT 'default'
  COMMENT '创建人邮箱前缀',
  `DataChange_CreatedTime`    timestamp        NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `DataChange_LastModifiedBy` varchar(32)               DEFAULT ''
  COMMENT '最后修改人邮箱前缀',
  `DataChange_LastTime`       timestamp        NULL     DEFAULT CURRENT_TIMESTAMP
  ON UPDATE CURRENT_TIMESTAMP
  COMMENT '最后修改时间',
  PRIMARY KEY (`Id`),
  KEY `AppId_ClusterName_GroupName` (`AppId`(191), `ClusterName`(191), `NamespaceName`(191)),
  KEY `DataChange_LastTime` (`DataChange_LastTime`),
  KEY `IX_ReleaseKey` (`ReleaseKey`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='发布';
