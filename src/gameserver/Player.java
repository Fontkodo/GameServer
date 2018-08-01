package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

import javafx.geometry.Point2D;

public class Player extends SpaceObject{
	String userid;
	
	Player(Point2D loc, String userid) throws IOException {
		this(new Velocity(0, 0), loc, 0, 0, userid);
	}
	
	Player(Velocity vel, Point2D loc, double rotvel, double currentRotation, String userid) throws IOException {
		super(vel, loc, rotvel, "http://blasteroids.prototyping.site/assets/images/ship/ship.png", currentRotation);
		this.userid = userid;
	}
	
	public JSONObject toJSON() {
		JSONObject ob = super.toJSON();
		ob.put("userid", this.userid);
		return ob;
	}
}
