package com.bouncers.b2b.player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import javax.swing.event.EventListenerList;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.bouncers.b2b.core.QMessage;
import com.bouncers.b2b.core.QMessage.COMMAND;
import com.bouncers.b2b.core.data.TransferredBook;
import com.bouncers.b2b.util.IOFileManager.MODE;
import com.bouncers.b2b.util.LoggerUtil;

public class PlayerPlayerCon extends Thread{

	public enum STATUS{
		IDLE,
		DOWNLOADING,
		UPLOADING,
	}
	
	private final EventListenerList listeners = new EventListenerList();
	private String ip;
	private int port;
	private boolean running;
	private BlockingQueue<QMessage> queue;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Map<String, Map<String, long[]>> availableBooks;
	private ObjectMapper mapper;
	private MODE mode;
	private STATUS status; 
	private final static int COMPLAIN_TRESH = 5; 
	
	public PlayerPlayerCon(BlockingQueue<QMessage> queue, Socket socket, int port, MODE mode) {
		super();
		this.queue = queue;
		this.socket = socket;
		this.mode = mode;
		this.ip = socket.getInetAddress().getHostAddress();
		this.port = port;
		mapper = new ObjectMapper();
		status = STATUS.IDLE;
		this.availableBooks = new HashMap<String, Map<String,long[]>>();
	}

	@Override
	public void run() {
		this.running = true;
		try {
			this.dos = new DataOutputStream(this.socket.getOutputStream());
			this.dis = new DataInputStream(this.socket.getInputStream());
			
			///this.dos.writeUTF(mapper.writeValueAsString(new QMessage<String>(COMMAND.PLAYER_CONNECTION, "PORT")));
		} catch (IOException e) {
			LoggerUtil.error("İşte Hata", e);
			killConnection();
		}
		int complaintTime = 0;
		
		listenForTheQueue();
		while (running) {
			try {
				String str = this.dis.readUTF();
				complaintTime = 0;
				QMessage msg = mapper.readValue(str, QMessage.class);
				//LoggerUtil.info("%%%% GELEN KOMUT : " + msg.getCommand() +  " " + str);
				switch (msg.getCommand()) {
				case REQUEST_AVAILABLE_BOOKS:
					msg = mapper.readValue(str, new TypeReference<QMessage<List<String>>>() {});
					List<String> librNames = (List<String>) msg.getObject();
					triggerRequestForAvailableBooksEvent(librNames);
					break;
				case RESPONSE_AVAILABLE_BOOKS:
					msg = mapper.readValue(str, new TypeReference<QMessage<Map<String, Map<String, long[]>>>>() {});
					availableBooks = (Map<String, Map<String, long[]>>) msg.getObject();
					triggerReceivedAvailableListOfBooksEvent(availableBooks);
					break;
				case DOWNLOADING_BOOK:
					status = STATUS.DOWNLOADING;
					msg = mapper.readValue(str, new TypeReference<QMessage<Map<String, Map<String, long[]>>>>() {});
					triggerAskingForBookDataEvent((Map<String, Map<String, long[]>>) msg.getObject());
					break;
				case UPLOADING_BOOK:
					this.status = STATUS.UPLOADING;
					msg = mapper.readValue(str, new TypeReference<QMessage<TransferredBook>>() {});
					TransferredBook tbk = (TransferredBook) msg.getObject();
					byte[] bookData = new byte[(int) tbk.getSize()];

					System.out.println("Book received " + getUniqueName() +" " + tbk.getBookNumber() + " " + tbk.getFileName());
					
					this.dis.readFully(bookData);
					tbk.setData(bookData);
					triggerReceivedBookDataEvent(tbk);
					
					break;
				default:
					break;
				}
				
			} catch (SocketTimeoutException e) {
				LoggerUtil.error("SocketTimeout oldu", e); // 3 times happer respectively, close tis connection and complain to  Hub
				complaintTime++;
				if(complaintTime == COMPLAIN_TRESH){
					killConnection();
				}
			} catch (EOFException e) {
				LoggerUtil.error("Disconnection oldu", e); // 3 times happer respectively, close tis connection and complain to  Hub
				complaintTime++;
				if(complaintTime == COMPLAIN_TRESH){
					killConnection();
				}
			}catch (Exception e) {
				e.printStackTrace();
				complaintTime++;
				if(complaintTime == COMPLAIN_TRESH + 3){
					killConnection();
				}
			}
		}
	}
	
	
	private void listenForTheQueue() {
		new Thread(new Runnable() {
			public void run() {
				try {
					while (running) {
						QMessage msg = queue.take();
						QMessage sendingMsg;
						switch (msg.getCommand()) {
						case ASK_LIST_OF_AVA_BOOKS:
							List<String> librNames = (List<String>) msg.getObject();
							
							sendingMsg = new QMessage<List<String>>(COMMAND.REQUEST_AVAILABLE_BOOKS, librNames);
							dos.writeUTF(mapper.writeValueAsString(sendingMsg));
							
							break;
						case ANSWER_OF_AVA_BOOKS:
							Map<String, Map<String, long[]>> sendData = (Map<String, Map<String, long[]>>) msg.getObject();
							
							sendingMsg = new QMessage<Map<String, Map<String, long[]>>>(COMMAND.RESPONSE_AVAILABLE_BOOKS, sendData);
							dos.writeUTF(mapper.writeValueAsString(sendingMsg));
							break;
							
						case ASK_BOOK_DATA:
							Map<String, Map<String, long[]>> askedBookList = (Map<String, Map<String, long[]>>) msg.getObject();
							if(askedBookList == null) break;
							
							sendingMsg = new QMessage<Map<String, Map<String, long[]>>>(COMMAND.DOWNLOADING_BOOK, askedBookList);
							dos.writeUTF(mapper.writeValueAsString(sendingMsg));
							break;
							
						case ANSWER_BOOK_DATA:
							TransferredBook tbk = (TransferredBook) msg.getObject();
							System.out.println("Book Sending : " + getUniqueName() + " " + tbk.getBookNumber() + " " + tbk.getFileName());
							sendingMsg = new QMessage<TransferredBook>(COMMAND.UPLOADING_BOOK, tbk);
							dos.writeUTF(mapper.writeValueAsString(sendingMsg));
							
							dos.write(tbk.getData());
							break;
						case PLAYER_CONNECTION:
							dos.writeUTF(mapper.writeValueAsString(msg));
							break;
						default:
							break;
						}
					}
				} catch (Exception e) {
					
				}
			}
		}, socket.getInetAddress().getHostAddress() + ":" + socket.getPort()).start();
		
	}

	public void killConnection(){
		this.running = false;
		try {
			triggerComplainToHubEvent();
			socket.close();
			this.dis.close();
			this.dos.flush();
			this.dos.close();
			this.socket.close();
		} catch (IOException e) {
			LoggerUtil.error("The player connection $$ $$ enccounter an errror while closing connection");
		} finally {
			triggerPlayerDisconnectEvent();
		}
	}
	
	private void triggerRequestForAvailableBooksEvent(List<String> librNames){
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.askingAvailableListOfBook(this, librNames);
		}
	}
	
	private void triggerReceivedAvailableListOfBooksEvent(Map<String, Map<String, long[]>> availableBooks){
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.receivedAvailableListOfBooks(this, availableBooks);
		}
	}
	
	private void triggerAskingForBookDataEvent(Map<String, Map<String, long[]>> askedBooks){
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.askingForBookData(this, askedBooks);
		}
	}
	
	private void triggerReceivedBookDataEvent(TransferredBook tbk){
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.receivedBookData(this, tbk);
		}
	}
	private void triggerPlayerDisconnectEvent(){
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.disconnectPlayer(this);
		}
	}
	private void triggerComplainToHubEvent(){
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.complainToHub(this);
		}
	}
	
	public void addPlayerPlayerConListener(PlayerPlayerConListener listener){
		this.listeners.add(PlayerPlayerConListener.class, listener);
	}
	public PlayerPlayerConListener[] getPlayerlayerConListener(){
		return listeners.getListeners(PlayerPlayerConListener.class);
	}
	public BlockingQueue<QMessage> getQueue() {
		return queue;
	}
	public boolean isRunning() {
		return running;
	}
	
	private boolean isMode1(){
		return mode == MODE.ONE;
	}
	
	public String getUniqueName(){
		return this.ip + ":" + this.port;
	}

	@Override
	public boolean equals(Object obj) {
		PlayerPlayerCon con = (PlayerPlayerCon) obj;
		return con.getUniqueName().equals(this.getUniqueName());
	}
	
	public int getAvaBookCountOfLibr(String librName) {
		int count = 0;
		for (Map.Entry<String, long[]> entry : this.availableBooks.get(librName).entrySet()) {
			count += entry.getValue().length;
		}
		return count;
	}
	
	public Map<String, long[]> getAvaBookOfLibr(String librName){
		return this.availableBooks.get(librName);
	}
	public STATUS getStatus() {
		return status;
	}
	public void setStatus(STATUS status) {
		this.status = status;
	}
}
