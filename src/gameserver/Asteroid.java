package gameserver;

import javafx.geometry.*;

public class Asteroid {
	final Velocity vel;
	final Point2D loc;
	final double rotvel;
	final long timestamp;
	
	public Asteroid(Velocity vel, Point2D loc, double rotvel) {
		this.vel = vel;
		this.loc = loc;
		this.rotvel = rotvel;
		this.timestamp = System.currentTimeMillis();
	}
	
	public Status getStatus() {
		long elapsed = System.currentTimeMillis()-this.timestamp;
		double angle = elapsed*rotvel;
		Point2D newLoc = new Point2D(loc.getX() + elapsed*vel.x, loc.getY() + elapsed*vel.y);
		return new Status(newLoc, angle);
	}
	
	public boolean inBounds(double maxX, double maxY) {
		Status status = this.getStatus();
		return (status.loc.getX() > 0 && status.loc.getX() < maxX && status.loc.getY() > 0 && status.loc.getY() < maxY);
	}
	
	static class Status {
		final Point2D loc;
		final double angle;
		
		Status(Point2D loc, double angle) {
			this.loc = loc;
			this.angle = angle;
		}
	}
}
