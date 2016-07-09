package com.bouncers.b2b.core.data;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class PlayerNode {
	private String ip;
	private int port;
	private  List<String> libraries; // librNAme:{fileNAme, [books]}
	public PlayerNode() {}

	public PlayerNode(String ip, int port, List<String> libraries) {
		super();
		this.ip = ip;
		this.port = port;
		this.libraries = libraries;
	}

	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<String> getLibraries() {
		if(this.libraries == null)
			libraries = new ArrayList<String>();
		return libraries;
	}
	public void setLibraries(List<String> libraries) {
		this.libraries = libraries;
	}

	@JsonIgnore
	public String getUniqueName(){
		return this.ip + ":" + this.getPort();
	}

	@Override
	@JsonIgnore
	public boolean equals(Object obj) {
		if(obj instanceof PlayerNode){
			PlayerNode pn = (PlayerNode) obj;
			return pn.equals(this.getIp()) && pn.getPort() == this.getPort();
		} else
			return false;
	}
	
	
	
}
