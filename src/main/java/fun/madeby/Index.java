package fun.madeby;

import java.util.HashMap;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class Index {

	static{indexInstance = new Index();}
	private static final Index indexInstance;
	private HashMap<Long, Long> mapRowIndexBytePosition;
	private Long nextWriteRowIndex = 0L;

	private Index() {
		this.mapRowIndexBytePosition = new HashMap<>();
	}

	public static Index getInstance() {
		return indexInstance;
	}

	public void add(Long bytePosition) {
		this.mapRowIndexBytePosition.put(this.nextWriteRowIndex++, bytePosition);
	}

	public void remove(Long rowIndex) {
		this.mapRowIndexBytePosition.remove(rowIndex);
		this.nextWriteRowIndex--;
	}

	public Long getNextWriteRowIndex() {
		return this.nextWriteRowIndex;
	}
}
