package gameserver;

import java.io.IOException;

class Geode extends Asteroid {

	Geode(Velocity vel, Coordinate loc, double rotvel, double currentRotation) throws IOException {
		super(vel, loc, rotvel, "http://blasteroids.net/assets/images/asteroids/asteroid-powerup.png", currentRotation);
		this.scale = 1;
	}
	
	boolean isGeode() {
		return true;
	}

}
