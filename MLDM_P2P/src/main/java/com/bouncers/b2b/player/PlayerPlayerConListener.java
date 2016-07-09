package com.bouncers.b2b.player;

import java.util.EventListener;
import java.util.List;
import java.util.Map;

import com.bouncers.b2b.core.data.TransferredBook;
import com.bouncers.b2b.librarifier.Library;

public interface PlayerPlayerConListener extends EventListener{

	public void askingAvailableListOfBook(PlayerPlayerCon playerCon, List<String> librNames);
	public void receivedAvailableListOfBooks(PlayerPlayerCon playerCon, Map<String, Map<String, long[]>> availableBooks);
	
	public void askingForBookData(PlayerPlayerCon playerCon, Map<String, Map<String, long[]>> askedBookList);
	public void receivedBookData(PlayerPlayerCon playerCon, TransferredBook tbk);
	
	public void complainToHub(PlayerPlayerCon playerCon);
	public void disconnectPlayer(PlayerPlayerCon playerCon);
	
	public void askHubForNewPlayers(List<Library> libraries);
	
}
