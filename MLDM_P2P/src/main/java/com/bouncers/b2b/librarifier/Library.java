package com.bouncers.b2b.librarifier;
import java.beans.Transient;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonFilter;
/**
 * @author emin
 * 
 * Actuall Torrent file. 
 * Containing a bunch of information about peer(Player) and shared file
 */
@JsonFilter("libraryFilter")
public class Library implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name; // max 30 character
	private String hubIp;
	private int hubPort;	
	private int numberOfFiles;
	private long bookSize; // size of each piece
	private long size;
	
	
	private boolean existOnDisk;
	private boolean amIInitialSharer;
	
	private List<StuffFile> files = new ArrayList<StuffFile>();
	public Library() {}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHubIp() {
		return hubIp;
	}
	public void setHubIp(String hubIp) {
		this.hubIp = hubIp;
	}
	public int getHubPort() {
		return hubPort;
	}
	public void setHubPort(int hubPort) {
		this.hubPort = hubPort;
	}
	public int getNumberOfStuffs() {
		return numberOfFiles;
	}
	public void setNumberOfStuffs(int numberOfStuffs) {
		this.numberOfFiles = numberOfStuffs;
	}
	public long getBookSize() {
		return bookSize;
	}
	public void setBookSize(long bookSize) {
		this.bookSize = bookSize;
	}
	public List<StuffFile> getStuffs() {
		return files;
	}
	public void setStuffs(List<StuffFile> stuffs) {
		this.files = stuffs;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public boolean isExistOnDisk() {
		return existOnDisk;
	}
	public StuffFile getStufFileByName(String stuffName){
		for (StuffFile stuff : files) {
			if(stuff.getName().equals(stuffName))
				return stuff;
		}
		return null;
	}
	
	public void setExistOnDisk(boolean existOnDisk) {
		this.existOnDisk = existOnDisk;
		if(this.existOnDisk){
			for (StuffFile stuff : files) {
				stuff.setExistOnDisk(true);
				for (Book book : stuff.getBooks()) {
					book.setExistOnDisk(true);
				}
			}
		}
	}
	@Transient
	public boolean isAmIInitialSharer() {
		return amIInitialSharer;
	}
	public void setAmIInitialSharer(boolean amIInitialSharer) {
		this.amIInitialSharer = amIInitialSharer;
	}

	@Override
	public String toString() {
		return "Library [name=" + name + ", hubIp=" + hubIp + ", hubPort=" + hubPort + ", numberOfFiles="
				+ numberOfFiles + ", bookSize=" + bookSize + ", size=" + size + ", files=" + files + "]";
	}

	@Override
	public boolean equals(Object obj) {
		Library ll = (Library) obj;
		return this.getName().equals(ll.getName());
	}

	public int numOfNonExistFiles(){
		int i = 0;
		for (StuffFile stuff : files) {
			if(!stuff.isExistOnDisk())
				i++;
		}
		return i;
	}
	@JsonIgnore
	public Map<String, long[]> getAvailableBooksListInMap(){
		Map<String, long[]> map = new HashMap<String, long[]>();
		
		for (StuffFile stuff : files) {
			List<Book> avaBooks = stuff.getAvailableBooks();
			long[] abArr = new long[avaBooks.size()];
			for (int i = 0; i < avaBooks.size(); i++) {
				abArr[i] = avaBooks.get(i).getIndex();
			}
			map.put(stuff.getName(), abArr);
		}
		return map;
	}
	
	@JsonIgnore
	public Map<String, long[]> getUnAvailableBooksListInMap(){
		Map<String, long[]> map = new HashMap<String, long[]>();
		
		for (StuffFile stuff : files) {
			List<Book> avaBooks = stuff.getUnAvailableBooks();
			long[] abArr = new long[avaBooks.size()];
			for (int i = 0; i < avaBooks.size(); i++) {
				abArr[i] = avaBooks.get(i).getIndex();
			}
			map.put(stuff.getName(), abArr);
		}
		return map;
	}
	
	@JsonIgnore
	public String getOnlyName(){
		int index = this.getName().lastIndexOf("/") == -1 ? this.getName().lastIndexOf("\\") : this.getName().lastIndexOf("/");
		return this.getName().substring(index);
	}
	@JsonIgnore
	public String getOnlyPath(){
		int index = this.getName().lastIndexOf("/") == -1 ? this.getName().lastIndexOf("\\") : this.getName().lastIndexOf("/");
		return this.getName().substring(0, index);
	}
}
