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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * This is the heart of the PDFGetter. This class represents one HTML web page
 * specified by a HTTP URL (see {@link #setURL(URL)}. The method
 * {@link #workTheMagic(ExecutorService, List, List)} downloads that web page,
 * searches it for links and, if they match a pattern (see
 * {@link #setPattern(Pattern)}, downloads the linked files to a local folder
 * (see {@link #setTargetLocation(File)}. All remote accesses allow password
 * protected access (see {@link #setUserpwd(String)}.
 * 
 * In order to use password protected access, a {@link Base64Encoder} must be
 * available. On a SUN Virtual Machine, it will be retreived automatically, but
 * on other VMs (e.g. Android), it must be set manually (see
 * {@link #setEncoder(Base64Encoder)}.
 * 
 * @author tillmann.gaida@gmail.com
 * 
 */
public class Page implements Serializable {
	private static final long serialVersionUID = 1L;

	public static interface Base64Encoder {
		public String encode(String in);
	}

	public static class Update {
		public UpdateType type;
		public File file;
	}

	public static class MagicResults {
		public final Throwable[] thrown;
		public final Update[] updates;

		public MagicResults(Throwable[] thrown, Update[] updates) {
			this.thrown = thrown;
			this.updates = updates;
		}
	}

	public enum UpdateType {
		isNew, wasModified
	}

	public static final AtomicLong done = new AtomicLong();
	public static final AtomicLong downloadedBytes = new AtomicLong();
	public static final AtomicLong downloadedFiles = new AtomicLong();
	public static final AtomicLong spotted = new AtomicLong();

	private static Base64Encoder encoder = null;

	public static void setEncoder(Base64Encoder encoder) {
		Page.encoder = encoder;
	}

	static {
		try {
			setEncoder(new Base64Encoder() {
				public String encode(String in) {
					return Base64.encode(in.getBytes());
				}
			});
		} catch (Throwable e) {
			// ok, will have to be set manually
		}
	}

	public static boolean updateAllFiles = false;

	/**
	 * Downloads a remote, password protected file. It will either be returned
	 * as a string or saved to a local file.
	 * 
	 * @param URL
	 *            The URL of the remote file that is to be downloaded
	 * @param userpwd
	 *            User names and passwords for the URL. For format, see
	 *            {@link #tryPasswords(URL, String)}.
	 * @param localTarget
	 *            A local file. If null is give, the contents of the file will
	 *            be returned as a String.
	 * @return null, if a local file was specified or the contents of the file,
	 *         if null was given as a local file.
	 * @throws IOException
	 *             all sorts of I/O exceptions, remote and local.
	 * @throws AutorizationException
	 *             If none of the given passwords were accepted.
	 */
	public static String download(URL URL, String userpwd, File localTarget)
			throws IOException, AutorizationException {
		final InputStream stream = tryPasswords(URL, userpwd).getInputStream();

		final OutputStream out;
		if (localTarget != null)
			out = new FileOutputStream(localTarget);
		else
			out = new ByteArrayOutputStream(64 * 1024);

		final int streamed = stream(stream, out, 64 * 1024);

		downloadedBytes.addAndGet(streamed);

		if (localTarget != null) {
			out.close();
			return null;
		} else
			return ((ByteArrayOutputStream) out).toString();

	}

	public static MagicResults execute(Page[] pages, boolean parallel) {
		downloadedBytes.set(0);
		downloadedFiles.set(0);
		spotted.set(0);
		done.set(0);

		final ExecutorService exec;
		final ExecutorService exec2;
		if (parallel) {
			exec = Executors.newCachedThreadPool();
			exec2 = exec;
		} else {
			exec = Executors.newSingleThreadExecutor();
			exec2 = Executors.newSingleThreadExecutor();
		}
		final List<Throwable> thrown = new ArrayList<Throwable>();
		final List<Update> updates = new ArrayList<Update>();

		final CountDownLatch latch = new CountDownLatch(pages.length);

		for (final Page p : pages) {
			exec.execute(new Runnable() {
				public void run() {
					try {
						p.workTheMagic(exec2, thrown, updates);
						done.incrementAndGet();
					} catch (Throwable t) {
						thrown.add(t);
					} finally {
						latch.countDown();
					}
				}
			});
			spotted.incrementAndGet();
		}

		try {
			latch.await();
			exec.shutdown();
			if (!parallel)
				exec2.shutdown();
			exec.awaitTermination(86400, TimeUnit.SECONDS);
			if (!parallel)
				exec2.awaitTermination(86400, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		return new MagicResults(thrown.toArray(new Throwable[thrown.size()]),
				updates.toArray(new Update[updates.size()]));
	}

	/**
	 * 
	 * @param URL
	 *            the URL to be opened
	 * @param password
	 *            the username and password to be used in the format
	 *            <code>user:password</code>. May be null.
	 * @return the InputStream to that URL if successful
	 * @throws AutorizationException
	 *             if the authorization through the specified user and password
	 *             was not successful
	 */
	public static HttpURLConnection getConnectedConnection(URL URL,
			String userpwd) throws IOException, AutorizationException {
		HttpURLConnection con = (HttpURLConnection) URL.openConnection();
		con.setRequestMethod("GET");
		if (userpwd != null) {
			if (encoder == null)
				throw new RuntimeException(
						"No Base64Encoder specified, see setEncoder(Base64Encoder)!");
			con.setRequestProperty("Authorization",
					"Basic " + encoder.encode(userpwd));
		}
		if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
			if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
				throw new AutorizationException(URL);
			else
				throw new IOException("Request for " + URL.toString()
						+ " returned " + con.getResponseCode() + ": "
						+ con.getResponseMessage());
		}
		return con;
	}

	public static void put(File parent, Map<String, File> map) {
		File[] files = parent.listFiles();
		if (files == null)
			return;
		for (File f : files) {
			String name = f.getName().toLowerCase();
			String ext = "";

			if (name.indexOf(".") >= 0) {
				ext = name.substring(name.lastIndexOf("."));
				name = name.substring(0, name.lastIndexOf("."));
			}

			while (name.matches(".*\\[[^\\]]*\\].*"))
				name = name.replaceAll("\\[[^\\]]*\\]", "");

			if (f.isFile()) {
				map.put(name + ext, f);
			} else {
				put(f, map);
			}
		}
	}

	public static int stream(InputStream in, OutputStream out, int bufsize)
			throws IOException {
		int ret = 0;
		byte[] buf = new byte[bufsize];
		for (int len = 0; (len = in.read(buf)) > 0; ret += len)
			out.write(buf, 0, len);

		return ret;
	}

	public static HttpURLConnection tryPasswords(URL URL, String userpwd)
			throws MalformedURLException, IOException, AutorizationException {
		try {
			return getConnectedConnection(URL, null);
		} catch (AutorizationException e) {
			// ok, continue;
		}

		if (userpwd != null && !userpwd.equals("")) {
			final BufferedReader sr = new BufferedReader(new StringReader(userpwd));
			for (String line; (line = sr.readLine()) != null;) {
				if (line.equals(""))
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

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public Pattern getPattern() {
		return pattern;
	}

	public File getTargetLocation() {
		return targetLocation;
	}

	public void setTargetLocation(File targetLocation) {
		this.targetLocation = targetLocation;
	}

	public URL getURL() {
		return URL;
	}

	public void setURL(URL uRL) {
		URL = uRL;
	}

	public String getUserpwd() {
		return userpwd;
	}

	public void setUserpwd(String userpwd) {
		this.userpwd = userpwd;
	}

	private Pattern pattern = Pattern.compile(".*\\.pdf",
			Pattern.CASE_INSENSITIVE);

	private File targetLocation = null;
	private URL URL = null;
	private String userpwd = null;

	public Page() {
	}

	public Page(URL URL, File targetLocation, String userpwd) {
		this.URL = URL;
		this.targetLocation = targetLocation;
		this.userpwd = userpwd;
	}
	
	public void validate() throws IllegalArgumentException {
		if(URL == null) {
			throw new IllegalArgumentException("Please specify a URL!");
		}
		if(targetLocation == null) {
			throw new IllegalArgumentException("Please specify a target location!");
		}
	}

	public void workTheMagic(ExecutorService exec,
			final List<Throwable> thrown, final List<Update> updates) {
		validate();
		
		String inputHTML;

		try {
			inputHTML = download(URL, userpwd, null);
		} catch (Exception e1) {
			thrown.add(e1);
			return;
		}

		// clean comments
		inputHTML = inputHTML.replaceAll("<!--.*?-->", "");
		Matcher comment = Pattern.compile("<!--.*?-->", Pattern.DOTALL)
				.matcher(inputHTML);
		while (comment.find())
			inputHTML = inputHTML.substring(0,
					inputHTML.indexOf(comment.group(0)))
					+ inputHTML.substring(inputHTML.indexOf(comment.group(0))
							+ comment.group(0).length());

		Pattern p = Pattern
				.compile("<a[^>]+href=[\"]?([^\"> ]+)[\"]?[^>]*>([^<]+)</a>");
		Matcher m = p.matcher(inputHTML);

		if (!targetLocation.exists())
			if (!targetLocation.mkdirs()) {
				thrown.add(new Exception("Could not create target Folder: "
						+ targetLocation.getAbsolutePath()));
				return;
			}

		HashMap<String, File> map = new HashMap<String, File>();
		put(targetLocation, map);

		while (m.find()) {

			String abstractURL = m.group(1);
			// System.out.println(abstractURL);
			if (pattern != null && !pattern.matcher(abstractURL).matches())
				continue;

			URL fileURL;

			try {
				fileURL = new URL(URL, abstractURL);
			} catch (MalformedURLException e) {
				thrown.add(e);
				continue;
			}

			final Update update = new Update();

			final URL u = fileURL;
			File targetFile = map.get(new File(fileURL.getPath()).getName()
					.toLowerCase());
			if (targetFile == null) {
				targetFile = new File(targetLocation, new File(
						fileURL.getPath()).getName());
				update.type = UpdateType.isNew;
			} else if (!updateAllFiles) {
				// so the file exists and we don't want to update all files
				// let's check, if the file sizes differ!

				try {
					int length = tryPasswords(fileURL, userpwd)
							.getContentLength();

					if (length == 0 || length == targetFile.length())
						continue;

					update.type = UpdateType.wasModified;
				} catch (Exception e) {
					thrown.add(e);
					continue;
				}
			}

			final File f = targetFile;
			update.file = f;

			spotted.incrementAndGet();

			exec.execute(new Runnable() {
				public void run() {
					try {
						download(u, userpwd, f);
						downloadedFiles.incrementAndGet();
						updates.add(update);
					} catch (Exception e) {
						thrown.add(e);
					} finally {
						done.incrementAndGet();
					}
				}
			});
		}
	}
}