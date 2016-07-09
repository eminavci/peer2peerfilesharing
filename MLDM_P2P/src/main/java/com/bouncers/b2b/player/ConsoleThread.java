package com.bouncers.b2b.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.event.EventListenerList;
import com.bouncers.b2b.librarifier.Library;
import com.bouncers.b2b.librarifier.LibraryProcessor;
import com.bouncers.b2b.util.Consts;
import com.bouncers.b2b.util.IOFileManager.MODE;
import com.bouncers.b2b.util.LoggerUtil;
import com.bouncers.b2b.util.Util;

public class ConsoleThread extends Thread{
	private final EventListenerList listeners = new EventListenerList();
	BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
	private boolean running;
	private String ip;
	private MODE mode;
	private boolean hasALibr = false;
	
	public ConsoleThread(MODE mode) throws Exception {
		this.setName("ConsoleThread");
		this.mode = mode;
		this.ip = Util.getIpAddress();
		if(this.ip == null || !this.ip.contains("."))
			throw new Exception("Ip address of The machine could not be found");
	}

	@Override
	public void run() {
		LoggerUtil.info("ConsoleThread started IN MODE : " + mode);
		running = true;
		welcomeMsg();
		instructionsMsg();
		while (running) {
			try {
				String line = bf.readLine();
				
				if(line == null || line.length() == 0)
					instructionsMsg();
				line = line.trim();
				line = Util.onlyOneWhiteSpace(line);
				
				if(line.equalsIgnoreCase("help"))
					instructionsMsg();
				else if(line.equalsIgnoreCase("quit")){
					triggerExitEvent();
					break;
				} else if(line.contains(".libr")){
					if(mode == MODE.ONE){
						if(!hasALibr){
							File librFile = new File(line);
							if(librFile.exists()){
								LibraryProcessor librProcessor = new LibraryProcessor(librFile);
								Library library = librProcessor.getLibrary();
								triggerDownloadingEvent(library);
								hasALibr = true;
							} else {
								LoggerUtil.error("The libr file for $$ path does not exist", line);
							}
						} else {
							LoggerUtil.error("In mode1, a Player can only upload or download one time");
						}
					} else { //MODE2
						File librFile = new File(line);
						if(librFile.exists()){
							LibraryProcessor librProcessor = new LibraryProcessor(librFile);
							Library library = librProcessor.getLibrary();
							triggerDownloadingEvent(library);
						} else {
							LoggerUtil.error("The libr file for $$ path does not exist", line);
						}
					}
				
				} else if(line.contains("/")){// Since it is uploading, an HubServer will be created. so a port is required
					if(mode == MODE.ONE){
						if(!hasALibr){
							String[] pars = line.split(" ");
							int port = Integer.valueOf(pars[1]);
							File dataFile = new File(pars[0]);
							if(!dataFile.exists()){
								System.out.println("The file doesn\'t exist : " + line);
							} else {
								if(!testSocketValid(this.ip, port))
									continue;
								LibraryProcessor librProcessor = new LibraryProcessor();
								librProcessor.setLibraryData(this.ip, port, dataFile.getPath());
								librProcessor.generateLibrary();
								
								triggerUploadingEvent(librProcessor.getLibrary());
								hasALibr = true;
							}
						} else {
							LoggerUtil.error("In mode1, a Player can only upload or download one time");
						}
					} else { // MODE2
						String[] pars = line.split(" ");
						String fileName = pars[0];
						String ip2 = pars[1];
						int port2 = Integer.valueOf(pars[2]);
						
						if(!testSocketValid(ip2, port2))
							continue;
						
						File dataFile = new File(fileName);
						if(!dataFile.exists()){
							System.out.println("The file doesn\'t exist : " + line);
						} else {
							LibraryProcessor librProcessor = new LibraryProcessor();
							librProcessor.setLibraryData(ip2, port2, fileName);
							librProcessor.generateLibrary();
							
							triggerUploadingEvent(librProcessor.getLibrary());
						}
					}
				} else {
					System.err.println("Wrong command!");
					instructionsMsg();
				}
			} catch (Exception e) {
				LoggerUtil.error(e);
			}
		}
		
		LoggerUtil.info("ConsoleThread is finished");
	}
	
	private boolean testSocketValid(String ip2, int port2){
		try {
			if(!Consts.IS_TEST){
				Socket ss = new Socket();
		        ss.connect(new InetSocketAddress(ip2, port2), 1);
		        ss.close();
		        
				LoggerUtil.error("There is no HUB on $$ $$ the your lbr file", ip2, port2);
				System.err.println(port2 + "");
				instructionsMsg();
				return false;
			}
		
		} catch (Exception e) {
			return true;
		}
		return true;
	}
	
	private void welcomeMsg(){
		for (int i = 0; i < 5; i++) {
			if(i == 0 || i == 4)
				System.out.println("##########################################################################");
			else if(i == 2)
				System.out.println("#                  WELCOME TO P2P DATA SHARING APP                       #");
			else
				System.out.println("#                                                                        #");
		}
	}
	private void instructionsMsg(){
		System.out.println("################################ INSTUCTIONS #############################\n");
		System.out.println("To Share Data give the absolute path of your folder or file and the port of Hub: ");
		System.out.println("To Download Data give the libr file");
		System.out.println("To See the result of your downloading Enter 'mystatus'");
		System.out.println("To See the instructions again Enter 'help'");
		System.out.println("To Exit the application Enter 'quit'");
	}
	private void goodByeMsg(){
		for (int i = 0; i < 5; i++) {
			if(i == 0 || i == 4)
				System.out.println("##########################################################################");
			else if(i == 2)
				System.out.println("#                            :) GOOD BYE!                               #");
			else
				System.out.println("#                                                                        #");
		}
	}

	
	public void addConsoleListener(ConsoleListener listener){
		this.listeners.add(ConsoleListener.class, listener);
	}
	public ConsoleListener[] getConsoleListener(){
		return this.listeners.getListeners(ConsoleListener.class);
	}
	
	public void triggerExitEvent(){
		killConnection();
		for (ConsoleListener listener : getConsoleListener()) {
			listener.exit();
		}
	}
	
	public void triggerUploadingEvent(Library library) throws IOException{
		for (ConsoleListener listener : getConsoleListener()) {
			listener.uploding(library);
		}
	}
	public void triggerDownloadingEvent(Library library) throws IOException{
		for (ConsoleListener listener : getConsoleListener()) {
			listener.downloading(library);
		}
	}

	public void killConnection(){
		goodByeMsg();
		this.running = false;
	}
}
