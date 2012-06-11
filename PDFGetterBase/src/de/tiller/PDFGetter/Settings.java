package de.tiller.PDFGetter;

import java.io.Serializable;

public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;
	
	Page page = null;
	String name = "";
	boolean download = false;
}

