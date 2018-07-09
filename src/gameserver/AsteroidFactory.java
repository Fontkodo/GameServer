package gameserver;

import javafx.geometry.*;
import javafx.stage.*;

import java.io.*;
import java.util.*;

public final class AsteroidFactory {

	static Random random = new Random(new Date().getTime());
	static double canvasWidth = 800;
	static double canvasHeight = 800;
	
	public static Asteroid makeAsteroid() throws IOException {
		
		Point2D loc = new Point2D(400,400);
		double rotvel = random.nextDouble()/1000;
		Velocity vel = new Velocity(random.nextDouble()*0.1, random.nextDouble()*0.1);
		
		return new Asteroid(vel, loc, rotvel);
	}
}