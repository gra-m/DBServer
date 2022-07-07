package fun.madeby;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class Index {

	static{indexInstance = new Index();}
	private static final Index indexInstance;
	private HashMap<Long, Long> mapRowNumberBytePosition;
	private Long totalNumberOfRows = 0L;
	private HashMap<String, Long> mapDbRecordNameByRowNumber;



	private Index() {
		this.mapRowNumberBytePosition = new HashMap<>();
		this.mapDbRecordNameByRowNumber = new HashMap<>();
	}

	public static Index getInstance() {
		return indexInstance;
	}

	public void printNameIndex(){
		if(this.mapDbRecordNameByRowNumber.size() == 0)
			System.out.println("'Vicar..' Teacup rattles on saucer. 'I do believe it's empty!'");
		this.mapDbRecordNameByRowNumber.entrySet().forEach(entry -> {
			System.out.println("Index().PrintNameIndex(): " + entry.getKey() + " " + entry.getValue());
		});
	}

	public int getMapRowNumberBytePositionSize() {
		return this.mapRowNumberBytePosition.size();
	}

	public int getMapDbRecordNameBytePositionSize() {
		return this.mapDbRecordNameByRowNumber.size();
	}

	public void resetTotalNumberOfRows() {
		this.totalNumberOfRows = 0L;
	}

	public void add(Long bytePosition) {
		this.mapRowNumberBytePosition.put(totalNumberOfRows, bytePosition);
		totalNumberOfRows++;
	}

	public void addNameToIndex(final String name, Long rowIndex) {
		this.mapDbRecordNameByRowNumber.put(name, rowIndex);
	}

	public boolean hasNameInIndex(final String name) {
		return this.mapDbRecordNameByRowNumber.containsKey(name);
	}

	public Long getRowNumberByName(final String name) {
		return this.mapDbRecordNameByRowNumber.getOrDefault(name, -1L);
	}

	public Long getUndeletedRowNumberByName(final String name) {
		// better to create active Name index but all of this is probably coming.
		return null;

	}

	public void remove(Long rowIndex, DBRecord existingRowNumberRecord) {
		this.mapRowNumberBytePosition.remove(rowIndex);
		this.mapDbRecordNameByRowNumber.remove(existingRowNumberRecord.getName());
		this.totalNumberOfRows--;
	}

	public Long getRowsBytePosition(Long rowNumber) {
		return this.mapRowNumberBytePosition.getOrDefault(rowNumber, -1L);
	}

	public Long getTotalNumberOfRows() {
		return this.totalNumberOfRows;
	}



	public void clear() {
		this.totalNumberOfRows = 0L;
		this.mapRowNumberBytePosition.clear();
		this.mapDbRecordNameByRowNumber.clear();
	}

	public Collection<String> getNames() {
		return this.mapDbRecordNameByRowNumber.keySet();
	}
}
