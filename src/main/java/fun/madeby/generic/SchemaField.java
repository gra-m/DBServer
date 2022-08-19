package fun.madeby.generic;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Created by Gra_m on 2022 08 01
 */

@SuppressFBWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class SchemaField {
	public String fieldName;
	public String fieldType;

	@Override
	public String toString(){
		return String.format("fieldName: %s, fieldType: %s", fieldName, fieldType);
	}

}
