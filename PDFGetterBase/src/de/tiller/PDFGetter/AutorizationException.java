package de.tiller.PDFGetter;
import java.net.URL;

public class AutorizationException extends Exception {
	private static final long serialVersionUID = 1L;
	private String url;
	
	public AutorizationException(URL url) {
		this.url = url.toString();
	}
	
	@Override
	public String getMessage() {
		return "Could not Authenticate " + url;
	}
}

