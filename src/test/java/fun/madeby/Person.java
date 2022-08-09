package fun.madeby;

import com.google.gson.JsonObject;

/**
 * Created by Gra_m on 2022 06 24
 */

public class Person {
	public String name;
	public int age;
	public String address;
	public String pet;
	public String description;

	@Override
	public String toString() {
		return String.format("Name: %s, Age: %s, Address: %s, reg#: %s, Description: %s",
				name, age, address, pet, description);
	}

	public Person(String name, int age, String address, String pet, String description) {
		this.name = name;
		this.age = age;
		this.address = address;
		this.pet = pet;
		this.description = description;
	}

	public Person() {
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

	public String getPet() {
		return pet;
	}

	public String getDescription() {
		return description;
	}

	public String toJSON() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("name", name);
		jsonObject.addProperty("age", age);
		jsonObject.addProperty("address", address);
		jsonObject.addProperty("pet", pet);
		jsonObject.addProperty("description", description);

		return jsonObject.toString();
	}

}
