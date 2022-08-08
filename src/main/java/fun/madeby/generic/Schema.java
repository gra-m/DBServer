package fun.madeby.generic;

import java.util.LinkedList;

/**
 * Created by Gra_m on 2022 08 01
 */

public class Schema {
	public String version;
	public String indexBy;
	public LinkedList<SchemaField> schemaFields;

	@Override
	public String toString() {
		return "Schema{" +
				"version='" + version + '\'' +
				", schemaFields=" + schemaFields +
				'}';
	}
}
