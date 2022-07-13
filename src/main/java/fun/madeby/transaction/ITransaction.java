package fun.madeby.transaction;

import java.util.Collection;

/**
 * Created by Gra_m on 2022 07 11
 */

public interface ITransaction {
	void commit();
	void rollback();
	double getUid();
	void registerAdd(Long position); // to easily update temporary flag
	void registerDelete(Long position); // to easily update deleted flag
	Collection<Long> getNewRowsBytePosition();
	Collection<Long> getDeletedRowsBytePosition();

}