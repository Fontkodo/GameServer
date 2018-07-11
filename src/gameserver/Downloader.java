package gameserver;

import java.io.*;
import java.net.*;

final class Downloader {
	
	private static String cacheFileName(String url) {
		int n = url.lastIndexOf(".");
		if(n < 0) {
			throw new RuntimeException("No Extension");
		}
		String ext = url.substring(n);
		String cacheFile = String.format("/tmp/cache%d%s", url.hashCode(), ext);
		return cacheFile;
	}
	
	static InputStream getInputStream(String spec) throws IOException {
		String cfn = cacheFileName(spec);
		try {
			return new BufferedInputStream(new FileInputStream(cfn));
		} catch (FileNotFoundException e) {}
		
		URL url = new URL(spec);
		try {
			InputStream is = url.openStream();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cfn));
			int ch;
			while((ch = is.read()) != -1) {
				bos.write(ch);
			}
			is.close();
			bos.close();
			return new BufferedInputStream(new FileInputStream(cfn));
		} catch (IOException e) {
			throw e;
		}
	}
}

