package gameserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ImageFactory {
	
	private static final Map<String, PackageImage> cache = new HashMap<String, PackageImage>();
	
	static synchronized PackageImage getImage(String spec) throws IOException {
		PackageImage image = cache.get(spec);
		if(image == null) {
			image = new PackageImage(Downloader.getInputStream(spec));
			cache.put(spec, image);
		}
		return image;
	}
}
