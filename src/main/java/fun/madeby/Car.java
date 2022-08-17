package fun.madeby;

import com.google.gson.JsonObject;
import fun.madeby.util.JSONRep;

/**
 * Created by Gra_m on 2022 06 24
 */

@SuppressWarnings("CanBeFinal")
public class Car implements DBRecord, JSONRep {
	private final String name;
	private final int age;
	private final String address;
	private final String carPlateNumber;
	private final String description;
	private Long length;

	public Car(String name, int age, String address, String carPlateNumber, String description)
		{
			this.name = name;
			this.age = age;
			this.address = address;
			this.carPlateNumber = carPlateNumber;
			this.description = description;
			this.length = null;
		}

	@Override
	public String toJSON()
		{
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", name);
			jsonObject.addProperty("age", age);
			jsonObject.addProperty("address", address);
			jsonObject.addProperty("carPlateNumber", carPlateNumber);
			jsonObject.addProperty("description", description);

			return jsonObject.toString();
		}

	@Override
	public DBRecord populateOwnRecordLength(DBRecord object)
		{
			this.length = (long) (INTEGER_LENGTH_IN_BYTES +  // name bytes
				  name.length() +
				  INTEGER_LENGTH_IN_BYTES + //age
				  INTEGER_LENGTH_IN_BYTES +
				  address.length() +
				  INTEGER_LENGTH_IN_BYTES +
				  carPlateNumber.length() +
				  INTEGER_LENGTH_IN_BYTES +
				  description.length());

			return this;
		}

	public Long getLength()
		{
			return length;
		}

	public String getName()
		{
			return name;
		}

	public int getAge()
		{
			return age;
		}

	public String getAddress()
		{
			return address;
		}

	public String getCarPlateNumber()
		{
			return carPlateNumber;
		}

	public String getDescription()
		{
			return description;
		}
}
