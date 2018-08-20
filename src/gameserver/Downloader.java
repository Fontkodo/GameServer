package gameserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

