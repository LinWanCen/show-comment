-- Oracle 注释来源 DDL
COMMENT ON TABLE from_oracle_table IS '表';
COMMENT ON COLUMN from_oracle_table.id IS '主键';
COMMENT ON COLUMN from_oracle_table.name IS '名字';
COMMENT ON COLUMN from_oracle_table.from_oracle_column IS '列';
COMMENT ON TABLE schema1.from_oracle_table IS '带schema表';
COMMENT ON COLUMN schema1.from_oracle_table.from_oracle_column IS '带schema列';