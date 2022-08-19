package fun.madeby.transaction;

import java.util.Collection;
import java.util.LinkedList;

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

	public void clear() {
		this.newRows.clear();
		this.deletedRows.clear();
	}

	@Override
	public double getUid() {
		return this.uId;
	}

	@Override
	public void registerAdd(Long position) {
		this.newRows.add(position);
	}

	@Override
	public void registerDelete(Long position) {
		this.deletedRows.add(position);
	}

	@Override
	public Collection<Long> getNewRowsBytePosition() {
		LinkedList<Long> copy = new LinkedList(this.newRows);
		return copy;
	}

	@Override
	public Collection<Long> getDeletedRowsBytePosition() {
		LinkedList<Long> copy = new LinkedList(this.deletedRows);
		return copy;
	}

	private Double getRandomNumber() {
		//return new Random().nextLong();

		double min = 1D;
		double max = Double.MAX_VALUE;
		//return min + new Random().nextDouble() * (max - min);
		return (Math.random() + 1) * (max - min);
	}

	}
