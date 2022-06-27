package fun.madeby;

import java.util.HashMap;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class Index {

	static{indexInstance = new Index();}
	private static final Index indexInstance;
	private HashMap<Long, Long> mapRowIndexBytePosition;
	private Long totalNumberOfRows = 0L;

	private Index() {
		this.mapRowIndexBytePosition = new HashMap<>();
	}

	public static Index getInstance() {
		return indexInstance;
	}

	public void add(Long bytePosition) {
		this.mapRowIndexBytePosition.put(this.totalNumberOfRows++, bytePosition);
	}

	public void remove(Long rowIndex) {
		this.mapRowIndexBytePosition.remove(rowIndex);
		this.totalNumberOfRows--;
	}

	public Long getRowsBytePosition(Long rowNumber) {
		return this.mapRowIndexBytePosition.getOrDefault(rowNumber, -1L);
	}

	public Long getTotalNumberOfRows() {
		return this.totalNumberOfRows;
	}
}
