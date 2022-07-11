package fun.madeby.util;

import fun.madeby.DBRecord;

/**
 * Created by Gra_m on 2022 06 30
 */

public interface DebugInfo {
	DBRecord getDbRecord();
	boolean isDeleted();
	boolean isTemporary();

}
