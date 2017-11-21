package de.tiller.PDFGetter;

import java.io.Serializable;
import java.util.List;

public class SettingsFile implements Serializable {
	private static final long serialVersionUID = 1L;

	public List<Settings> settings;
	public boolean update;
	public boolean updateChanged;

	public SettingsFile(List<Settings> settings, boolean update,
			boolean updateChanged) {
		super();
		this.settings = settings;
		this.update = update;
		this.updateChanged = updateChanged;
	}
}