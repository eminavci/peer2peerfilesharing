package com.bouncers.b2b.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Stores (static) logging-related functions for the PeerBase system.
 * @author nhamid
 *
 */
public class LoggerUtil {

	public static final String LOGGERNAME = "p2p_mldm.logging";
	public static final String DELIMITER = "\\$\\$";
	private static final DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
	static {
		Logger.getLogger(LOGGERNAME).setUseParentHandlers(false);
		
		Formatter formatter = new Formatter() {
			
			@Override
			public String format(LogRecord record) {
				StringBuilder builder = new StringBuilder(1000);
				builder.append(record.getLevel().toString().charAt(0)).append(" ");
				builder.append(df.format(new Date(record.getMillis())));
				builder.append(this.formatThreadId(Long.toString(Thread.currentThread().getId()))).append(" - ");
				builder.append(formatMessage(record));
		        builder.append("\r\n");
		        return builder.toString();
			}
			
			private String formatThreadId(String str){
				if(str == null || str.length() == 0)
					return "[    ]";
				if(str.length() < 5){
					return "[" + String.format("%"+ (str.length() - 4) +"s", "") + str + "]";
				}
				return "[" + str + "]";
			}
		};
		
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.FINE);
		handler.setFormatter(formatter);
		Logger.getLogger(LOGGERNAME).addHandler(handler);
	}
	
	public static void setHandlersLevel( Level level ) {
		Handler[] handlers = 
			Logger.getLogger( LOGGERNAME ).getHandlers();
		for (Handler h : handlers) 
			h.setLevel(level);
		
		Logger.getLogger( LOGGERNAME ).setLevel(level);
	}
	
	public static Logger getLogger() {
		return Logger.getLogger(LOGGERNAME);
	}
	
	public static void info(String msg, Object...pars){
		getLogger().log(Level.INFO, getMsg(msg, pars));
	}
	
	@Deprecated
	public static void debug(String msg, Object...pars){
		getLogger().log(Level.FINE, getMsg(msg, pars));
	}
	
	public static void warning(String msg, Object...pars){
		getLogger().log(Level.WARNING, getMsg(msg, pars));
	}
	
	public static void error(String msg, Object...pars){
		getLogger().log(Level.SEVERE, getMsg(msg, pars));
	}
	
	public static void error(String msg, Throwable ex, Object...pars){
		getLogger().log(Level.SEVERE, getMsg(msg, pars));
		
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));

		getLogger().log(Level.SEVERE, sw.toString());
	}
	
	public static void error(Throwable ex){
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));

		getLogger().log(Level.SEVERE, sw.toString());
	}
	
	private static String getMsg(String msg, Object...pars){
		if(pars != null){
			for (Object object : pars) {
				msg = msg.replaceFirst(DELIMITER, object.toString());
			}
		}
		return msg;
	}
	
	
}
