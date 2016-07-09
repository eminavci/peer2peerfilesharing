package com.bouncers.b2b.hub;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import com.bouncers.b2b.core.PlayerValidityCheck;
import com.bouncers.b2b.core.QMessage;
import com.bouncers.b2b.core.QMessage.COMMAND;
import com.bouncers.b2b.core.data.PlayerNode;
import com.bouncers.b2b.librarifier.Library;
import com.bouncers.b2b.util.IOFileManager.MODE;
import com.bouncers.b2b.util.LoggerUtil;
import com.bouncers.b2b.util.Util;

public class Hub {
	
	private static long DELAY = 10 * 60 * 1000; // 3 minutes
	private static final int MAX_ASKED_PLAYER = 10;
	private String hubIp;
	private int hubPort;
	private PlayerValidityCheck playerValidity;
	private ServerSocket serverSocket; 
	private Map<String, PlayerNode> players;
	private MODE mode;
	
	public Hub(int hubPort, MODE mode) {
		super();
		this.hubIp = Util.getIpAddress();
		this.hubPort = hubPort;
		this.mode = mode;
		players = new HashMap<String, PlayerNode>();
		playerValidity = new PlayerValidityCheck(players);
	}

	
	private void startHub() throws IOException{
		LoggerUtil.info("HubServer started on $$ $$", hubIp, hubPort);
		startPlayerValidity();
		
		this.serverSocket = new ServerSocket(hubPort);
		while (true) {
			LoggerUtil.info("HubServer on $$ $$ is listening for a player", hubIp, hubPort);
			Socket socket = this.serverSocket.accept();
			LoggerUtil.info("A player on $$ $$ connected to the hub", socket.getInetAddress().getHostAddress(), socket.getPort());
			
			listenPlayer(socket);
		}
		
	}

	private void listenPlayer(final Socket socket) {
		new Thread(new Runnable() {
			public void run() {
				DataOutputStream dos = null;
				DataInputStream dis = null;
				ObjectMapper mapper = new ObjectMapper();
				String ip = socket.getInetAddress().getHostAddress();
				try {
					dos = new DataOutputStream(socket.getOutputStream());
					dis = new DataInputStream(socket.getInputStream());
					
					String str = dis.readUTF();
					
					QMessage msg = mapper.readValue(str, new TypeReference<QMessage>(){});
					
					int port = msg.getPort();
					PlayerNode pNode = players.get(ip + ":" + port);
					
					switch (msg.getCommand()) {
						case DOWNLOADING: // This command has 2 function. introduce player to hub and if already done, it means  asking new players

							if(isHubInMode1()){
								if(pNode == null){
									pNode = new PlayerNode(ip, port, new ArrayList<String>());
									players.put(pNode.getUniqueName(), pNode);
								} else
									LoggerUtil.info("The player on $$ is coming to wrong HUB. Or the player should run on Mode1" , pNode.getUniqueName());
								
								LoggerUtil.info("Hub will give player List to the player on " + pNode.getUniqueName());
								givePlayersToPlayer(dos, null, pNode);
							} else {
								msg = mapper.readValue(str, new TypeReference<QMessage<String>>() {});
								String librName = msg.getObject().toString();
								List<String> librs = new ArrayList<String>();
								librs.add(librName);
								if(pNode == null){
									pNode = new PlayerNode(ip, port, librs);
									players.put(pNode.getUniqueName(), pNode);
								} else if(!pNode.getLibraries().contains(librName))
									pNode.getLibraries().addAll(librs);
								
								LoggerUtil.info("Hub will give player List to the player on " + pNode.getUniqueName());
								givePlayersToPlayer(dos, librName, pNode);
							}
							break;
						case UPLOADING:
							
							if(isHubInMode1()){
								if(pNode == null){
									pNode = new PlayerNode(ip, port, new ArrayList<String>());
									players.put(pNode.getUniqueName(), pNode);
								} else
									LoggerUtil.info("The player on $$ is coming to wrong HUB. Or the player should run on Mode1" , pNode.getUniqueName());
							} else {
								msg = mapper.readValue(str, new TypeReference<QMessage<String>>() {});
								List<String> librs = new ArrayList<String>();
								String librName = msg.getObject().toString();
								librs.add(librName);
								if(pNode == null){
									pNode = new PlayerNode(ip, port, librs);
									players.put(pNode.getUniqueName(), pNode);
								} else if(!pNode.getLibraries().contains(librName))
									pNode.getLibraries().addAll(librs);
							}
							break;
						case PLAYER_COMPLAIN :
							msg = mapper.readValue(str, new TypeReference<QMessage<PlayerNode>>() {});
							PlayerNode plyrNode = (PlayerNode) msg.getObject();
							if(plyrNode != null){
								PlayerNode pFound = null;
								try{
									pFound = players.get(plyrNode.getUniqueName());
									if(pFound == null)
										break;
							        Socket ss = new Socket();
							        ss.connect(new InetSocketAddress(pFound.getIp(), pFound.getPort()), 1);
							        ss.close();
							    }catch(Exception e) {
							    	LoggerUtil.info("** Player on $$ is not alive anymore", pFound.getUniqueName());
							        players.remove(plyrNode);
							    }
							}
							break;
						default:
							break;
					}
					System.out.println("NUMBER OF PLAYERS CONNECTED : " + players.size());
				} catch (Exception e) {
					LoggerUtil.error("Hub Player Listening Error : " , e);
				} finally {
					try {
						if(dis != null) dis.close(); 
						if(dos != null) dos.close();
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			/** Selecting 10 Random player
			 * @param dos
			 * @param librName
			 * @param pNode
			 * @throws JsonGenerationException
			 * @throws JsonMappingException
			 * @throws IOException
			 */
			private synchronized void givePlayersToPlayer(DataOutputStream dos, String librName, PlayerNode pNode) throws JsonGenerationException, JsonMappingException, IOException {
				List<PlayerNode> plyList = new ArrayList<PlayerNode>(players.values());
				if(plyList == null)
					return;
				
				if(librName == null){ //This means its mode-1
					Collections.shuffle(plyList);
					plyList.remove(pNode);// TODO this remove is not working try to remove in loop
					QMessage<List<PlayerNode>> msg = new QMessage<List<PlayerNode>>(COMMAND.HUB_RESPONSE, plyList.subList(0, plyList.size()<= MAX_ASKED_PLAYER ? plyList.size() : MAX_ASKED_PLAYER));
					dos.writeUTF(new ObjectMapper().writer(Util.getFullFilter()).writeValueAsString(msg));
				} else {
					Collections.shuffle(plyList);
					int i = 0;
					List<PlayerNode> pnodes = new ArrayList<>();
					for (Iterator<PlayerNode> iterator = plyList.iterator(); iterator.hasNext();) {
						PlayerNode pn = iterator.next();
						if(pn != null && pn.getLibraries().contains(librName)){
							pnodes.add(pn);
							i++;
							
							if(i == MAX_ASKED_PLAYER)
								break;
						}
					}
					
					QMessage<List<PlayerNode>> msg = new QMessage<List<PlayerNode>>(COMMAND.HUB_RESPONSE, pnodes);
					dos.writeUTF(new ObjectMapper().writer(Util.getFullFilter()).writeValueAsString(msg));
					System.out.println("GIVEN Player List : " + pnodes);
				}		
			}

		}).start();
	}


	private void startPlayerValidity() {
		LoggerUtil.info("HubServer PlayerCheckValidity is starting...");
		Timer timer = new Timer("playervalidity");
		timer.schedule(playerValidity, new Date(), DELAY);
	}



	public static void main(String[] args) throws IOException {

    	int modeCode;
		try {
			modeCode = Integer.valueOf(args[0]);
			if(modeCode == 0 || !(modeCode == 1 || modeCode == 2))
				modeCode = 1;
			
		} catch (NumberFormatException e) {
			modeCode = 1;
		}
		
		MODE mode = MODE.values()[modeCode-1];
		//String ip = args[1];
		int port = Integer.valueOf(args[1]);
		
		Hub hub = new Hub(port, mode);
		hub.startHub();
	}

	private boolean isHubInMode1(){ // Mode 1 is isngle library for hub
		return this.mode == MODE.ONE;
	}
}
