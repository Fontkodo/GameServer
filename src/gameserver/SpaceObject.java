package gameserver;

import java.io.IOException;

import javafx.geometry.*;
import javafx.scene.image.*;

import org.json.simple.*;

public class SpaceObject {
	final Velocity vel;
	final Point2D loc;
	final double rotvel;
	final long timestamp;
	final String imgURL;
	final Image img;
	
	public SpaceObject(Velocity vel, Point2D loc, double rotvel, String imgURL) throws IOException {
		this.vel = vel;
		this.loc = loc;
		this.rotvel = rotvel;
		this.timestamp = System.currentTimeMillis();
		this.imgURL = imgURL;
		this.img = ImageFactory.getImage(imgURL);
		//System.out.printf("Width: %f\nHeight: %f\nURL: %s", img.getWidth(), img.getHeight(), imgURL);
	}
	
	public Status getStatus() {
		long elapsed = System.currentTimeMillis()-this.timestamp;
		double angle = elapsed*rotvel;
		Point2D newLoc = new Point2D(loc.getX() + elapsed*vel.x, loc.getY() + elapsed*vel.y);
		return new Status(newLoc, angle);
	}
	
	public double getRadius() {
		return (Math.max(img.getWidth(), img.getHeight()))/2;
	}
	
	public boolean inBounds(double maxX, double maxY) {
		Status status = this.getStatus();
		return (status.loc.getX() > -getRadius()
				&& status.loc.getX() < maxX+getRadius()
				&& status.loc.getY() > -getRadius()
				&& status.loc.getY() < maxY+getRadius());
	}
	
	static class Status {
		final Point2D loc;
		final double angle;
		
		Status(Point2D loc, double angle) {
			this.loc = loc;
			this.angle = angle;
		}
	}
	
	public JSONObject toJSON() {
		JSONObject ob = new JSONObject();
		ob.put("vel", this.vel.toJSON());
		JSONObject loc = new JSONObject();
		loc.put("x", this.loc.getX());
		loc.put("y", this.loc.getY());
		ob.put("loc", loc);
		ob.put("rotvel", this.rotvel);
		ob.put("timestamp", this.timestamp);
		ob.put("imgURL", this.imgURL);
		return ob;
	}
}
