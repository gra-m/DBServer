package fun.madeby;

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

	public void add(Long bytePosition) {
		this.mapRowNumberBytePosition.put(this.totalNumberOfRows++, bytePosition);
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

	public void remove(Long rowIndex) {
		this.mapRowNumberBytePosition.remove(rowIndex);
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
}
