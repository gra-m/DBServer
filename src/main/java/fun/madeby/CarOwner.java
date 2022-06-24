package fun.madeby;

/**
 * Created by Gra_m on 2022 06 24
 */

public class Car implements Record{
	private String name;
	private int age;
	private String address;
	private String carPlateNumber;
	private String description;
	private Long length;

	public Car(String name, int age, String address, String carPlateNumber, String description, Long length) {
		this.name = name;
		this.age = age;
		this.address = address;
		this.carPlateNumber = carPlateNumber;
		this.description = description;
		this.length = null;
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
	public Record populateOwnRecordLength(Record record) {
		this.length = (long) (4 +  // name bytes
						name.length() +
						4 + //age
						4 +
						address.length() +
						4 +
						carPlateNumber.length() +
						4 +
						description.length());

		return this;
	}
}
