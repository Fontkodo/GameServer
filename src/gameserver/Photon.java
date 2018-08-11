package gameserver;

import java.io.IOException;

import org.json.simple.JSONObject;

public class Photon extends SpaceObject{
	Player player;
	
	Photon(Coordinate loc, double currentRotation, Player player) throws IOException {
		super(new Velocity(Math.cos(currentRotation), -Math.sin(currentRotation)), loc, 0, "http://blasteroids.net/assets/images/ship/photon.png", currentRotation);
		this.player = player;
	}
}
