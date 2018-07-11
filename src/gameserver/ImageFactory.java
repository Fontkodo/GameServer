package gameserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public final class ImageFactory {
	private static final Map<String, Image> cache = new HashMap<String, Image>();
	
	static synchronized Image getImage(String spec) throws IOException {
		Image image = cache.get(spec);
		if(image == null) {
			image = new Image(Downloader.getInputStream(spec));
			cache.put(spec, image);
		}
		return image;
	}
}
