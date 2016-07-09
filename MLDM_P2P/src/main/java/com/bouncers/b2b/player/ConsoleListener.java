package com.bouncers.b2b.player;

import java.io.IOException;
import java.util.EventListener;

import com.bouncers.b2b.librarifier.Library;

public interface ConsoleListener extends EventListener{

	public void exit();
	public void uploding(Library libr) throws IOException;
	public void downloading(Library libr) throws IOException;
	
}
