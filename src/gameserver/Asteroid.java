package gameserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javafx.geometry.Point2D;

public class Asteroid extends SpaceObject{
	static Random random = new Random(new Date().getTime());
	
	public Asteroid(Velocity vel, Point2D loc, double rotvel, String imgURL, double currentRotation) throws IOException {
		super(vel, loc, rotvel, imgURL, currentRotation);
		this.scale = random.nextDouble() + 0.1;
	}
	
	public Explosion explode() throws IOException {
		return new Explosion(this.vel, this.getStatus().loc, this.rotvel, this.currentRotation, this.scale);
	}
	
	public ArrayList<Asteroid> giveBirth() throws IOException {
		ArrayList<Asteroid> children = new ArrayList<Asteroid>();
		double totalChildArea = 0;
		if (this.getArea() < 5_000) {
			return children;
		}
		int amount = 2 + random.nextInt(10);
		for (int i = 0; i < amount; i++) {
			Asteroid child = AsteroidFactory.makeAsteroid();
			child.loc = this.getStatus().loc;
			children.add(child);
			totalChildArea += child.getArea();
		}
		if (totalChildArea > this.getArea()) {
			for (Asteroid c : children) {
				c.scale = c.scale * (c.getArea() / totalChildArea);
			}
		}
		return children;
	}
}
