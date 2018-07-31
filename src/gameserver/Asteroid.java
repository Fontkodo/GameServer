package gameserver;

import java.io.IOException;

import javafx.geometry.Point2D;

public class Asteroid extends SpaceObject{
	public Asteroid(Velocity vel, Point2D loc, double rotvel, String imgURL) throws IOException {
		super(vel, loc, rotvel, imgURL);
	}
}
