package fun.madeby;

/**
 * Created by Gra_m on 2022 06 24
 */

public interface DBRecord {

	int INTEGER_LENGTH_IN_BYTES = 4;
	DBRecord populateOwnRecordLength(DBRecord object);
	Long getLength();
	String getName();
	int getAge();
	String getAddress();
	String getCarPlateNumber();
	String getDescription();
	String toJSON();
}
