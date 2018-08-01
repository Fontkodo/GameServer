package gameserver;

import java.io.IOException;

import javafx.geometry.Point2D;

public class Photon extends SpaceObject{
	Photon(Point2D loc, double currentRotation) throws IOException {
		super(new Velocity(Math.cos(currentRotation), -Math.sin(currentRotation)), loc, 0, "http://blasteroids.prototyping.site/assets/images/ship/photon.png", currentRotation);
	}
}
