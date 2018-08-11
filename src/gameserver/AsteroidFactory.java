package gameserver;

import javafx.geometry.*;
import javafx.stage.*;

import java.io.*;
import java.util.*;

public final class AsteroidFactory {

	static Random random = new Random(new Date().getTime());
	
	public static Asteroid makeAsteroid() throws IOException {
		
		Coordinate tempLoc = new Coordinate(GameServer.width * random.nextDouble(), GameServer.height * random.nextDouble());
		Coordinate loc = new Coordinate(0, 0);
		Velocity vel = new Velocity(random.nextDouble()*0.2-0.1, random.nextDouble()*0.2-0.1);
		if((Math.abs(vel.x) > Math.abs(vel.y)) && (vel.x > 0)) {
			loc = new Coordinate(-70, tempLoc.getY());
		}
		
		else if((Math.abs(vel.x) <= Math.abs(vel.y)) && (vel.y >= 0)) {
			loc = new Coordinate(tempLoc.getX(), GameServer.height + 70);
		}
		
		else if((Math.abs(vel.x) > Math.abs(vel.y)) && (vel.x <= 0)) {
			loc = new Coordinate(GameServer.width + 70, tempLoc.getY());
		}
		
		else if((Math.abs(vel.x) <= Math.abs(vel.y)) && (vel.y < 0)) {
			loc = new Coordinate(tempLoc.getX(), -70);
		}
		
		else {
			System.out.println("This is some serious fuckshit!");
		}
		double rotvel = random.nextDouble()/500;
		if (random.nextDouble() > 0.95) {
			return new Geode(vel, loc, rotvel, 0);
		}
		
		String imgURL = "http://blasteroids.net/assets/images/asteroids/asteroid" + random.nextInt(9) + ".png";
		return new Asteroid(vel, loc, rotvel, imgURL, 0);
	}
}