package fun.madeby;

/**
 * Created by Gra_m on 2022 06 24
 */

public interface DbRecord {

	int INTEGER_LENGTH_IN_BYTES = 4;
	DbRecord populateOwnRecordLength(DbRecord dbRecord);
	Long getLength();
	String getName();
	int getAge();
	String getAddress();
	String getCarPlateNumber();
	String getDescription();

}
