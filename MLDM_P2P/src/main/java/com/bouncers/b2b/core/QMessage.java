package com.bouncers.b2b.core;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.bouncers.b2b.util.Util;


public class QMessage<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum COMMAND{
		EXIT(1),
		DOWNLOADING(2),
		UPLOADING(3),
		// response players list
		HUB_RESPONSE(4),
		ASK_LIST_OF_AVA_BOOKS(5),
		ANSWER_OF_AVA_BOOKS(6),
		ASK_BOOK_DATA(7),
		ANSWER_BOOK_DATA(8),
		PLAYER_COMPLAIN(9),
		
		REQUEST_AVAILABLE_BOOKS(20),
		RESPONSE_AVAILABLE_BOOKS(21),
		DOWNLOADING_BOOK(22),
		UPLOADING_BOOK(23),
		PLAYER_CONNECTION(25);
		
		final int commandCode;
		
		COMMAND(int commandCode){
			this.commandCode = commandCode;
		}
		@JsonIgnore
		public int getCommandCode() {
			return commandCode;
		}
	}
	
	private COMMAND command;
	private T object;
	private int port;
	public QMessage() {}
	
	public QMessage(COMMAND command, T object) {
		super();
		this.command = command;
		this.object = object;
		this.port = 0;
	}

	public QMessage(COMMAND command, int port, T object) {
		super();
		this.command = command;
		this.object = object;
		this.port = port;
	}
	
	public COMMAND getCommand() {
		return command;
	}
	public void setCommand(COMMAND command) {
		this.command = command;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public T getObject() {
		return object;
	}
	public void setObject(T object) {
		this.object = object;
	}
	
	@JsonIgnore
	public String getMsgString() throws JsonGenerationException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writer(Util.getFullFilter()).writeValueAsString(this.getObject());
	}

	@Override
	public String toString() {
		try {
			return "QMessage [komut=" + command + ", msg=" + getMsgString() + "]";
		} catch (Exception e) {
			return "QMessage [komut=" + command + ", msg=" + object + "]";
		} 
	}
	
	

}
