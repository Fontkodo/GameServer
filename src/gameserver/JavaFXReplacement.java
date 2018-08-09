package gameserver;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

class Coordinate {
	final double x;
	final double y;

	Coordinate(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	double getX() { return x; }
	double getY() { return y; }

	double distance(Coordinate another) {
		double xDiff = another.x - this.x;
		double yDiff = another.y - this.y;
		return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
	}
}


class PackageImage {
	
	private final java.awt.image.BufferedImage awtImage;

	PackageImage(InputStream imageInputStream) throws IOException {
		awtImage = ImageIO.read(imageInputStream);
	}

	double getWidth() {
		return (double) awtImage.getWidth();
	}

	double getHeight() {
		return (double) awtImage.getHeight();
	}
}

