package gameserver;

import org.json.simple.JSONObject;

final class Velocity {
	
	final double x;
	final double y;
	
	Velocity(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	double getMagnitude() {
		return Math.sqrt(x*x + y*y);
	}
	
	@SuppressWarnings("unchecked")
	JSONObject toJSON() {
		JSONObject ob = new JSONObject();
		ob.put("x", this.x);
		ob.put("y", this.y);
		return ob;
	}
}
