package gameserver;

import java.io.IOException;

import javafx.geometry.Point2D;

public class Explosion extends SpaceObject{
	
	public Explosion(Velocity vel, Point2D loc, double rotvel, double currentRotation) throws IOException{
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/explosions/Explosion1.png", currentRotation);
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
