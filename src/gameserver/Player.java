package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

import javafx.geometry.Point2D;

public class Player extends SpaceObject{
	String userID;
	
	Player(Point2D loc, String userID) throws IOException {
		super(new Velocity(0, 0), loc, 0, "http://blasteroids.prototyping.site/assets/images/ship/ship.png");
		this.userID = userID;
	}
	
	public JSONObject toJSON() {
		JSONObject ob = super.toJSON();
		ob.put("userID", this.userID);
		return ob;
	}
}
