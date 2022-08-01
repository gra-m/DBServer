package fun.madeby.generic;

/**
 * Created by Gra_m on 2022 08 01
 */

public class SchemaField {
	public String fieldName;
	public String fieldType;

	@Override
	public String toString(){
		return String.format("fieldName: %s, fieldType: %s", fieldName, fieldType);
	}

}
