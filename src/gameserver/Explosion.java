package gameserver;

import java.io.IOException;

class Explosion extends SpaceObject{
	
	Explosion(Velocity vel, Coordinate loc, double rotvel, double currentRotation, double scale) throws IOException{
		super(vel, loc, rotvel, "http://blasteroids.net/assets/images/explosions/Explosion1.png", currentRotation);
		this.scale = scale;
	}
	
	boolean shouldILive (long now) {
		long millisSinceExplosion = now - this.timestamp;
		long frame = millisSinceExplosion/50 + 1;
		
		if (frame > 5) {
			return false;
		}
		
		this.imgURL = ("http://blasteroids.net/assets/images/explosions/Explosion" + frame + ".png");
		return true;
	}
}
