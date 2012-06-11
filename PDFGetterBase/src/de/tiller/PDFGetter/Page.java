package de.tiller.PDFGetter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Page implements Serializable {
	public static interface Base64Encoder {
		public String encode(String in);
	}
	
	public static class Update {
		public UpdateType type;
		public File file;
	}
	
	public static class MagicResults {
		public Throwable[] thrown;
		public Update[] updates;
	}
	
	public enum UpdateType {
		isNew,
		wasModified
	}
	
	public static int[] done = new int[] { 0 };
	public static int[] downloadedBytes = new int[] { 0 };
	public static int[] downloadedFiles = new int[] { 0 };
	
	public static Base64Encoder encoder = null;
	
	private static final long serialVersionUID = 1L;
	public static int[] spotted = new int[] { 0 };

	public static boolean updateAllFiles = false;
	public static String download(URL URL, String userpwd, File localTarget) throws IOException, AutorizationException {
		OutputStream out;
		
		InputStream stream = tryPasswords(URL, userpwd).getInputStream();
		
		if(localTarget != null)
			out = new FileOutputStream(localTarget);
		else
			out = new ByteArrayOutputStream(64 * 1024);
		
		int streamed = stream(stream, out, 64 * 1024);
		
		synchronized(downloadedBytes) {
			downloadedBytes[0]+=streamed;
		}
		
		if(localTarget != null) {
			out.close();
			return null;
		} else
			return ((ByteArrayOutputStream) out).toString();
		
	}
	
	public static MagicResults execute(Page[] pages, boolean parallel) {
		downloadedBytes[0] = 0;
		downloadedFiles[0] = 0;
		spotted[0] = 0;
		done[0] = 0;
		
		ExecutorService exec = parallel ? Executors.newCachedThreadPool() : Executors.newSingleThreadExecutor();
		final ExecutorService exec2 = parallel ? exec : Executors.newSingleThreadExecutor();
		final Vector<Throwable> thrown = new Vector<Throwable>();
		final Vector<Update> updates = new Vector<Update>();
		
		final CountDownLatch latch = new CountDownLatch(pages.length);
		
		for(final Page p : pages) {
			exec.execute(new Runnable() {
				public void run() {
					try {
						p.workTheMagic(exec2, thrown, updates);
						synchronized(done) {
							done[0]++;
						}
					} catch(Throwable t) {
						thrown.add(t);
					} finally {
						latch.countDown();
					}
				}
			});
			synchronized (spotted) {
				spotted[0]++;
			}
		}
		
		try {
			latch.await();
			exec.shutdown();
			if(!parallel)
				exec2.shutdown();
			exec.awaitTermination(86400, TimeUnit.SECONDS);
			if(!parallel)
				exec2.awaitTermination(86400, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		MagicResults ret = new MagicResults();
		ret.thrown = thrown.toArray(new Throwable[thrown.size()]);
		ret.updates = updates.toArray(new Update[updates.size()]);
		
		return ret;
	}
	
	/**
	 * 
	 * @param URL the URL to be downloaded
	 * @param password to be tried
	 * @return the InputStream to that URL if successful
	 */
	public static HttpURLConnection getConnectedConnection(URL URL, String userpwd) throws IOException, AutorizationException {
		HttpURLConnection con = (HttpURLConnection) URL.openConnection();
		con.setRequestMethod("GET");
		if(userpwd != null) {
			con.setRequestProperty("Authorization", "Basic " + encoder.encode(userpwd));
		} if(con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			if(con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
				throw new AutorizationException(URL);
			else
				throw new IOException("Request for " + URL.toString() + " returned " + con.getResponseCode() + ": " + con.getResponseMessage());
		}
		return con;
	}
	
	public static void put(File parent, HashMap<String, File> map) {
		File[] files = parent.listFiles();
		if(files == null)
			return;
		for(File f : files) {
			String name = f.getName().toLowerCase();
			String ext = "";
			
			if(name.indexOf(".") >= 0) {
				ext = name.substring(name.lastIndexOf("."));
				name = name.substring(0, name.lastIndexOf("."));
			}

			while(name.matches(".*\\[[^\\]]*\\].*"))
				name = name.replaceAll("\\[[^\\]]*\\]", "");
			
			if(f.isFile()) {
				map.put(name + ext, f);
			} else {
				put(f, map);
			}
		}
	}

	public static int stream(InputStream in, OutputStream out, int bufsize) throws IOException {
		int ret = 0;
		byte[] buf = new byte[bufsize];
		for(int len = 0; (len = in.read(buf)) > 0; ret += len)
			out.write(buf, 0, len);
		
		return ret;
	}
	
	public static HttpURLConnection tryPasswords(URL URL, String userpwd) throws MalformedURLException, IOException, AutorizationException {
		try {
			return getConnectedConnection(URL, null);
		} catch (AutorizationException e) {
			// ok, continue;
		}
		
		if(userpwd != null && !"".equals(userpwd)) {
			BufferedReader sr = new BufferedReader(new StringReader(userpwd));
			String line;
			while((line = sr.readLine()) != null) {
				if("".equals(line))
					continue;
				
				try {
					return getConnectedConnection(URL, line);
				} catch (AutorizationException e) {
					// ok, keep trying;
				}
			}
		}
		
		throw new AutorizationException(URL);
	}
	
	Pattern pattern = Pattern.compile(".*\\.pdf", Pattern.CASE_INSENSITIVE);
	
	File targetLocation = null;
	
	URL URL = null;
	
	String userpwd = null;
	
	public Page() {
	}
	
	public Page(URL URL, File targetLocation, String userpwd) {
		this.URL = URL;
		this.targetLocation = targetLocation;
		this.userpwd = userpwd;
	}
	
	public void workTheMagic(ExecutorService exec, final Vector<Throwable> thrown, final Vector<Update> updates) {
		String inputHTML;
		
		try {
			inputHTML = download(URL, userpwd, null);
		} catch (Exception e1) {
			thrown.add(e1);
			return;
		}
		
		// clean comments
		inputHTML = inputHTML.replaceAll("<!--.*?-->", "");
		Matcher comment = Pattern.compile("<!--.*?-->", Pattern.DOTALL).matcher(inputHTML);
		while(comment.find())
			inputHTML = inputHTML.substring(0, inputHTML.indexOf(comment.group(0))) + inputHTML.substring(inputHTML.indexOf(comment.group(0)) + comment.group(0).length());
		
		Pattern p = Pattern.compile("<a[^>]+href=[\"]?([^\"> ]+)[\"]?[^>]*>([^<]+)</a>");
		Matcher m = p.matcher(inputHTML);
		
		if(!targetLocation.exists())
			if(!targetLocation.mkdirs()) {
				thrown.add(new Exception("Could not create target Folder: " + targetLocation.getAbsolutePath()));
				return;
			}
		
		HashMap<String, File> map = new HashMap<String, File>();
		put(targetLocation, map);
		
		while(m.find()) {
			
			String abstractURL = m.group(1);
			//System.out.println(abstractURL);
			if(!pattern.matcher(abstractURL).matches())
				continue;
			
			URL fileURL;
			
			try {
				fileURL = new URL(URL, abstractURL);
			} catch(MalformedURLException e) {
				thrown.add(e);
				continue;
			}
			
			final Update update = new Update();
			
			final URL u = fileURL;
			File targetFile = map.get(new File(fileURL.getPath()).getName().toLowerCase());
			if(targetFile == null) {
				targetFile = new File(targetLocation, new File(fileURL.getPath()).getName());
				update.type = UpdateType.isNew;
			} else if(!updateAllFiles) {
				// so the file exists and we don't want to update all files
				// let's check, if the file sizes differ!
				
				try {
					int length = tryPasswords(fileURL, userpwd).getContentLength();
					
					if(length == 0 || length == targetFile.length())
						continue;
					
					update.type = UpdateType.wasModified;
				} catch(Exception e) {
					thrown.add(e);
					continue;
				}
			}
			
			final File f = targetFile;
			update.file = f;
			
			synchronized (spotted) {
				spotted[0]++;
			}
			
			exec.execute(new Runnable() {
				public void run() {
					try {
						download(u, userpwd, f);
						synchronized (downloadedFiles) {
							downloadedFiles[0]++;
						}
						updates.add(update);
					} catch (Exception e) {
						thrown.add(e);
					} finally {
						synchronized(done) {
							done[0]++;
						}
					}
				}
			});
		}
	}
}