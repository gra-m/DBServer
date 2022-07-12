package fun.madeby;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class Index {

	static{indexInstance = new Index();}
	private static final Index indexInstance;
	private ConcurrentHashMap<Long, Long> mapRowNumberBytePosition;
	private Long totalNumberOfRows = 0L;
	private ConcurrentHashMap<String, Long> mapDbRecordNameByRowNumber;



	private Index() {
		this.mapRowNumberBytePosition = new ConcurrentHashMap<>();
		this.mapDbRecordNameByRowNumber = new ConcurrentHashMap<>();
	}

	public static Index getInstance() {
		return indexInstance;
	}

	public synchronized void printNameIndex(){
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

	public synchronized void add(Long bytePosition) {
		this.mapRowNumberBytePosition.put(totalNumberOfRows, bytePosition);
		totalNumberOfRows++;
	}

	public synchronized void addNameToIndex(final String name, Long rowIndex) {
		this.mapDbRecordNameByRowNumber.put(name, rowIndex);
	}

	public boolean hasNameInIndex(final String name) {
		return this.mapDbRecordNameByRowNumber.containsKey(name);
	}

	public Long getRowNumberByName(final String name) {
		return this.mapDbRecordNameByRowNumber.getOrDefault(name, -1L);
	}

	public void remove(Long rowIndex, DBRecord existingRowNumberRecord) {
		this.mapRowNumberBytePosition.remove(rowIndex);
		this.mapDbRecordNameByRowNumber.remove(existingRowNumberRecord.getName());
		this.totalNumberOfRows--;
	}

	public void remove(Long rowIndex, String recordName) {
		this.mapRowNumberBytePosition.remove(rowIndex);
		this.mapDbRecordNameByRowNumber.remove(recordName);
		this.totalNumberOfRows--;
	}

	public Long getRowsBytePosition(Long rowNumber) {
		return this.mapRowNumberBytePosition.getOrDefault(rowNumber, -1L);
	}

	public synchronized Long getTotalNumberOfRows() {
		return this.totalNumberOfRows;
	}



	public synchronized void clear() {
		this.totalNumberOfRows = 0L;
		this.mapRowNumberBytePosition.clear();
		this.mapDbRecordNameByRowNumber.clear();
	}

	public synchronized Collection<String> getNames() {
		return this.mapDbRecordNameByRowNumber.keySet();
	}

	public void removeByFilePosition(Long position) {
		Long rowIndex;
		String rowName = null;

		if(mapRowNumberBytePosition.isEmpty()) {
			return;
		}
		rowIndex = mapRowNumberBytePosition.search(1, (k, v) -> (Objects.equals(v, position)) ? k : -1);
		if (rowIndex != -1L) {
			rowName = mapDbRecordNameByRowNumber.search(1, (k, v) -> (Objects.equals(v, rowIndex)) ? k : null);
		}

		if (rowName != null)
			remove(rowIndex, rowName);

	}
}
