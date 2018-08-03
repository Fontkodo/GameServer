package gameserver;

import java.io.IOException;

import javafx.geometry.Point2D;

public class Explosion extends SpaceObject{
	long explosionTime;
	
	public Explosion(Velocity vel, Point2D loc, double rotvel, double currentRotation) throws IOException{
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/explosions/Explosion1.png", currentRotation);
		this.explosionTime = System.currentTimeMillis();
	}
	
	public boolean shouldILive (long now) {
		long nanosSinceExplosion = now - this.explosionTime;
		long frame = nanosSinceExplosion/100_000_000 + 1;
		
		if (frame > 5) {
			return false;
		}
		
		try {
			this.img = ImageFactory.getImage(
					"http://blasteroids.prototyping.site/assets/images/explosions/Explosion" + frame + ".png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
