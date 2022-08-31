package fun.madeby.db.generic_server;

import fun.madeby.db.DbTable;
import fun.madeby.db.Table;
import fun.madeby.exceptions.DBException;
import fun.madeby.generic.GenericIndex;
import fun.madeby.generic.GenericIndexPool;
import fun.madeby.util.LoggerSetUp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Gra_m on 2022 07 30
 */

public class DBGenericServer implements DBGeneric {
	private Map<String, Table> tablePool = new HashMap<>();
	private GenericIndexPool indexPool = new GenericIndexPool();
	private Table currentlyInUse = null;
	private Logger LOGGER;

	{
		try {
			LOGGER = LoggerSetUp.setUpLogger("DbGenericServer");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param tableName
	 * @param tableSchema
	 * @param aClass
	 * @return
	 */
	@Override
	public Table useTable(String tableName, String tableSchema, Class aClass) throws DBException, FileNotFoundException
		{
			LOGGER.severe("@DBGenericServer/useTable(tableName, tableSchema, tableClass");
			DbTable _reinitCurrentTable;


			String fileName = tableName + ".db";
			if (tableNotAlreadyLoaded(fileName)) {
				Table newTable = new DbTable(fileName, tableSchema, aClass, indexPool);
				this.tablePool.put(fileName, newTable);
			} else {
				// Check if index pool already contains filename, if it does reinstate index (reinit)
				GenericIndex existingIndex = this.indexPool.getIndex(tableName);
				if (existingIndex != null) {
					_reinitCurrentTable = (DbTable) currentlyInUse;
					_reinitCurrentTable.refreshTableIndex_WasGenericIndex();
					// unecessary currentlyInUse =  _reinitCurrentTable;
				}
			}

			this.currentlyInUse = tablePool.get(fileName);
			LOGGER.finest("@DBGenericServer/useTable: table index is: " + indexPool.getIndex(fileName));


			return currentlyInUse;

		}

	@Override
	public Boolean dropCurrentTable() throws IOException, DBException
		{

			String currentTableName = currentlyInUse.getTableName();
			this.currentlyInUse.close();
			this.indexPool.deleteIndex(currentTableName);
			this.tablePool.remove(currentTableName);
			this.currentlyInUse = null;
			File file = new File(currentTableName);
			if (!file.exists())
				throw new DBException("Table " + currentTableName + " could not be deleted");
			return file.delete();
		}

	@Override
	public void close() throws IOException
		{
			// left in for closeable
		}

	@Override
	public void closeCurrentTable() throws IOException, DBException
		{
			this.currentlyInUse.close();
			this.currentlyInUse = null;
		}

	@Override
	public void suspendCurrentTable()
		{
			this.currentlyInUse.suspend();
			this.currentlyInUse = null;
		}


	private boolean tableNotAlreadyLoaded(String fileName)
		{
			return !tablePool.containsKey(fileName);
		}

	public GenericIndex getIndex(final String tableName)
		{
			return indexPool.getIndex(tableName);
		}


}
