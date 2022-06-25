package fun.madeby;

/**
 * Created by Gra_m on 2022 06 24
 */

public class CarOwner implements DbRecord {
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
	public DbRecord populateOwnRecordLength(DbRecord dbRecord) {
		this.length = (long) (4 +  // name int bytes
						name.length() +
						4 + // age int bytes
						4 + // address int bytes
						address.length() +
						4 + //carPlate int bytes
						carPlateNumber.length() +
						4 + //description int bytes
						description.length());

		return this;
	}
}
