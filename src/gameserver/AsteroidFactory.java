package gameserver;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

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
		
		double rotvel = random.nextDouble()/500;
		if (random.nextInt(50) == 0) {
			return new Geode(vel, loc, rotvel, 0);
		}
		
		String imgURL = "http://blasteroids.net/assets/images/asteroids/asteroid" + random.nextInt(9) + ".png";
		return new Asteroid(vel, loc, rotvel, imgURL, 0);
	}
}