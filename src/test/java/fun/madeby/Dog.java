package fun.madeby;

/**
 * Created by Gra_m on 2022 08 02
 */

public class Dog {
	public String pName;
	public int age;
	public String owner;

	public Dog() {
	}

	public Dog(String pName, int age, String owner) {
		this.pName = pName;
		this.age = age;
		this.owner = owner;
	}

	public String getpName() {
		return pName;
	}

	public int getAge() {
		return age;
	}

	public String getOwner() {
		return owner;
	}

	@Override
	public String toString() {
		return "Dog{" +
				"pName='" + pName + '\'' +
				", age=" + age +
				", owner='" + owner + '\'' +
				'}';
	}
}
