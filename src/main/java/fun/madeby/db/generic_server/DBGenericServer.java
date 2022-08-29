package fun.madeby.db.generic_server;

import fun.madeby.db.DbTable;
import fun.madeby.db.Table;
import fun.madeby.exceptions.DBException;
import fun.madeby.generic.GenericIndexPool;
import fun.madeby.generic.Schema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gra_m on 2022 07 30
 */

public class DBGenericServer implements DBGeneric  {
	Map<String, Table> tablePool = new HashMap<>();
	GenericIndexPool indexPool = new GenericIndexPool();
	Table currentlyInUse = null;



	/**
	 * @param tableName
	 * @param tableSchema
	 * @param aClass
	 * @return
	 */
	@Override
	public Table useTable(String tableName, String tableSchema, Class aClass) throws DBException, FileNotFoundException
		{
			String fileName = tableName + ".db";
			if (tableNotAlreadyLoaded(tableName)) {
				Table newTable = new DbTable(fileName, tableSchema, aClass);
				this.tablePool.put(fileName, newTable);
			}

			this.currentlyInUse = tablePool.get(fileName);

			return currentlyInUse;

		}

	@Override
	public Boolean dropCurrentTable() throws IOException, DBException
		{

			String currentTableName = currentlyInUse.getTableName();
			this.currentlyInUse.close();
			// todo remove index from index pool
			this.indexPool.deleteIndex(currentTableName);
			this.tablePool.remove(currentTableName);
			this.currentlyInUse = null;
			File file = new File(currentTableName);
			if(!file.exists())
				throw new DBException("Table " + currentTableName + " could not be deleted");
			return file.delete();
		}

	private boolean tableNotAlreadyLoaded(String tableName) {
		return !tablePool.containsKey(tableName);
	}

	@Override
	public void close() throws IOException
		{
			try {
				this.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
}
