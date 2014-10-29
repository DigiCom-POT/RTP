CREATE TABLE
moviesinfo (movie_id STRING, movie_name STRING, genre STRING)
STORED BY 'org.apache.hcatalog.hbase.HBaseHCatStorageHandler'
TBLPROPERTIES (
  'hbase.table.name' = 'moviesinfo',
  'hbase.columns.mapping' = 'd:movie_name,d:genre',
  'hcat.hbase.output.bulkMode' = 'true'
);
