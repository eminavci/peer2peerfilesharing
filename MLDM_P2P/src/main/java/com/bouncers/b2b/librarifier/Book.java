package com.bouncers.b2b.librarifier;

import java.beans.Transient;
import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonFilter;

/**
 * @author emin
 * it is original piece of share file(Library)
 */
@JsonFilter("bookFilter")
public class Book implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient String shaCode;
	private long index;
	private long size;
	private boolean existOnDisk;
	
	public Book() {}

	public Book(String shaCode, long size) {
		super();
		this.shaCode = shaCode;
		this.size = size;
	}
	@Transient
	public String getShaCode() {
		return shaCode;
	}
	public void setShaCode(String shaCode) {
		this.shaCode = shaCode;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getIndex() {
		return index;
	}
	public void setIndex(long index) {
		this.index = index;
	}

	public boolean isExistOnDisk() {
		return existOnDisk;
	}
	public void setExistOnDisk(boolean existOnDisk) {
		this.existOnDisk = existOnDisk;
	}

	@Override
	public String toString() {
		return "Book [shaCode=" + shaCode + ", index=" + index + ", size=" + size + ", existOnDisk=" + existOnDisk + "]";
	}
}
