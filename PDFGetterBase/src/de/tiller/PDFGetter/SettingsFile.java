package de.tiller.PDFGetter;

import java.io.Serializable;
import java.util.List;

public class SettingsFile implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public List<Settings> settings;
	public boolean update;
	
	public SettingsFile(List<Settings> settings2, boolean b) {
		settings = settings2;
		update = b;
	}
}