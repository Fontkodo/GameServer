package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

import javafx.geometry.Point2D;

public class Photon extends SpaceObject{
	Player player;
	
	Photon(Point2D loc, double currentRotation, Player player) throws IOException {
		super(new Velocity(Math.cos(currentRotation), -Math.sin(currentRotation)), loc, 0, "http://blasteroids.prototyping.site/assets/images/ship/photon.png", currentRotation);
		this.player = player;
	}
}
