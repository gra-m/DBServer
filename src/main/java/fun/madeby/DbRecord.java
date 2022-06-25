package fun.madeby;

/**
 * Created by Gra_m on 2022 06 24
 */

public interface DbRecord {
	DbRecord populateOwnRecordLength(DbRecord dbRecord);
	Long getLength();
	String getName();
	int getAge();
	String getAddress();
	String getCarPlateNumber();
	String getDescription();

}
