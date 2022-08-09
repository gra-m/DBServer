package fun.madeby.generic;

import fun.madeby.exceptions.DBException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gra_m on 2022 06 27
 */

public final class GenericIndex {

	static{
		genericIndexInstance = new GenericIndex();}

	private static final GenericIndex genericIndexInstance;
	private Schema schema;
	private ConcurrentHashMap<Long, Long> mapRowNumberBytePosition;
	private Long totalNumberOfRows = 0L;
	// Updated String indexedBy[name], CHM<String value, Long rowNum>
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> indexByWithMappedValueAndRowNumber;



	private GenericIndex() {
		this.mapRowNumberBytePosition = new ConcurrentHashMap<>();
		this.indexByWithMappedValueAndRowNumber = new ConcurrentHashMap<>();
	}

	public static GenericIndex getInstance() {
		return genericIndexInstance;
	}

	public void initialiseGenericIndexSchema(Schema schema) {
		this.schema = schema;
		String indexBy = schema.indexBy;

		try {
			if (indexBy == null) {
				throw new DBException("@GenericIndex/initialiseIndexSchema(Schema) -> Schema's indexBy field found null");
			}
		}catch (DBException e) {
			e.printStackTrace();
		}
	}

	//printIndexByValues
	public synchronized void printNameIndex(){
		ConcurrentHashMap<String, Long> retrievedValuesToPrint = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);
		if(retrievedValuesToPrint.size() == 0)
			System.out.println("There is nothing in that Indexes ConcurrentHashMap.");
		retrievedValuesToPrint.forEach((key, value) -> System.out.println("@GenericIndex/PrintNameIndex: Lists all values and row Numbers within schema indexBy namespace : " + key + " " + value));
	}


	public synchronized void printIndexByNames(){
		if(this.indexByWithMappedValueAndRowNumber.size() == 0)
			System.out.println("There are no keys in indexByWithMappedValueAndRowNumber");
		this.indexByWithMappedValueAndRowNumber.forEach((key, value) -> System.out.println("@GenericIndex/printIndexByNames() Lists All keys in indexByWithMappedValueAndRowNumber : " + key ));
	}


	public void resetTotalNumberOfRows() {
		this.totalNumberOfRows = 0L;
	}

	public synchronized void add(Long bytePosition) {
		this.mapRowNumberBytePosition.put(totalNumberOfRows, bytePosition);
		totalNumberOfRows++;
	}

	public synchronized void addGenericIndexedValue(String GenericIndexedValue, Long rowIndex) {
		ConcurrentHashMap<String, Long> isExisting;
		ConcurrentHashMap<String, Long> addedNewIndexByValue = new ConcurrentHashMap<>();

		if (checkExists()) {
			isExisting = getExisting();
			isExisting.put(GenericIndexedValue, rowIndex);
			this.indexByWithMappedValueAndRowNumber.put(this.schema.indexBy, isExisting);
		} else {
				addedNewIndexByValue.put(GenericIndexedValue, rowIndex);
				this.indexByWithMappedValueAndRowNumber.put(this.schema.indexBy, addedNewIndexByValue);
		}
	}

	private boolean checkExists() {
		return indexByWithMappedValueAndRowNumber.containsKey(this.schema.indexBy) ? true : false;
	}

	private ConcurrentHashMap<String, Long> getExisting() {
		return indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);
	}

	//previously hasNameInIndex
	public boolean hasGenericIndexedValueInGenericIndex(final String GenericIndexedValue) {
		ConcurrentHashMap<String, Long> checkValue = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);

		if (checkValue != null) {
			return checkValue.containsKey(GenericIndexedValue);
		}
		return false;
	}


	public Long getRowNumberByName(final String GenericIndexedValue) {
		ConcurrentHashMap<String, Long> checkValue = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);

		try {
			if (checkValue != null) {
				return checkValue.getOrDefault(GenericIndexedValue, -1L);
			}
			throw new DBException("@GenericIndex/getRowNumberByName: found indexed by but returned a null CHashMap");
		}catch (DBException e) {
			e.printStackTrace();
		}
		return -2L;
	}

	// Not used in Index so not here
	/*public void remove(Long rowIndex, Object existingRowNumberObject, Class aClass) {
		this.mapRowNumberBytePosition.remove(rowIndex);
		this.indexByWithMappedValueAndRowNumber.remove(existingRowNumberObject.getName());
		this.totalNumberOfRows--;
	}*/


	public Long getRowsBytePosition(Long rowNumber) {
		return this.mapRowNumberBytePosition.getOrDefault(rowNumber, -1L);
	}

	public synchronized Long getTotalNumberOfRows() {
		return this.totalNumberOfRows;
	}



	public synchronized void clear() {
		this.totalNumberOfRows = 0L;
		this.mapRowNumberBytePosition.clear();
		this.indexByWithMappedValueAndRowNumber.clear();
	}

	// was getNames()
	public synchronized Collection<String> getGenericIndexedValues() {
		Collection<String> noValuesFound  = new ArrayList<>(0);
		ConcurrentHashMap<String, Long> checkValue = new ConcurrentHashMap<>();
		try {
			 checkValue = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		if(checkValue != null)
		return checkValue.keySet();
		else
			return noValuesFound;
	}

	public void removeByFilePosition(Long position) {
		Long rowIndex;
		String GenericIndexedValue = null;
		ConcurrentHashMap<String, Long> cHashMap = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);

		if(mapRowNumberBytePosition.isEmpty()) {
			return;
		}
		rowIndex = mapRowNumberBytePosition.search(1, (k, v) -> (Objects.equals(v, position)) ? k : -1);
		if (rowIndex != -1L) {
			GenericIndexedValue = cHashMap.search(1, (k, v) -> Objects.equals(v, rowIndex) ? k : null);
		}

		if (GenericIndexedValue != null)
			remove(rowIndex, GenericIndexedValue); // just doing this in same way for now could just pass to remove(rowIndex);
		else throw new RuntimeException("INDEX: removeByFilePosition - rowIndex " + rowIndex + " was searched for in indexByWithMappedValueAndRowNumber, but was not found in order to be removed");

	}


	public void remove(Long rowIndex, String GenericIndexedValue) {
		ConcurrentHashMap<String, Long> checkValue = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);

		this.mapRowNumberBytePosition.remove(rowIndex);
		this.totalNumberOfRows--;

		if(checkValue != null) {
			if (GenericIndexedValue != null)
				checkValue.remove(GenericIndexedValue);
		}
	}


	//removes from ByteMap and NameMap (Better names?)
	public void remove(Long rowIndex) {
		this.mapRowNumberBytePosition.remove(rowIndex);
		this.totalNumberOfRows--;

		ConcurrentHashMap<String, Long> checkValue = this.indexByWithMappedValueAndRowNumber.get(this.schema.indexBy);
		if(checkValue != null) {
			String valueToDelete = checkValue.search(1, (k, v) -> Objects.equals(v, rowIndex) ? k : null);
			if (valueToDelete != null)
				checkValue.remove(valueToDelete);
		}
	}

}
