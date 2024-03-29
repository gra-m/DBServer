package fun.madeby;

import com.google.gson.JsonObject;
import fun.madeby.util.JSONRep;

/**
 * Created by Gra_m on 2022 06 24
 */

public class CarOwner implements DBRecord, JSONRep {
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

	@Override
	public String toJSON() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", name);
		jsonObject.addProperty("age", age);
		jsonObject.addProperty("address", address);
		jsonObject.addProperty("carPlateNumber", carPlateNumber);
		jsonObject.addProperty("description", description);

		return jsonObject.toString();
	}

	public Long getLength() {
		if (this.length == null) {
			this.populateOwnRecordLength(this);
		}
		return this.length;
	}

	@Override
	public DBRecord populateOwnRecordLength(DBRecord object) {
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
