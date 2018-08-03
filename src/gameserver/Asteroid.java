package gameserver;

import java.io.IOException;

import javafx.geometry.Point2D;

public class Asteroid extends SpaceObject{
	public Asteroid(Velocity vel, Point2D loc, double rotvel, String imgURL, double currentRotation) throws IOException {
		super(vel, loc, rotvel, imgURL, currentRotation);
	}
	
	public Explosion explode() throws IOException {
		return new Explosion(this.vel, this.getStatus().loc, this.rotvel, this.currentRotation);
	}
}
