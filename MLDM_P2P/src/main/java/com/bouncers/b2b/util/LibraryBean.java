package com.bouncers.b2b.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import com.bouncers.b2b.core.data.TransferredBook;
import com.bouncers.b2b.librarifier.Book;
import com.bouncers.b2b.librarifier.Library;
import com.bouncers.b2b.librarifier.StuffFile;


public class LibraryBean {
	
	private ObjectMapper mapper;
	private Map<String, RandomAccessFile> raFiles;
	private Library library;
	private IOFileManager ioManager;
	
	public LibraryBean() {}

	public LibraryBean(Library library, IOFileManager ioManager) {
		super();
		this.library = library;
		this.ioManager = ioManager;
		this.raFiles = new HashMap<String, RandomAccessFile>();
		initRaf();
	}
	
	private void initRaf(){
		for (StuffFile stuff : library.getStuffs()) {
			try {
				File f;
				if(!library.isAmIInitialSharer())
					f = new File(ioManager.getDownloadFilePath() + File.separator + stuff.getOnlyPath(library.getOnlyName()), stuff.getOnlyName());
				else
					f = new File(stuff.getName());
					
				RandomAccessFile raf = new RandomAccessFile(f, "rw");
				raf.setLength(stuff.getSize());
				raFiles.put(stuff.getName(), raf);
			} catch (FileNotFoundException e) {
				LoggerUtil.error("Fİle is not found");
			} catch (IOException e) {
				LoggerUtil.error("Fİle is not found2222");
			}
		}
		
	}
	
	
	@JsonIgnore
	public TransferredBook getBooksData(String fileName, long bookNumber) throws IOException{
		Book bk = library.getStufFileByName(fileName).getBookByIndex(bookNumber);
		
		byte[] data = new byte[(int) bk.getSize()];
		
		RandomAccessFile rac = raFiles.get(fileName);
		if(rac == null)
			return null;
		
		try {
			rac.seek(Consts.BOOK_SIZE * 1024 * bookNumber);
		} catch (Exception e) {
			System.out.println("Error caught but : " + e);
			rac = new RandomAccessFile(new File(fileName), "rw");
			rac.seek(Consts.BOOK_SIZE * 1024 * bookNumber);
		}
		rac.read(data);
		//rac.close();
		TransferredBook tb = new TransferredBook(library.getName(), fileName, bookNumber, bk.getSize(), data);
		
		return tb;
	}
	@JsonIgnore
	public  void writeBookData(TransferredBook tbk) throws Exception{
		Book bk = library.getStufFileByName(tbk.getFileName()).getBookByIndex(tbk.getBookNumber());
		if(bk !=null){
			if(!bk.getShaCode().equals(Util.hashShaString(tbk.getData())))
				return;
			
			RandomAccessFile rac = raFiles.get(tbk.getFileName());
			if(rac == null)
				return;
			rac.seek(Consts.BOOK_SIZE * 1024 * (bk.getIndex()));
			
			rac.write(tbk.getData());
			//rac.close();
			bk.setExistOnDisk(true);
			ioManager.updateLibraryHistFile(true);
		}
	}

	public Library getLibrary() {
		return library;
	}
	@JsonIgnore
	public ObjectMapper getMapper() {
		return mapper;
	}
	@JsonIgnore
	public Map<String, RandomAccessFile> getRaFiles() {
		return raFiles;
	}
	
	public synchronized void closeRFiles(){
		try {
			for(Map.Entry<String, RandomAccessFile> entry : raFiles.entrySet()){
				RandomAccessFile raf = entry.getValue();
				raf.close();
			}
		} catch (Exception e) {
			LoggerUtil.error("Closing RandomAccessFile Error : ", e);
		}
	}
}
