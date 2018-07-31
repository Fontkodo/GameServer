package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

import javafx.geometry.Point2D;

public class Player extends SpaceObject{
	String userid;
	
	Player(Point2D loc, String userid) throws IOException {
		super(new Velocity(0, 0), loc, 0, "http://blasteroids.prototyping.site/assets/images/ship/ship.png");
		this.userid = userid;
	}
	
	public JSONObject toJSON() {
		JSONObject ob = super.toJSON();
		ob.put("userid", this.userid);
		return ob;
	}
}
