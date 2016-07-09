package com.bouncers.b2b.librarifier;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonFilter;
/**
 * @author emin
 * it is a file which is uploaded and downloaded
 */
@JsonFilter("stuffFileFilter")
public class StuffFile implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name; // TODO this will include path
	private long size;
	private int numberOfBooks;
	private List<Book> books;
	private transient boolean existOnDisk;
	
	public StuffFile() {}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}

	public int getNumberOfBooks() {
		return numberOfBooks;
	}
	public void setNumberOfBooks(int numberOfBooks) {
		this.numberOfBooks = numberOfBooks;
	}
	public List<Book> getBooks() {
		return books;
	}
	public void setBooks(List<Book> books) {
		this.books = books;
	}

	public boolean isExistOnDisk() {
		return existOnDisk;
	}
	public void setExistOnDisk(boolean existOnDisk) {
		this.existOnDisk = existOnDisk;
	}
	@Override
	public String toString() {
		return "StuffFile [name=" + name + ", size=" + size + ", numberOfBooks=" + numberOfBooks + ", books=" + books
				+ "]";
	}
	public Book getBookByIndex(long index){
		for (Book book : books) {
			if(book.getIndex() == index)
				return book;
		}
		return null;
	}
	@JsonIgnore
	public List<Book> getAvailableBooks(){
		List<Book> avaBooks = new ArrayList<Book>();
		for (Book bk : this.getBooks()) {
			if(bk.isExistOnDisk())
				avaBooks.add(bk);
		}
		return avaBooks;
	}
	@JsonIgnore
	public List<Book> getUnAvailableBooks(){
		List<Book> unAvaBooks = new ArrayList<Book>();
		for (Book bk : this.getBooks()) {
			if(!bk.isExistOnDisk())
				unAvaBooks.add(bk);
		}
		return unAvaBooks;
	}
	@JsonIgnore
	public String getOnlyName(){
		int index = this.getName().lastIndexOf("/") == -1 ? this.getName().lastIndexOf("\\") : this.getName().lastIndexOf("/");
		return this.getName().substring(index);
	}
	
	/** Dosyanın indirilidği adrsteki adresi
	 * @param libraryName
	 * @return
	 */
	public String getOnlyPath(String libraryName){
		int index = this.getName().lastIndexOf("/") == -1 ? this.getName().lastIndexOf("\\") : this.getName().lastIndexOf("/");
		return this.getName().substring(this.getName().indexOf(libraryName), index);
	}
	
	/** Dosyanın orjinal paylaşıldığı disk üzerindeki adresi
	 * @return
	 */
	@JsonIgnore
	public String getRealPath(){
		int index = this.getName().lastIndexOf("/") == -1 ? this.getName().lastIndexOf("\\") : this.getName().lastIndexOf("/");
		return getName().substring(0, index);
	}
}

