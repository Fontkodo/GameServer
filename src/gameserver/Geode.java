package gameserver;

import java.io.IOException;

import javafx.geometry.Point2D;

public class Geode extends Asteroid {

	public Geode(Velocity vel, Point2D loc, double rotvel, double currentRotation) throws IOException {
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/asteroids/asteroid-powerup.png", currentRotation);
		this.scale = 1;
	}
	
	public boolean isGeode() {
		return true;
	}

}
