package hecdssvue.cdec.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	
	public static void copyInputStreamToFile( InputStream in, File file ) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024*64];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static String getAppDataDir() {
		String appDataDir = System.getenv("LOCALAPPDATA");
		if (appDataDir==null || appDataDir.length() == 0){
			appDataDir = System.getProperty("user.home");
		}
		return appDataDir + File.separator;
	}

	public static String fetchDataInUrl(String url) throws Exception {
		URL u = new URL(url);
		HttpURLConnection c = (HttpURLConnection) u.openConnection();
		c.setRequestMethod("GET");
		c.setRequestProperty("User-Agent", "Mozilla");
		int responseCode = c.getResponseCode();
		if (responseCode != 200) {
			System.err.println("Response code: " + responseCode + " for URL: "
					+ u);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				c.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = in.readLine()) != null) {
			sb.append(line+'\n');
		}
		in.close();
		c.disconnect();
		return sb.toString();
	}

	public static String[] getAllMatches(String regex, String data){
		return getAllMatches(Pattern.compile(regex).matcher(data));
	}

	
	private static String[] getAllMatches(Matcher m) {
		ArrayList<String> matches = new ArrayList<String>();
		while (m.find()) {
			matches.add(m.group(1));
		}
		return matches.toArray(new String[matches.size()]);
	}

	public static String[] getTagContents(String tag, String data) {
		return getAllMatches(regexTagContents(tag).matcher(data));
	}

	public static Pattern regexTagContents(String tag) {
		if (tag == null) {
			tag = "html";
		}
		return Pattern.compile("<" + tag + ".*?>(.*?)</" + tag + ">",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	}

}
