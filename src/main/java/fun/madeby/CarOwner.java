package fun.madeby;

/**
 * Created by Gra_m on 2022 06 24
 */

public class CarOwner implements DBRecord {
	private String name;
	private int age;
	private String address;
	private String carPlateNumber;
	private String description;
	private Long length;

	@Override
	public String toString() {
		return String.format("Name: %s, Age: %s, Address: %s, reg#: %s, Description: %s",
				name, age, address, carPlateNumber, description);
	}

	public CarOwner(String name, int age, String address, String carPlateNumber, String description) {
		this.name = name;
		this.age = age;
		this.address = address;
		this.carPlateNumber = carPlateNumber;
		this.description = description;
		this.length = null;
	}

	public CarOwner() {
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return  age;
	}

	public String getAddress() {
		return address;
	}

	public String getCarPlateNumber() {
		return carPlateNumber;
	}

	public String getDescription() {
		return description;
	}

	public Long getLength() {
		return length;
	}

	@Override
	public DBRecord populateOwnRecordLength(DBRecord dbRecord) {
		this.length = (long) (INTEGER_LENGTH_IN_BYTES +  // name int bytes
						name.length() +
						INTEGER_LENGTH_IN_BYTES + // age int bytes
						INTEGER_LENGTH_IN_BYTES+ // address int bytes
						address.length() +
						INTEGER_LENGTH_IN_BYTES + //carPlate int bytes
						carPlateNumber.length() +
						INTEGER_LENGTH_IN_BYTES + //description int bytes
						description.length());

		return this;
	}
}
