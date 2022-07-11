package fun.madeby.util;

import fun.madeby.DBRecord;

/**
 * Created by Gra_m on 2022 06 30
 */

public final class DebugRowInfo implements DebugInfo {
	private DBRecord dbRecord;
	private boolean isTemporary;
	private boolean isDeleted;

	public DebugRowInfo(){}

	public DebugRowInfo(DBRecord dbRecord, boolean isTemporary, boolean isDeleted){
		this.dbRecord = dbRecord;
		this.isTemporary = isTemporary;
		this.isDeleted = isDeleted;
	}

	public DBRecord getDbRecord() {
		return dbRecord;
	}

	public boolean isTemporary() {
		return isTemporary;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

}
