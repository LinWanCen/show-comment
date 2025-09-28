-- MySQL 注释来源 DDL
CREATE TABLE from_mysql_table
(
    id                INT NOT NULL AUTO_INCREMENT COMMENT '主键',
    from_mysql_column INT NOT NULL COMMENT '创建列',
    from_mysql_mod_column INT NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='表';

ALTER TABLE from_mysql_table COMMENT = '修改表';

ALTER TABLE from_mysql_table ADD from_mysql_add_column
    VARCHAR(255) COMMENT '增加列';

ALTER TABLE from_mysql_table
    MODIFY COLUMN from_mysql_mod_column
        DECIMAL(12, 2) DEFAULT 0 COMMENT '修改列';

CREATE INDEX idx1 ON from_mysql_table(from_mysql_column);
CREATE INDEX idx1 ON from_mysql_table(from_mysql_add_column, from_mysql_mod_column);