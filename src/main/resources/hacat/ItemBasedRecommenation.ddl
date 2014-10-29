CREATE TABLE
movmov (user_id STRING, recommendation STRING)
STORED BY 'org.apache.hcatalog.hbase.HBaseHCatStorageHandler'
TBLPROPERTIES (
  'hbase.table.name' = 'movmovrec',
  'hbase.columns.mapping' = 'd:recommendation',
  'hcat.hbase.output.bulkMode' = 'true'
);
