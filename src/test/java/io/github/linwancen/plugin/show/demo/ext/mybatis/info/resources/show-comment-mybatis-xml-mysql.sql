-- table-show-comment-mybatis.xml.doc.tsv
SELECT t.TABLE_NAME
     , CONCAT(
        REPLACE(REPLACE(t.TABLE_COMMENT, CHAR(10), ' '), CHAR(13), ' '), ' - ',
        ROUND(TABLE_ROWS / 10000, 0), 'w - ',
        GROUP_CONCAT(COLUMN_NAMES SEPARATOR ' | ')) AS INFO
     , t.TABLE_SCHEMA
FROM (SELECT c.TABLE_SCHEMA,
             c.TABLE_NAME,
             c.INDEX_NAME,
             CONCAT(GROUP_CONCAT(c.COLUMN_NAME),
                    CASE MAX(c.NON_UNIQUE) WHEN 0 THEN ' U' ELSE '' END) AS COLUMN_NAMES
      FROM information_schema.STATISTICS c
      WHERE c.TABLE_SCHEMA NOT IN ('mysql', 'information_schema', 'performance_schema', 'sys')
      GROUP BY c.TABLE_SCHEMA, c.TABLE_NAME, c.INDEX_NAME) c
         JOIN information_schema.`TABLES` t ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME
GROUP BY t.TABLE_SCHEMA, t.TABLE_NAME, t.TABLE_COMMENT, TABLE_ROWS;

-- column-show-comment-mybatis.xml.doc.tsv
SELECT c.COLUMN_NAME,
       CONCAT(REPLACE(REPLACE(c.COLUMN_COMMENT, CHAR(10), ' '), CHAR(13), ' '),
              CASE COUNT(s.INDEX_NAME) WHEN 0 THEN '' ELSE ' √' END) AS INFO,
       c.TABLE_NAME,
       c.TABLE_SCHEMA
FROM information_schema.`COLUMNS` c
         JOIN information_schema.`TABLES` t ON t.TABLE_SCHEMA = c.TABLE_SCHEMA AND t.TABLE_NAME = c.TABLE_NAME
         LEFT JOIN information_schema.`STATISTICS` s
                   ON s.TABLE_SCHEMA = c.TABLE_SCHEMA AND s.TABLE_NAME = c.TABLE_NAME AND s.COLUMN_NAME = c.COLUMN_NAME
WHERE c.COLUMN_COMMENT != ''
  AND c.TABLE_SCHEMA NOT IN ('mysql', 'information_schema', 'performance_schema', 'sys')
GROUP BY c.COLUMN_NAME, c.COLUMN_COMMENT, c.TABLE_NAME, c.TABLE_SCHEMA;