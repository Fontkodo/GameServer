package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

public class Player extends SpaceObject{
	String userid;
	long score;
	long photonCount;
	double fuel;
	long shieldLevel;
	double lastInjury;
	
	Player(Coordinate loc, String userid) throws IOException {
		this(new Velocity(0, 0), loc, 0, 0, userid);
	}
	
	Player(Velocity vel, Coordinate loc, double rotvel, double currentRotation, String userid) throws IOException {
		this(vel, loc, rotvel, currentRotation, userid, 0, 100, 200, 10, System.currentTimeMillis());
	}
	
	Player(Velocity vel, Coordinate loc, double rotvel, double currentRotation, String userid, long score, long photonCount, double fuel, long shieldLevel, double lastInjury) throws IOException {
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/ship/ship.png", currentRotation);
		this.userid = userid;
		this.score = score;
		this.photonCount = photonCount;
		this.fuel = fuel;
		this.shieldLevel = shieldLevel;
		this.lastInjury = lastInjury;
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
		ob.put("highScore", GameState.getHighScore(this.userid));
		return ob;
	}
	
	public boolean vulnerable() {
		return (System.currentTimeMillis() - this.lastInjury) > 3000;
	}
}
