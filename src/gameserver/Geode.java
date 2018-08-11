package gameserver;

import java.io.IOException;

public class Geode extends Asteroid {

	public Geode(Velocity vel, Coordinate loc, double rotvel, double currentRotation) throws IOException {
		super(vel, loc, rotvel, "http://blasteroids.net/assets/images/asteroids/asteroid-powerup.png", currentRotation);
		this.scale = 1;
	}
	
	public boolean isGeode() {
		return true;
	}

}
