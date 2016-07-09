package com.bouncers.b2b.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.bouncers.b2b.core.data.TransferredBook;
import com.bouncers.b2b.librarifier.Library;
import com.bouncers.b2b.librarifier.StuffFile;

public class IOFileManager {
	
	public enum MODE{
		ONE(1),
		TWO(2);		
		final int mode;
		
		MODE(int mode){
			this.mode = mode;
		}
		public int getMode() {
			return mode;
		}
	}
	
	private final static String historyFileName = "p2p.txt";
	private File histFile;
	private String playerPath; // Maybe multiple client can run on same machine...This is a way to spearate their history file.
	private String pcPath = System.getProperty("user.home") + File.separator + "Downloads";
	private File downloadFilePath;
	private Map<String, LibraryBean> librBeans;
	private MODE mode; // 1 or 2
	
	
	public IOFileManager(String playerPath, MODE mode) {
		super();
		this.playerPath = playerPath;
		this.mode = mode;
		if(!(mode == MODE.ONE || mode == MODE.TWO))
			this.mode = MODE.ONE;
		init();
	}


	private synchronized void init() {
		histFile = new File(this.playerPath + File.separator + historyFileName);
		
		if(!histFile.exists()){
			histFile.getParentFile().mkdirs();
			try {
				histFile.createNewFile();
			} catch (IOException e) {
				LoggerUtil.error("Error occured while creating player\'s history file", e);
				System.exit(0); // TODO trigger exit event;
			}
		}
		
		downloadFilePath = new File(pcPath + File.separator + playerPath);
		if(!downloadFilePath.exists())
			downloadFilePath.mkdirs();
		
		librBeans = initLibraries();
		
	}


	private synchronized Map<String, LibraryBean> initLibraries() {
		Map<String, LibraryBean> librBeans = new HashMap<String, LibraryBean>();
		if(this.histFile.length()>0){
			List<Library> librs = null;
			try {
				librs = new ObjectMapper().readValue(this.histFile, new TypeReference<List<Library>>() {});
			} catch (Exception e) {
				LoggerUtil.error("Library File error. Exit," , e); // TODO trigger exit event;
				System.exit(0);
			} 
			
			if(librs != null && librs.size()>0){
				boolean updateFile = false;
        		
        		for (Iterator<Library> iterator = librs.iterator(); iterator.hasNext();) {
					Library lib = (Library) iterator.next();
					if(! isLibraryValid(lib)){
    					iterator.remove();
    					updateFile = true;
    				} else {
    					LibraryBean librBean = new LibraryBean(lib, this);
    					librBeans.put(lib.getName(), librBean);
    				}
				}
        		
        		if(updateFile){
        			try {
        				FileWriter fr = new FileWriter(histFile, false);
        				fr.write(new ObjectMapper().writeValueAsString(librs));
        				fr.flush();
        				fr.close();
        			} catch (Exception e) {
        				LoggerUtil.error("Initilazing user file sharing history encountered an error ", e);// TODO trigger exit event;
        				System.exit(0);
        			}
        		}
			}
		}
		return librBeans;
	}
	
	/** 
	 * Even if a stuff in library is not found, it means the file corrupted. 
	 * so it is not valid anymore. Warn user and revome  it from availableLibrary list.
	 * @param lib
	 * @return
	 */
	private boolean isLibraryValid(Library lib){
		if(lib == null || ((lib.isAmIInitialSharer() && !new File(lib.getName()).exists()) ||
				(!lib.isAmIInitialSharer() && !new File(downloadFilePath, lib.getOnlyName()).exists())))
			return false;
		
		for(StuffFile stuff : lib.getStuffs()){
			if(lib.isAmIInitialSharer()){
				if(!new File(stuff.getName()).exists())
					return false;
			} else {
				if(!new File(downloadFilePath + File.separator + stuff.getOnlyPath(lib.getOnlyName()), stuff.getOnlyName()).exists())
					return false;
			}
		}
		return true;
	}
	
	public synchronized boolean addANewLibrary(Library libr) throws IOException{
			
		if(!isAvailableForUploadDownload() && this.librBeans.containsKey(libr.getName()))
			return false;
		if(!libr.isAmIInitialSharer())
			writeFileDataofLibrary(libr);// MAybe better to add a transaction rollback here also
		
		this.librBeans.put(libr.getName(), new LibraryBean(libr, this));
		
		try {
			FileWriter fr = new FileWriter(histFile, false);
			
			fr.write(new ObjectMapper().writer(Util.getEmptyFilter()).writeValueAsString(getLibrariesAsList()));
			fr.flush();
			fr.close();
		} catch (Exception e) {
			LoggerUtil.error("Adding new library to player history file error occured ", e);
			this.librBeans.remove(libr.getName());
			return false;
		}
	
		return true;
	}
	
	private void writeFileDataofLibrary(Library libr) throws IOException{
		if(libr.isAmIInitialSharer()){
			
		} else {
			File mainFile = new File(downloadFilePath, libr.getOnlyName());
			if(mainFile.isDirectory()){
				mainFile.createNewFile();
			} else {
				mainFile.mkdirs();
				for (StuffFile stuff : libr.getStuffs()) {
					String temp = stuff.getOnlyPath(libr.getOnlyName());
					
					File fff = new File(downloadFilePath, temp);
					if(!fff.exists() && !fff.mkdirs()){
					    throw new IllegalStateException("Couldn't create dir: " + fff);
					}
					if(fff.exists()){
						File f = new File(fff, stuff.getOnlyName());
						f.createNewFile();
					}
				}
			}
		}
	}
	
	/**
	 * @param stuffName it is library name
	 * @param bookIndex
	 * @return
	 * @throws Exception
	 */
	public synchronized TransferredBook getBooksData(String stuffName, String fileName, long bookNumber) throws Exception {
		LibraryBean librBean = librBeans.get(stuffName);
		return librBean.getBooksData(fileName, bookNumber);
	}
	
	
	public synchronized void writeBookData(TransferredBook tbk) throws Exception{
		LibraryBean librBean = librBeans.get(tbk.getStuffName());
		librBean.writeBookData(tbk);
	}
	
	protected synchronized void updateLibraryHistFile(final boolean isBooksExistanceAlso){
//		new Thread(new Runnable() {
//			public void run() {
				try {
					List<Library> librs = getLibrariesAsList();
					if(isBooksExistanceAlso){
						for (Library library : librs) {
							boolean isLibrExist = true;
						    for (StuffFile stuff : library.getStuffs()) {
								if(stuff.getUnAvailableBooks().size() == 0)
									stuff.setExistOnDisk(true);
								else
									isLibrExist = false;
							}
						    if(isLibrExist){
						    	library.setExistOnDisk(true);
						    	System.out.println("{\n################################################# \n"
						    			+ "A Library Download is finished! \n"
						    			+ "#################################################\n");
						    	librBeans.get(library.getName()).closeRFiles();
						    	
						    }
						}
					}
					
					FileWriter fw = new FileWriter(histFile, false);
					fw.write(new ObjectMapper().writer(Util.getEmptyFilter()).writeValueAsString(librs));
					fw.flush();
					fw.close();
			
				} catch (Exception e) {
					LoggerUtil.error("Update,ng library history file error", e);
				}
//			}
//		}).start();
	}
	
	public List<Library> getLibrariesAsList(){
		List<Library> librs = new ArrayList<Library>();
		for (Map.Entry<String, LibraryBean> entry : librBeans.entrySet()){
			LibraryBean librBean = entry.getValue();
			librs.add(librBean.getLibrary());
		}
		return librs;
	}
	
	public List<Library> getUnAvailableLibrs(){
		List<Library> librs = getLibrariesAsList();
		Iterator<Library> iter = librs.iterator();
		while (iter.hasNext()) {
			Library library = (Library) iter.next();
			if(library.isExistOnDisk())
				iter.remove();
		}
		
		return librs;
	}
	
	public File getDownloadFilePath() {
		return downloadFilePath;
	}
	
	public boolean isAvailableForUploadDownload(){
		return (this.mode == MODE.ONE && librBeans.size() == 0) || (this.mode == MODE.TWO);
	}
	
	public synchronized Library getLibraryByName(String libraryName){
		for (Library library : getLibrariesAsList()) {
			if(library.getName().equals(libraryName))
				return library;
		}
		return null;
	}
	public Map<String, LibraryBean> getLibrBeans() {
		return librBeans;
	}
	
}
