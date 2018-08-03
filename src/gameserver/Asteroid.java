package gameserver;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javafx.geometry.Point2D;

public class Asteroid extends SpaceObject{
	static Random random = new Random(new Date().getTime());
	
	public Asteroid(Velocity vel, Point2D loc, double rotvel, String imgURL, double currentRotation) throws IOException {
		super(vel, loc, rotvel, imgURL, currentRotation);
		this.scale = random.nextDouble() + 0.1;
	}
	
	public Explosion explode() throws IOException {
		return new Explosion(this.vel, this.getStatus().loc, this.rotvel, this.currentRotation, this.scale);
	}
}
