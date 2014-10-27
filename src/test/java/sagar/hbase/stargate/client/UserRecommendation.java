package sagar.hbase.stargate.client;

public class UserRecommendation {
	private Row[] Row;

	public Row[] getRow() {
		return Row;
	}

	public void setRow(Row[] row) {
		Row = row;
	}
}

class Row {
	private String key;
	private Cell[] Cell;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Cell[] getCell() {
		return Cell;
	}
	public void setCell(Cell[] Cell) {
		this.Cell = Cell;
	}
}

class Cell {
	String column;
	String timestamp;
	String $;
	
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String get$() {
		return $;
	}
	public void set$(String $) {
		this.$ = $;
	}
	
}