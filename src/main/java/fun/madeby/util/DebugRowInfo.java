package fun.madeby.util;

import fun.madeby.DBRecord;

/**
 * Created by Gra_m on 2022 06 30
 */

public final class DebugRowInfo implements DebugInfo {
	private Object object;
	private boolean isTemporary;
	private boolean isDeleted;

	public DebugRowInfo(){}

	public DebugRowInfo(Object object, boolean isTemporary, boolean isDeleted){
		this.object = object;
		this.isTemporary = isTemporary;
		this.isDeleted = isDeleted;
	}

	public Object getDbRecord() {
		return object;
	}

	public boolean isTemporary() {
		return isTemporary;
	}

	@Override
	public String toString() {
		return  "\n" + object.getClass() + " /t " + isTemporary + " /d " + isDeleted;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

}
