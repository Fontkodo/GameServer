package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

import javafx.geometry.Point2D;

public class Player extends SpaceObject{
	String userid;
	long score;
	long photonCount;
	double fuel;
	long shieldLevel;
	
	Player(Point2D loc, String userid) throws IOException {
		this(new Velocity(0, 0), loc, 0, 0, userid);
	}
	
	Player(Velocity vel, Point2D loc, double rotvel, double currentRotation, String userid) throws IOException {
		this(vel, loc, rotvel, currentRotation, userid, 0, 100, 200, 10);
	}
	
	Player(Velocity vel, Point2D loc, double rotvel, double currentRotation, String userid, long score, long photonCount, double fuel, long shieldLevel) throws IOException {
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/ship/ship.png", currentRotation);
		this.userid = userid;
		this.score = score;
		this.photonCount = photonCount;
		this.fuel = fuel;
		this.shieldLevel = shieldLevel;
	}
	
	public Explosion explode() throws IOException {
		return new Explosion(this.vel, this.getStatus().loc, this.rotvel, this.currentRotation, this.scale);
	}
	
	public JSONObject toJSON() {
		JSONObject ob = super.toJSON();
		ob.put("userid", this.userid);
		ob.put("score", this.score);
		ob.put("photonCount", this.photonCount);
		ob.put("fuel", this.fuel);
		ob.put("shieldLevel", this.shieldLevel);
		return ob;
	}
}
