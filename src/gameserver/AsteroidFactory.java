package gameserver;

import javafx.geometry.*;
import javafx.stage.*;

import java.io.*;
import java.util.*;

public final class AsteroidFactory {

	static Random random = new Random(new Date().getTime());
	
	public static SpaceObject makeAsteroid() throws IOException {
		
		Point2D tempLoc = new Point2D(GameServer.width * random.nextDouble(), GameServer.height * random.nextDouble());
		Point2D loc = new Point2D(0, 0);
		Velocity vel = new Velocity(random.nextDouble()*0.2-0.1, random.nextDouble()*0.2-0.1);
		if((Math.abs(vel.x) > Math.abs(vel.y)) && (vel.x > 0)) {
			loc = new Point2D(-70, tempLoc.getY());
		}
		
		else if((Math.abs(vel.x) <= Math.abs(vel.y)) && (vel.y >= 0)) {
			loc = new Point2D(tempLoc.getX(), GameServer.height + 70);
		}
		
		else if((Math.abs(vel.x) > Math.abs(vel.y)) && (vel.x <= 0)) {
			loc = new Point2D(GameServer.width + 70, tempLoc.getY());
		}
		
		else if((Math.abs(vel.x) <= Math.abs(vel.y)) && (vel.y < 0)) {
			loc = new Point2D(tempLoc.getX(), -70);
		}
		
		else {
			System.out.println("This is some serious fuckshit!");
		}
		double rotvel = random.nextDouble()/1000;
		String imgURL = "http://blasteroids.prototyping.site/assets/images/asteroids/asteroid" + random.nextInt(9) + ".png";
		
		return new SpaceObject(vel, loc, rotvel, imgURL, 0);
	}
}