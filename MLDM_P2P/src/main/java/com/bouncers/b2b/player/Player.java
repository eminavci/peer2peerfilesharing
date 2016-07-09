package com.bouncers.b2b.player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.bouncers.b2b.core.DownloadTask;
import com.bouncers.b2b.core.QMessage;
import com.bouncers.b2b.core.QMessage.COMMAND;
import com.bouncers.b2b.core.data.PlayerNode;
import com.bouncers.b2b.core.data.TransferredBook;
import com.bouncers.b2b.librarifier.Book;
import com.bouncers.b2b.librarifier.Library;
import com.bouncers.b2b.librarifier.StuffFile;
import com.bouncers.b2b.util.IOFileManager;
import com.bouncers.b2b.util.IOFileManager.MODE;
import com.bouncers.b2b.util.LoggerUtil;
import com.bouncers.b2b.util.Util;

public class Player implements PlayerPlayerConListener, ConsoleListener{

	public static final long DOWNLOAD_TASK_TIMER = 1 * 30 * 1000; //
	public static final int SOCKET_TIMEOUT = 2*60*1000;
	private MODE mode;
	private int port;
	private String ip;
	private ConsoleThread console;
	private IOFileManager ioManager;
	private ServerSocket serverSocket;
	private List<PlayerPlayerCon> playerCons;
	private DownloadTask downloadTask;
	private String hubIp;
	private int hubPort;
	
	public Player(MODE mode, int port) throws Exception {
		super();
		this.mode = mode;
		this.port = port;
		this.ip = Util.getIpAddress();
		ioManager = new IOFileManager(this.port+"", this.mode);
		console = new ConsoleThread(this.mode);
		console.addConsoleListener(this);
		this.playerCons = new CopyOnWriteArrayList<PlayerPlayerCon>();
		this.downloadTask = new DownloadTask(this.playerCons, ioManager, new ArrayBlockingQueue<QMessage>(512));
	}
	
	public void startPlayer(){
		console.start();
		if(ioManager.getLibrariesAsList().size() > 0){
			Library[] librss = ioManager.getLibrariesAsList().toArray(new Library[ioManager.getLibrariesAsList().size()]);
			sendDownloadRequestToHub(this, librss);
		}
		startDownloadTask();
		
		try {
			this.serverSocket = new ServerSocket(port);
			
			while (true) {
				LoggerUtil.info("Player on $$ waiting for a player connection", port);
				Socket socket = this.serverSocket.accept();
				socket.setSoTimeout(SOCKET_TIMEOUT);// connected players should send his port in 120 second
				synchronized (socket) {
					LoggerUtil.info("A Player on $$ $$ is connected now", socket.getInetAddress().getHostAddress(), socket.getPort());
					
					QMessage<String> msg = new ObjectMapper().readValue(new DataInputStream(socket.getInputStream()).readUTF(), new TypeReference<QMessage<String>>() {});
					if(msg.getCommand() == COMMAND.PLAYER_CONNECTION){
						PlayerPlayerCon playerCon = new PlayerPlayerCon(new ArrayBlockingQueue<QMessage>(256), socket, Integer.valueOf(msg.getObject().toString()), mode);
						playerCon.addPlayerPlayerConListener(this);
						playerCon.start();
						this.playerCons.add(playerCon);
						System.out.println("NUMBER OF PLAYER CONNECTIONS : " + this.playerCons.size());
					}
				}
			}
			
		} catch (Exception e) {
			LoggerUtil.error("startPlayer ERROR " , e);
		}
	}

	public static void main(String[] args) throws Exception {
		int modeCode;
		try {
			modeCode = Integer.valueOf(args[0]);
			if(modeCode == 0 || !(modeCode == 1 || modeCode == 2))
				modeCode = 1;
		} catch (NumberFormatException e) {
			modeCode = 1;
		}
		
		MODE mode = MODE.values()[modeCode-1];
		int port = Integer.valueOf(args[1]);
		
		Player player = new Player(mode, port);
		player.startPlayer();
		
	}
	
	private void startDownloadTask(){
		LoggerUtil.info("++ Player Download Task starting");
		Timer timer = new Timer("downloadTask");
		timer.schedule(downloadTask, new Date(), DOWNLOAD_TASK_TIMER);
		
	} 

	private void sendDownloadRequestToHub(final Player plyr, final Library...librs){
		new Thread(new Runnable() {
			public void run() {
				if(librs == null || librs.length == 0)
					return;
				
				for (Library library : librs) {
					try {
						Socket hubSocket = new Socket(library.getHubIp(), library.getHubPort());
						QMessage<String> msg = new QMessage<String>(library.isExistOnDisk() ? COMMAND.UPLOADING : COMMAND.DOWNLOADING, 
								port, library.getName());
						new DataOutputStream(hubSocket.getOutputStream()).writeUTF(new ObjectMapper().writeValueAsString(msg));
						if(msg.getCommand() == COMMAND.UPLOADING)
							continue;
						
						QMessage<List<PlayerNode>> msgRec = new ObjectMapper().readValue(new DataInputStream(hubSocket.getInputStream()).readUTF(),
								new TypeReference<QMessage<List<PlayerNode>>>() {});
						
						List<PlayerNode> players = msgRec.getObject();
						
						if(players != null){
							for (PlayerNode playerNode : players) {
								if(playerNode.getIp().equals(ip) && playerNode.getPort() == port)
									continue;
								for (Iterator<PlayerPlayerCon> iterator = playerCons.iterator(); iterator.hasNext();) {
									PlayerPlayerCon playerPlayerCon = iterator.next();
									if(!playerPlayerCon.getUniqueName().equals(playerNode.getUniqueName())){
										Socket socket = new Socket(playerNode.getIp(), playerNode.getPort());
										socket.setSoTimeout(SOCKET_TIMEOUT);
										PlayerPlayerCon pCon = new PlayerPlayerCon(new ArrayBlockingQueue<QMessage>(256), socket, playerNode.getPort(), mode);
										pCon.addPlayerPlayerConListener(plyr);
										pCon.start();
										playerCons.add(pCon);
										System.out.println("NUMBER OF PLAYER CONNECTIONS : " + playerCons.size());
										pCon.getQueue().add(new QMessage<String>(COMMAND.PLAYER_CONNECTION, port+""));
									}
								}
								
								
								
								if(playerCons.size() == 0){
									if(playerNode.getIp().equals(ip) && playerNode.getPort() == port)
										continue;
									Socket socket = new Socket(playerNode.getIp(), playerNode.getPort());
									socket.setSoTimeout(SOCKET_TIMEOUT);
									PlayerPlayerCon pCon = new PlayerPlayerCon(new ArrayBlockingQueue<QMessage>(256), socket, playerNode.getPort(), mode);
									pCon.addPlayerPlayerConListener(plyr);
									pCon.start();
									playerCons.add(pCon);
									System.out.println("NUMBER OF PLAYER CONNECTIONS : " + playerCons.size());
									pCon.getQueue().add(new QMessage<String>(COMMAND.PLAYER_CONNECTION, port+""));
								}
							}
						}
						hubSocket.close();
					} catch (Exception e) {
						LoggerUtil.error("sendDownloadRequestToHub ERROR : ", e);
					}
				}
			}
		}, "HubDownloadRequestThread").start();
		
	}
	
	public void exit() {
		console.killConnection();
		for (PlayerPlayerCon playerPlayerCon : playerCons) {
			playerPlayerCon.killConnection();
		}
		System.exit(0);
	}

	public synchronized void uploding(Library libr) throws IOException {
		
		if(ioManager.isAvailableForUploadDownload()){
			libr.setExistOnDisk(true);
			libr.setAmIInitialSharer(true);

			if(ioManager.addANewLibrary(libr)){
				if(mode == MODE.ONE){
					this.hubIp = libr.getHubIp();
					this.hubPort = libr.getHubPort();
				}
				Socket hubsocket = new Socket(libr.getHubIp(), libr.getHubPort());
				QMessage<String> msg = new QMessage<String>(COMMAND.UPLOADING, port, libr.getName());
				new DataOutputStream(hubsocket.getOutputStream()).writeUTF(new ObjectMapper().writeValueAsString(msg));
				hubsocket.close();
			} else {
				LoggerUtil.error("Could not add the file to ioManager. Try again");
			}
			
		} else {
			LoggerUtil.error("This player is not available to upload file");
		}
	}

	public synchronized void downloading(Library libr) throws IOException {
		if(ioManager.isAvailableForUploadDownload()){
			libr.setExistOnDisk(false);
			libr.setAmIInitialSharer(false);
			if(ioManager.addANewLibrary(libr)){
				if(mode == MODE.ONE){
					this.hubIp = libr.getHubIp();
					this.hubPort = libr.getHubPort();
				}
				sendDownloadRequestToHub(this, libr);
			} else {
				LoggerUtil.error("Could not add the file to ioManager. Try again");
			}
		} else {
			LoggerUtil.error("This player is not available to download file");
		}
	}

	public synchronized void askingAvailableListOfBook(PlayerPlayerCon playerCon, List<String> librNames) {
		Map<String, Map<String, long[]>> data = new HashMap<String, Map<String,long[]>>();
		for (Library librs : ioManager.getLibrariesAsList()) {
			if(librNames.contains(librs.getName())){
				data.put(librs.getName(), librs.getAvailableBooksListInMap());
			}
		}
		
		playerCon.getQueue().add(new QMessage<Map<String, Map<String, long[]>>>(COMMAND.ANSWER_OF_AVA_BOOKS, data));
	}

	public synchronized void askingForBookData(PlayerPlayerCon playerCon, Map<String, Map<String, long[]>> askedBookList) {
		
		for (Map.Entry<String, Map<String, long[]>> entry : askedBookList.entrySet()) {
			String librName = entry.getKey();
			Map<String, long[]> fileBookMap = entry.getValue();
			Library libr = ioManager.getLibraryByName(librName);
			if(libr != null){
				for(Map.Entry<String, long[]> entry2 : fileBookMap.entrySet()){
					String fileName = entry2.getKey();
					StuffFile stfFile = libr.getStufFileByName(fileName);
					if(stfFile != null){
						for (long bkNumber : entry2.getValue()) {
							for(Book bkk : stfFile.getAvailableBooks()){
								if(bkk.getIndex() == bkNumber){
									try {
										TransferredBook trfBk = ioManager.getBooksData(librName, fileName, bkNumber);
										QMessage<TransferredBook> msg = new QMessage<TransferredBook>(COMMAND.ANSWER_BOOK_DATA, trfBk);
										playerCon.getQueue().add(msg);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public synchronized void receivedAvailableListOfBooks(PlayerPlayerCon playerCon, Map<String, Map<String, long[]>> availableBooks) {
		
	}

	public synchronized void receivedBookData(PlayerPlayerCon playerCon, TransferredBook tbk) {
		try {
			ioManager.writeBookData(tbk);
		} catch (Exception e) {
			LoggerUtil.error("WriteBook error", e);
		}
	}

	public synchronized void complainToHub(PlayerPlayerCon playerCon) {
		LoggerUtil.info("Player on $$ is not working. complain to hub");
		if(mode == MODE.ONE){
			Socket hubsocket;
			try {
				hubsocket = new Socket(this.hubIp, this.hubPort);
				QMessage<PlayerNode> msg = new QMessage<PlayerNode>(COMMAND.PLAYER_COMPLAIN, port, new PlayerNode(this.hubIp, this.hubPort, null));
				new DataOutputStream(hubsocket.getOutputStream()).writeUTF(new ObjectMapper().writeValueAsString(msg));
				hubsocket.close();
			}  catch (Exception e) {
				LoggerUtil.error("Player complain error : ", e);
			}
			
		}
	}

	public void disconnectPlayer(PlayerPlayerCon playerCon) {
		playerCons.remove(playerCon);
	}

	public void askHubForNewPlayers(List<Library> libraries) {
		LoggerUtil.info("Asking Hub for new players");
		sendDownloadRequestToHub(this, libraries.toArray(new Library[libraries.size()]));
	}
	
}
