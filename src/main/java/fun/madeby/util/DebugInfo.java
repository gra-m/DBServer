package fun.madeby.util;


/**
 * Created by Gra_m on 2022 06 30
 */

public interface DebugInfo {
	Object getDbRecord();
	boolean isDeleted();
	boolean isTemporary();

}
