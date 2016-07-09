package com.bouncers.b2b.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;

public class Util {

	private final static Random random = new Random();
	
	public static ArrayList<File> listFilesForFolder(final File folder) {
		ArrayList<File> fileNames = new ArrayList<File>();
		if (folder.isFile()) {
			fileNames.add(folder);
		} else { // it is a file directory then
			for (File fileEntry : folder.listFiles()) {
				fileNames.addAll(listFilesForFolder(fileEntry));
			}
		}
		return fileNames;
	}

	public static String hashShaString(byte[] inputBytes)throws Exception {
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-1");
	        //byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
	        digest.update(inputBytes);
	        		 
	        byte[] hashedBytes = digest.digest();
	        return byteArrayToHexString(hashedBytes);
	    } catch (Exception ex) {
	        throw new Exception("Could not generate hash from String", ex);
	    }
	}
	
	public static String convertByteArrayToHexString(byte[] arrayBytes) {
	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < arrayBytes.length; i++) {
	        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return stringBuffer.toString();
	}

	public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

	public static String onlyOneWhiteSpace(String string){
		return (string != null) ? string.replaceAll("\\s+", " ") : string;
	}
	
	
	public static int getARandomInt(int max){
		return random.nextInt(max) + 0;
	}
	
	/**
     * Convert a byte array integer (4 bytes) to its int value
     * @param b byte[]
     * @return int
     */
    public static int byteArrayToInt(byte[] b) {
        if(b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 |
                    (b[3] & 0xff);
        else if(b.length == 2)
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

        return 0;
    }
    
	public static FilterProvider getFullFilter(){
		FilterProvider fp = new SimpleFilterProvider().addFilter("libraryFilter", 
				SimpleBeanPropertyFilter.serializeAllExcept("onlyPath", "onlyName", 
				"unAvailableBooksListInMap", "availableBooksListInMap", "numOfNonExistFiles", "amIInitialSharer", 
				"existOnDisk")).
				addFilter("stuffFileFilter", SimpleBeanPropertyFilter.serializeAllExcept("existOnDisk",
						"availableBooks", "unAvailableBooks", "onlyName", "realPath")).
				addFilter("bookFilter", SimpleBeanPropertyFilter.serializeAllExcept("existOnDisk"));
		return fp;
	}
	
	public static FilterProvider getEmptyFilter(){
		FilterProvider fp = new SimpleFilterProvider().addFilter("libraryFilter", 
				SimpleBeanPropertyFilter.serializeAllExcept("")).
				addFilter("stuffFileFilter", SimpleBeanPropertyFilter.serializeAllExcept("")).
				addFilter("bookFilter", SimpleBeanPropertyFilter.serializeAllExcept("")); 
		return fp;
	}
/*	
	public static String getIpAddress() throws IOException{
		String command = null;
        if(System.getProperty("os.name").equals("Linux"))
            command = "ifconfig";
        else
            command = "ipconfig";
        Runtime r = Runtime.getRuntime();
        Process p = r.exec(command);
        Scanner s = new Scanner(p.getInputStream());

        StringBuilder sb = new StringBuilder("");
        while(s.hasNext()){
        	String ss = s.next();
        	System.out.println(ss);
        	sb.append(ss);
        }
        Enumeration<NetworkInterface> nInterfaces = NetworkInterface.getNetworkInterfaces();
        String ipconfig = sb.toString();
        Pattern pt = Pattern.compile("192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}");
        Matcher mt = pt.matcher(ipconfig);
        mt.find();
        String ip = mt.group();
		return ip;
	}
	*/
	public static String getIpAddress(){
		String result = null;
		Enumeration<NetworkInterface> interfaces = null;
		try {
		    interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
		    // handle error
		}
		 
		if (interfaces != null) {
		    while (interfaces.hasMoreElements()) {
		        NetworkInterface i = interfaces.nextElement();
		        Enumeration<InetAddress> addresses = i.getInetAddresses();
		        while (addresses.hasMoreElements() && (result == null || result.isEmpty())) {
		            InetAddress address = addresses.nextElement();
		            if (!address.isLoopbackAddress()  &&
		                    address.isSiteLocalAddress()) {
		                result = address.getHostAddress();
		            }
		        }
		    }
		}
		return result;
	}
}
