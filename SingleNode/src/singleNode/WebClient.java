package singleNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;


public class WebClient {
	private boolean isHttpSecure;

	private String URL;
	private String method = "GET";
	private String version = "HTTP/1.0";
	private String userAgent = "cis455crawler";
	private String hostName;
	private int port;
	private String path;

	private int statusCode = 200;
	private String content = "";
	private long lastModified = 0;

	private InputStream inStream;
	private OutputStream outStream;
	private Socket socket;
	private HttpsURLConnection connection;
	
	private boolean isConnected = false;

	private static final String contentLengthPattern = "(Content-Length:\\s*)(\\d+)";
	private static final String contentTypePattern = "(Content-Type:\\s*)(.*)";
	private static final String statusCodePattern = "(HTTP/1.[01]\\s*)(\\d+)(.*)";
	private static final String lastModifiedPattern = "(Last-Modified\\s*)(.*)";

	private int contentLength = -1;
	private String contentType = "";

	public int getContentLength() {
		return contentLength;
	}

	public long getLastModified() {
		return lastModified;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public String getHostName() {
		return hostName;
	}

	public String getContent() {
		return content;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getContentType() {
		return contentType;
	}

	public WebClient(String URL, String method, boolean isHttpSecure)
			throws UnknownHostException, IOException {
		this.isHttpSecure = isHttpSecure;
		this.URL = URL;
		this.method = method;

		if (isHttpSecure) {
			URL myurl = new URL(this.URL);
			hostName = myurl.getHost();
			connection = (HttpsURLConnection) myurl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setFollowRedirects(false);


		} else {
			URLInfo urlInfo = new URLInfo(URL);
			hostName = urlInfo.getHostName();
			port = urlInfo.getPortNo();
			path = urlInfo.getFilePath();

			socket = new Socket(hostName, port);

			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
		}
	}

	public void sendRequest() throws IOException {
		if (isHttpSecure) {
			connection.setRequestMethod(method);
			connection.setRequestProperty("User-Agent", userAgent);
			
			if (!isConnected) {
				outStream = connection.getOutputStream();
				inStream = connection.getInputStream();
				isConnected = true;
			}
			
			outStream.flush();

		} else {
			String request = "";
			request = method + " " + path + " " + version + "\r\n" + "Host: "
					+ hostName + ":" + port + "\r\n" + "User-agent: "
					+ userAgent + "\r\n\r\n";

			outStream.write(request.getBytes());
			outStream.flush();
		}

	}

	public String getResponse() throws IOException {
		if (isHttpSecure) {
			contentType = connection.getContentType();
			contentType = contentType.replaceAll(";.*", "");
			statusCode = connection.getResponseCode();
			lastModified = connection.getLastModified();

			StringBuilder sb = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					inStream));
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				sb.append(line + "\n");
			}

			content = sb.toString();

		} else {
			Pattern pattern;
			Matcher matcher;

			BufferedReader in = new BufferedReader(new InputStreamReader(
					inStream));
			while (true) {

				String line = in.readLine();

				if (("").equals(line)) {
					break;
				}

				if (line.matches(contentLengthPattern)) {
					pattern = Pattern.compile(contentLengthPattern);
					matcher = pattern.matcher(line);
					matcher.find();
					contentLength = Integer.valueOf(matcher.group(2));
				} else if (line.matches(contentTypePattern)) {
					pattern = Pattern.compile(contentTypePattern);
					matcher = pattern.matcher(line);
					matcher.find();
					contentType = matcher.group(2);
					contentType = contentType.replaceAll(";.*", "");
				} else if (line.matches(statusCodePattern)) {
					pattern = Pattern.compile(statusCodePattern);
					matcher = pattern.matcher(line);
					matcher.find();
					statusCode = Integer.parseInt(matcher.group(2));
				} else if (line.matches(lastModifiedPattern)) {
					pattern = Pattern.compile(lastModifiedPattern);
					matcher = pattern.matcher(line);
					matcher.find();
					SimpleDateFormat format = new SimpleDateFormat(
							"EEE, dd MMM yyyy HH:mm:ss zzz");
					Date date = null;
					try {
						date = format.parse(matcher.group(2));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (date != null)
						lastModified = date.getTime();
				}
			}

			StringBuilder sb = new StringBuilder();

			if (contentLength == -1) {
				while (true) {
					int temp = in.read();
					if (temp == -1) {
						break;
					}
					sb.append((char) temp);

				}
			} else {
				for (int i = 0; i < contentLength; i++) {
					sb.append((char) in.read());
				}
			}

			content = sb.toString();
		}

		return content;

	}

}
