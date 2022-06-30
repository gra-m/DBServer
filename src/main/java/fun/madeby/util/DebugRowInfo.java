package fun.madeby.util;

import fun.madeby.DBRecord;

/**
 * Created by Gra_m on 2022 06 30
 */

public final class DebugRowInfo implements DebugInfo {
	private DBRecord dbRecord;
	private boolean isDeleted;

	public DebugRowInfo(){};

	public DebugRowInfo(DBRecord dbRecord, boolean isDeleted){
		this.dbRecord = dbRecord;
		this.isDeleted = isDeleted;
	}

	public DBRecord getDbRecord() {
		return dbRecord;
	}

	public boolean isDeleted() {
		return isDeleted;
	}
}
