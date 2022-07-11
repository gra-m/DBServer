package fun.madeby.transaction;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Gra_m on 2022 07 11
 */

public final class Transaction implements ITransaction{
	private double uId;
	private LinkedList<Long> newRows;
	private LinkedList<Long> deletedRows;


	public Transaction() {
		this.uId = getRandomNumber();
		newRows = new LinkedList<>();
		deletedRows = new LinkedList<>();
	}


	@Override
	public void commit() {

	}

	@Override
	public void rollback() {

	}

	@Override
	public double getUid() {
		return 0;
	}

	@Override
	public void registerAdd(Long position) {
		this.newRows.add(position);
	}

	@Override
	public void registerDelete(Long position) {
		this.deletedRows.add(position);
	}

	private Double getRandomNumber() {
		//return new Random().nextLong();

		double min = 1D;
		double max = Double.MAX_VALUE;
		//return min + new Random().nextDouble() * (max - min);
		return (Math.random() + 1) * (max - min);
	}

	}
