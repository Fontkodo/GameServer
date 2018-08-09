package gameserver;

import java.io.IOException;

public class Explosion extends SpaceObject{
	
	public Explosion(Velocity vel, Coordinate loc, double rotvel, double currentRotation, double scale) throws IOException{
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/explosions/Explosion1.png", currentRotation);
		this.scale = scale;
	}
	
	public boolean shouldILive (long now) {
		long millisSinceExplosion = now - this.timestamp;
		long frame = millisSinceExplosion/50 + 1;
		
		if (frame > 5) {
			return false;
		}
		
		this.imgURL = ("http://blasteroids.prototyping.site/assets/images/explosions/Explosion" + frame + ".png");
		return true;
	}
}
