package com.bouncers.b2b.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

import javax.swing.event.EventListenerList;

import com.bouncers.b2b.core.QMessage.COMMAND;
import com.bouncers.b2b.librarifier.Library;
import com.bouncers.b2b.player.PlayerPlayerCon;
import com.bouncers.b2b.player.PlayerPlayerConListener;
import com.bouncers.b2b.player.PlayerPlayerCon.STATUS;
import com.bouncers.b2b.util.IOFileManager;
import com.bouncers.b2b.util.LibraryBean;
import com.bouncers.b2b.util.LoggerUtil;

public class DownloadTask extends TimerTask{
	private final EventListenerList listeners = new EventListenerList();
	private boolean running = false;
	private List<PlayerPlayerCon> playerCons;
	private IOFileManager ioManager;
	private BlockingQueue<QMessage> queue;
	private int askHubForNewPlayerCount = 0;
	
	public DownloadTask(List<PlayerPlayerCon> playerCons, IOFileManager ioManager, BlockingQueue<QMessage> queue) {
		super();
		this.playerCons = playerCons;
		this.ioManager = ioManager;
		this.queue = queue;
	}

	@Override
	public void run() {
		running = true;
		listenForQueue();
		askHubForNewPlayerCount++;
		if(askHubForNewPlayerCount > 5){
			askHubForNewPlayerCount = 0;
			askHubNewPlayers();
		}
		LoggerUtil.info("Download Task started...");
		for (Map.Entry<String, LibraryBean> librEntry : ioManager.getLibrBeans().entrySet()) {
			String librName = librEntry.getKey();
			LibraryBean librBean = librEntry.getValue();
			Library library = librBean.getLibrary();
			
			if(!library.isExistOnDisk()){
				Map<String, long[]> unAvaBooks = library.getUnAvailableBooksListInMap();
				
				Iterator<PlayerPlayerCon> iter = playerCons.iterator();
				while (iter.hasNext()) {
					PlayerPlayerCon playerPlayerCon = (PlayerPlayerCon) iter.next();
					Map<String, long[]> avaBooks = playerPlayerCon.getAvaBookOfLibr(librName);
					if(playerPlayerCon.getStatus() != STATUS.DOWNLOADING){
						if(avaBooks != null){
							Map<String, Map<String, long[]>> askingBookList = new HashMap<String, Map<String,long[]>>();
							Map<String, long[]> stfMap = new HashMap<String, long[]>();
							for(Map.Entry<String, long[]> entr : unAvaBooks.entrySet() ){
								stfMap.put(entr.getKey(), getRandom5Elem(entr.getValue()));
								
								// TODO remove the the books you assigned to previous Player for Download
							}
							askingBookList.put(librName, stfMap);
							
							QMessage<Map<String, Map<String, long[]>>> msg = new QMessage<Map<String,Map<String,long[]>>>(COMMAND.ASK_BOOK_DATA, askingBookList);
							playerPlayerCon.getQueue().add(msg);
						} else {
							List<String> librNames = new ArrayList<String>();
							librNames.add(librName);
							QMessage<List<String>> msg = new QMessage<List<String>>(COMMAND.ASK_LIST_OF_AVA_BOOKS, librNames);
							playerPlayerCon.getQueue().add(msg);
						}
					}
				}
				
			}
		}
		running = false;
	}

	private void askHubNewPlayers() {
		for (PlayerPlayerConListener listener : getPlayerlayerConListener()) {
			listener.askHubForNewPlayers(ioManager.getUnAvailableLibrs());
		}
	}

	private void listenForQueue() {
		
	}
	
	private long[] getRandom5Elem(long[] bookNumbers){
		Collections.shuffle(Arrays.asList(bookNumbers));
		long[] dd = new long[bookNumbers.length < 5 ? bookNumbers.length : 5];
		for (int i = 0; i < dd.length; i++) {
			dd[i] = bookNumbers[i];
		}
		return dd;
	}

	public boolean isRunning() {
		return running;
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
}
