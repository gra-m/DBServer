package fun.madeby.util;

import fun.madeby.exceptions.DBException;

/**
 * Created by Gra_m on 2022 08 17
 */

public final class GeneralUtils {

	public static boolean testInputStreamReadLength(String classMethod, int isReadLength, int expectedReadLength) throws DBException
		{
		if (isReadLength == expectedReadLength)
			return true;
		else throw new DBException(classMethod + " isReadLength of stream.read did not match expectedReadLength");
	}

}
