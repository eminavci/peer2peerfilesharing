package com.bouncers.b2b.librarifier;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.bouncers.b2b.util.Consts;
import com.bouncers.b2b.util.LoggerUtil;
import com.bouncers.b2b.util.Util;


public class LibraryProcessor {
	
	private Library library;
	private boolean generating = true;
	private File librFile;

	/**
	 *  When a player wants to create a librs file to share
	 */
	public LibraryProcessor() {
		super();
		this.generating = true;
		this.library = new Library();
	}
	
	/**
	 * When a player wants to DOWNLOAD data by uploading a .libr file
	 * @param librFile
	 * @throws Exception
	 */
	public LibraryProcessor(File librFile) throws Exception{
		super();
		this.generating = false;
		if(librFile == null || !librFile.exists())
			throw new Exception("The libr file doesn\'t exist!");
		this.librFile = librFile;
	}
	
	
	
//	public LibraryProcessor(Library library) {
//		super();
//		this.generating = true;
//		this.library = library;
//	}
	
	
	
	public void setLibraryData(String hubip, int hubPort, String filePath) throws Exception{
		if(!this.generating)
			throw new Exception("THis method can not be called if you dont generate a libr file!");
			
		File libr = new File(filePath);
		if(!libr.exists())
			throw new Exception("File does not exist : " + filePath);
		
		List<StuffFile> stuffs = createStuffsForLibrary(Util.listFilesForFolder(libr));
		createBooksForStuff(stuffs);
		
		this.library.setBookSize(Consts.BOOK_SIZE * 1024);
		this.library.setHubIp(hubip);
		this.library.setHubPort(hubPort);
		this.library.setName(libr.getPath());
		this.library.setNumberOfStuffs(stuffs.size());
		this.library.setSize(FileUtils.sizeOf(libr));
		this.library.setStuffs(stuffs);
		
	}


	/**
	 *  Save to the Desktop as default
	 * @throws Exception 
	 */
	public void generateLibrary() throws Exception{
		if(!this.generating)
			throw new Exception("THis method can not be called if you dont generate a libr file!");
		ObjectMapper mapper = new ObjectMapper();
		
		File desktop = new File(System.getProperty("user.home"), "Desktop");
		FileOutputStream fos = new FileOutputStream(desktop + File.separator + this.library.getOnlyName() + "." + Consts.EXTENSION);
		fos.write(mapper.writer(Util.getFullFilter()).writeValueAsBytes(this.library));
	}
	
	
	private List<StuffFile> createStuffsForLibrary(List<File> files) throws Exception{
		if(!this.generating)
			throw new Exception("THis method can not be called if you dont generate a libr file!");
		List<StuffFile> stuffs = new ArrayList<StuffFile>();
		for (File file : files) {
			StuffFile stuff = new StuffFile();
			stuff.setName(file.getPath());
			stuff.setSize(FileUtils.sizeOf(file));
			stuffs.add(stuff);
		}
		return stuffs;
	}
	
	private void createBooksForStuff() throws Exception{
		createBooksForStuff(this.getLibrary().getStuffs());
	}
	
	private void createBooksForStuff(List<StuffFile> stuffs) throws Exception{
		if(!this.generating)
			throw new Exception("THis method can not be called if you dont generate a libr file!");
		for (StuffFile stuff : stuffs) {
			List<Book> books = new ArrayList<Book>();
			File fileStuff = new File(stuff.getName()); 
			if(!fileStuff.exists())
				throw new Exception("File does not exist in Library : " + stuff.getName());
			
			byte[] buffer = new byte[(int) (Consts.BOOK_SIZE * 1024)];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileStuff));
			int tmp = 0;
			while ((tmp = bis.read(buffer)) > 0) {
				if (tmp < buffer.length) // the last piece
					buffer = Arrays.copyOfRange(buffer, 0, tmp);
				Book book = new Book(Util.hashShaString(buffer), tmp);
				book.setIndex(books.size());
				books.add(book);
			}
			if(fileStuff.length() == 0){ // it means it did not enter to while loop
				Book book = new Book("", 0);
				book.setIndex(0);
				books.add(book);
			}
			stuff.setBooks(books);
			stuff.setNumberOfBooks(books.size());
		}
	}
	

	public Library getLibrary() {
		if(generating)
			return library;
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(librFile, Library.class);
		} catch (Exception e) {
			LoggerUtil.getLogger().severe("Libr File error : " + e);
		}
		return null;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	@Override
	public String toString() {
		return "LibraryProcessor [library=" + library + "]";
	}
}
