package fun.madeby.generic;

import fun.madeby.exceptions.DBException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gra_m on 2022 08 29
 * This class holds references for all GenericIndexObjects.
 * When a new table is opened, then a new GI is created and stored here.
 */

public final class GenericIndexPool {
	// stores tablename and index
	private Map<String, GenericIndex> indexStore;

	public GenericIndexPool() {
		this.indexStore = Collections.synchronizedMap(new HashMap<>());
	}

	public GenericIndex createIndex(final String tableName, final Schema schema) throws DBException
		{
		GenericIndex genIndex = new GenericIndex(schema);
		indexStore.put(tableName, genIndex);
		return genIndex;
	}

	public void deleteIndex(final String tableName) {
		GenericIndex _index = indexStore.remove(tableName);
		_index.clear();
	}
}
