package com.bouncers.b2b.util;

import java.nio.charset.Charset;


public class Consts {
	public static final boolean IS_TEST = true;
	
	public static final String currIP = "127.0.0.1";
	public static final long BOOK_SIZE = 256;
	public static final String EXTENSION = "libr";
	public static final String DEFAULT_ENCODING = "UTF8";
    public static final String BYTE_ENCODING = "ISO-8859-1";
    public static Charset BYTE_CHARSET;
    public static Charset DEFAULT_CHARSET;

    static {
        try {

            BYTE_CHARSET = Charset.forName(Consts.BYTE_ENCODING);
            DEFAULT_CHARSET = Charset.forName(Consts.DEFAULT_ENCODING);

        } catch (Throwable e) {

            e.printStackTrace();
        }
    }
    public static String SAVEPATH = "Downloads/";
}
