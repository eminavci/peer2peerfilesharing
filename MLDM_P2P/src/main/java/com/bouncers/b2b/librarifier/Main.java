package com.bouncers.b2b.librarifier;

/**
 * @author emin
 * Creating library file
 */
public class Main {
	
	public static void main(String[] args) throws Exception {

		String filePath = "/home/emin/Documents"; 

		
		//Socket soc = new Socket("161.3.244.136", 45452);
		
		LibraryProcessor librProcessor = new LibraryProcessor();
		librProcessor.setLibraryData("127.0.0.1", 42000, filePath);
		librProcessor.generateLibrary();
		
		System.out.println("Library file is created");
		System.exit(0);
		
//		PlayerConnection con = new PlayerConnection(null);
//		con.start();
	}

}
