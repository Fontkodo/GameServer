package gameserver;

import java.io.IOException;
import org.json.simple.*;


class SpaceObject {
	final Velocity vel;
	Coordinate loc;
	final double rotvel;
	final long timestamp;
	String imgURL;
	PackageImage img;
	double currentRotation;
	double scale;
	
	SpaceObject(Velocity vel, Coordinate loc, double rotvel, String imgURL, double currentRotation) throws IOException {
		this.vel = vel;
		this.loc = loc;
		this.rotvel = rotvel;
		this.timestamp = System.currentTimeMillis();
		this.imgURL = imgURL;
		this.img = ImageFactory.getImage(imgURL);
		this.currentRotation = currentRotation;
		this.scale = 1;
	}
	
	Status getStatus() {
		long elapsed = System.currentTimeMillis()-this.timestamp;
		double angle = elapsed*rotvel;
		Coordinate newLoc = new Coordinate(loc.getX() + elapsed*vel.x, loc.getY() + elapsed*vel.y);
		return new Status(newLoc, angle);
	}
	
	double getRadius() {
		return this.scale*(Math.max(img.getWidth(), img.getHeight()))/2;
	}
	
	boolean inBounds(double maxX, double maxY) {
		Status status = this.getStatus();
		return (status.loc.getX() > -100
				&& status.loc.getX() < maxX+100
				&& status.loc.getY() > -100
				&& status.loc.getY() < maxY+100);
	}
	
	static class Status {
		final Coordinate loc;
		final double angle;
		
		Status(Coordinate loc, double angle) {
			this.loc = loc;
			this.angle = angle;
		}
	}
	
	@SuppressWarnings("unchecked")
	JSONObject toJSON() {
		JSONObject ob = new JSONObject();
		ob.put("vel", this.vel.toJSON());
		JSONObject loc = new JSONObject();
		loc.put("x", this.loc.getX());
		loc.put("y", this.loc.getY());
		ob.put("loc", loc);
		ob.put("rotvel", this.rotvel);
		ob.put("timestamp", this.timestamp);
		ob.put("imgURL", this.imgURL);
		ob.put("currentRotation", this.currentRotation);
		ob.put("scale", this.scale);
		return ob;
	}
	
	boolean inContactWith(SpaceObject ob) {
		Coordinate currentLoc = this.getStatus().loc;
		Coordinate obCurrentLoc = ob.getStatus().loc;
		double distance = currentLoc.distance(obCurrentLoc);
		boolean result = (distance < (this.getRadius() + ob.getRadius()));
		return result;
	}
	
	double getArea() {
		return this.img.getWidth()*this.img.getHeight()*this.scale*this.scale;
	}
}
