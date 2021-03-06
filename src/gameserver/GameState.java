package gameserver;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

class GameState {

	private static Map<String, Long> highScore = new HashMap<String, Long>();
	private static final String highScoreFile = System.getProperty("user.home")
			+ "/high-score.json";
	
	static {
		System.out.println("attempting to read " + highScoreFile);
		try {
			JSONParser parser = new JSONParser();
			FileReader fos = new FileReader(highScoreFile);
			JSONObject o = (JSONObject) parser.parse(fos);
			fos.close();
			for (Object k : o.keySet()) {
				highScore.put(k.toString(), ((Number) o.get(k)).longValue());
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	static long getHighScore(String userid) {
		synchronized (highScore) {
			return highScore.getOrDefault(userid, 0L);
		}
	}

	static void updatePossibleHighScore(String userid, long score) {
		synchronized (highScore) {
			if (score > getHighScore(userid)) {
				System.out.println("new high score " + score + " for " + userid);
				highScore.put(userid, score);
				JSONObject o = new JSONObject(highScore);
				try {
					FileWriter fw = new FileWriter(highScoreFile);
					fw.write(o.toJSONString() + "\n");
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	List<Asteroid> loa = new ArrayList<Asteroid>();
	List<Photon> lop = new ArrayList<Photon>();
	List<String> los = new ArrayList<String>();
	List<Explosion> loe = new ArrayList<Explosion>();
	Map<String, Player> players = new HashMap<String, Player>();
	private boolean _playersChanged = false;

	Player getPlayer(String userid) throws IOException {
		_playersChanged = true;
		if (players.containsKey(userid)) {
			return players.get(userid);
		}
		Player newPlayer = new Player(new Coordinate(800 - Math.random() * 200, 500 - Math.random() * 200), userid);
		players.put(userid, newPlayer);
		if (!highScore.containsKey(userid)) {
			highScore.put(userid, Long.valueOf(0));
		}
		return newPlayer;
	}

	void removePlayer(String userid) throws IOException {
		_playersChanged = true;
		players.remove(userid);
	}

	boolean playersChanged() throws IOException {
		for (String key : players.keySet()) {
			Player p = players.get(key);
			double halfW = p.img.getWidth() / 2;
			double halfH = p.img.getHeight() / 2;
			long elapsed = System.currentTimeMillis() - p.timestamp;
			double dx = elapsed * p.vel.x;
			double dy = elapsed * p.vel.y;
			Coordinate oldLoc = new Coordinate(p.loc.getX() + dx, p.loc.getY() + dy);
			Coordinate newLoc = null;
			if (oldLoc.getX() < -halfW) {
				newLoc = new Coordinate(GameServer.width + halfW, oldLoc.getY());
			} else if (oldLoc.getY() < -halfH) {
				newLoc = new Coordinate(oldLoc.getX(), GameServer.height + halfH);
			} else if (oldLoc.getX() > GameServer.width + halfW) {
				newLoc = new Coordinate(-halfW, oldLoc.getY());
			} else if (oldLoc.getY() > GameServer.height + halfH) {
				newLoc = new Coordinate(oldLoc.getX(), -halfH);
			}

			if (p.score > highScore.get(key)) {
				highScore.replace(key, p.score);
			}

			if (newLoc != null) {
				players.replace(key,
						new Player(players.get(key).vel, newLoc, players.get(key).rotvel,
								players.get(key).currentRotation, players.get(key).userid, players.get(key).score,
								players.get(key).photonCount, players.get(key).fuel, players.get(key).shieldLevel,
								players.get(key).lastInjury));
				_playersChanged = true;
			}
		}
		try {
			return _playersChanged;
		} finally {
			_playersChanged = false;
		}
	}

	@SuppressWarnings("unchecked")
	String serialize() {
		JSONObject ob = new JSONObject();
		JSONArray ar = new JSONArray();
		for (SpaceObject a : loa) {
			ar.add(a.toJSON());
		}
		for (SpaceObject ph : lop) {
			ar.add(ph.toJSON());
		}
		for (String key : players.keySet()) {
			Player p = players.get(key);
			ar.add(p.toJSON());
		}
		for (SpaceObject ex : loe) {
			ar.add(ex.toJSON());
		}
		ob.put("SpaceObjects", ar);
		JSONArray sounds = new JSONArray();
		for (String s : los) {
			sounds.add(s);
		}
		ob.put("Sounds", sounds);
		JSONObject tempDim = new JSONObject();
		tempDim.put("Width", GameServer.width);
		tempDim.put("Height", GameServer.height);
		ob.put("Dimensions", tempDim);
		ob.put("currentMillis", System.currentTimeMillis());
		return ob.toJSONString();
	}

	void connect(String userid) throws IOException {
		getPlayer(userid);
	}

	void left(String userid) throws IOException {
		Player p = getPlayer(userid);
		p.currentRotation += 5 * Math.PI / 180;
	}

	void right(String userid) throws IOException {
		Player p = getPlayer(userid);
		p.currentRotation -= 5 * Math.PI / 180;
	}
	
	private void movement(String userid, double sign) throws IOException {
		Player p = getPlayer(userid);
		Velocity newVel = new Velocity(p.vel.x + sign * (Math.cos(p.currentRotation)) / 200,
					p.vel.y - sign * (Math.sin(p.currentRotation)) / 200);
		long elapsedTime = System.currentTimeMillis() - p.timestamp;
		double dx = p.vel.x * elapsedTime;
		double dy = p.vel.y * elapsedTime;
		Player newP = new Player(newVel, new Coordinate(p.loc.getX() + dx, p.loc.getY() + dy), p.rotvel,
				p.currentRotation, p.userid, p.score, p.photonCount, p.fuel, p.shieldLevel, p.lastInjury);
		newP.currentRotation = p.currentRotation;
		if (newP.fuel > 0) {
			newP.fuel -= 0.1;
		}
		players.replace(p.userid, newP);
		los.add("http://blasteroids.net/assets/sounds/thrust.wav");
	}

	void forward(String userid) throws IOException {
		movement(userid, 1);
	}

	void backward(String userid) throws IOException {
		movement(userid, -1);
	}

	void fire(String userid) throws IOException {
		Player p = getPlayer(userid);
		if (p.photonCount > 0) {
			long elapsed = System.currentTimeMillis() - p.timestamp;
			double dx = elapsed * p.vel.x;
			double dy = elapsed * p.vel.y;
			Coordinate newLoc = new Coordinate(p.loc.getX() + dx, p.loc.getY() + dy);
			lop.add(new Photon(newLoc, p.currentRotation, p));
			p.photonCount -= 1;
			los.add("http://blasteroids.net/assets/sounds/photon.wav");
		}
	}

	void disconnect(String userid) throws IOException {
		removePlayer(userid);
	}
}