package com.bouncers.b2b.core;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.event.EventListenerList;

import com.bouncers.b2b.core.data.PlayerNode;
import com.bouncers.b2b.util.LibraryBean;
import com.bouncers.b2b.util.LoggerUtil;

public class PlayerValidityCheck extends TimerTask{

	private boolean running;
	private Map<String, PlayerNode> players;
	private transient final EventListenerList listeners = new EventListenerList();

	public PlayerValidityCheck(Map<String, PlayerNode> players) {
		super();
		this.players = players;
	}

	@Override
	public void run() {
		running = true;
		LoggerUtil.info("HubServer player validity check started..................");
		
		for (Map.Entry<String, PlayerNode> entry : players.entrySet()){
			PlayerNode player = entry.getValue();
			try{
		        Socket ss = new Socket();
		        ss.connect(new InetSocketAddress(player.getIp(), player.getPort()), 1);
		        ss.close();
		    }catch(Exception e) {
		    	LoggerUtil.info("** Player on $$ $$ is not alive anymore", player.getIp(), player.getPort());
		        players.remove(player);
		    }
		}
		
		running = false;
	}

	public void killConnection(){
		LoggerUtil.info("HubServer Player validity checking is stopped");
		this.running = false;
	}
}
