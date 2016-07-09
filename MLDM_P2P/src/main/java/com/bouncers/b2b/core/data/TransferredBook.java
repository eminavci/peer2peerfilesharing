package com.bouncers.b2b.core.data;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferredBook {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String stuffName;
	private String fileName;
	private long bookNumber;
	private long size;
	private byte[] data;
	
	public TransferredBook() {}
	
	public TransferredBook(String stuffName, String fileName, long bookNumber, long size, byte[] data) {
		super();
		this.stuffName = stuffName;
		this.fileName = fileName;
		this.bookNumber = bookNumber;
		this.size = size;
		this.data = data;
	}
	
	public String getStuffName() {
		return stuffName;
	}
	public void setStuffName(String stuffName) {
		this.stuffName = stuffName;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public long getBookNumber() {
		return bookNumber;
	}
	public void setBookNumber(long bookNumber) {
		this.bookNumber = bookNumber;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	@JsonIgnore
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
}
