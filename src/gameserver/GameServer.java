package gameserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.geometry.Point2D;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.*;

public class GameServer {

	static long width = 1400;
	static long height = 800;
	static BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(1);

	static class GameState {
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
			Player newPlayer = new Player(new Point2D(200, 200), userid);
			players.put(userid, newPlayer);
			return newPlayer;
		}

		void removePlayer(String userid) throws IOException {
			_playersChanged = true;
			players.remove(userid);
		}

		boolean playersChanged() throws IOException {
			for (String key : players.keySet()) {
				Player p = players.get(key);
				long elapsed = System.currentTimeMillis() - p.timestamp;
				double dx = elapsed * p.vel.x;
				double dy = elapsed * p.vel.y;
				Point2D oldLoc = new Point2D(p.loc.getX() + dx, p.loc.getY() + dy);
				Point2D newLoc = null;
				if (oldLoc.getX() < -70) {
					newLoc = new Point2D(width + 70, oldLoc.getY());
				} else if (oldLoc.getY() < -70) {
					newLoc = new Point2D(oldLoc.getX(), height + 70);
				} else if (oldLoc.getX() > width + 70) {
					newLoc = new Point2D(-70, oldLoc.getY());
				} else if (oldLoc.getY() > height + 70) {
					newLoc = new Point2D(oldLoc.getX(), -70);
				}

				if (newLoc != null) {
					players.replace(key, new Player(players.get(key).vel, newLoc, players.get(key).rotvel,
							players.get(key).currentRotation, players.get(key).userid));
					_playersChanged = true;
				}
			}
			try {
				return _playersChanged;
			} finally {
				_playersChanged = false;
			}
		}

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
			tempDim.put("Width", width);
			tempDim.put("Height", height);
			ob.put("Dimensions", tempDim);
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

		void forward(String userid) throws IOException {
			Player p = getPlayer(userid);
			Velocity newVel = new Velocity(p.vel.x + (Math.cos(p.currentRotation)) / 1000,
					p.vel.y - (Math.sin(p.currentRotation)) / 1000);
			long elapsedTime = System.currentTimeMillis() - p.timestamp;
			double dx = p.vel.x * elapsedTime;
			double dy = p.vel.y * elapsedTime;
			Player newP = new Player(newVel, new Point2D(p.loc.getX() + dx, p.loc.getY() + dy), p.rotvel,
					p.currentRotation, p.userid);
			newP.currentRotation = p.currentRotation;
			players.replace(p.userid, newP);
			los.add("http://blasteroids.prototyping.site/assets/sounds/thrust.wav");
		}

		void fire(String userid) throws IOException {
			Player p = getPlayer(userid);
			if (p.photonCount > 0) {
				long elapsed = System.currentTimeMillis() - p.timestamp;
				double dx = elapsed * p.vel.x;
				double dy = elapsed * p.vel.y;
				Point2D newLoc = new Point2D(p.loc.getX() + dx, p.loc.getY() + dy);
				lop.add(new Photon(newLoc, p.currentRotation, p));
				p.photonCount -= 1;
				los.add("http://blasteroids.prototyping.site/assets/sounds/photon.wav");
			}
		}

		void disconnect(String userid) throws IOException {
			removePlayer(userid);
		}
	}

	static GameState gameState = new GameState();

	static class QueueMonitor implements Runnable {

		void handleAsteroidQuantity() throws IOException {
			synchronized (gameState) {
				int madeChange = 0;
				ArrayList<Asteroid> keepers = new ArrayList<Asteroid>();
				for (Asteroid a : gameState.loa) {
					if (a.inBounds(width, height)) {
						keepers.add(a);
					} else {
						madeChange++;
					}
				}
				if (keepers.stream().mapToDouble(a -> a.getArea()).sum() < 40000) {
					while (keepers.stream().mapToDouble(a -> a.getArea()).sum() < 50000) {
						keepers.add(AsteroidFactory.makeAsteroid());
						madeChange++;
					}
				}
				gameState.loa = keepers;
				ArrayList<Photon> phKeepers = new ArrayList<Photon>();
				for (Photon ph : gameState.lop) {
					if (ph.inBounds(width, height)) {
						phKeepers.add(ph);
					} else {
						madeChange++;
					}
				}
				gameState.lop = phKeepers;
				Set<SpaceObject> destroyed = new HashSet<SpaceObject>();
				Set<String> destroyedPlayers = new HashSet<String>();
				ArrayList<Asteroid> tempLoa = new ArrayList<Asteroid>();
				for (Photon ph : gameState.lop) {
					for (Asteroid a : gameState.loa) {
						if (a.inContactWith(ph)) {
							gameState.loe.add(a.explode());
							tempLoa.addAll(a.giveBirth());
							destroyed.add(a);
							destroyed.add(ph);
							madeChange++;
						}
					}
					for (String p : gameState.players.keySet()) {
						if (gameState.players.get(p).inContactWith(ph)) {
							if (gameState.players.get(p).shieldLevel > 0) {
								gameState.players.get(p).shieldLevel -= 1;
							}
							if (gameState.players.get(p).shieldLevel <= 0) {
								gameState.loe.add(gameState.players.get(p).explode());
								// destroyedPlayers.add(p);
								destroyed.add(ph);
								madeChange++;
							}
						}
					}
				}
				for (Asteroid a : tempLoa) {
					for (String p : gameState.players.keySet()) {
						if (gameState.players.get(p).inContactWith(a)) {
							if (gameState.players.get(p).shieldLevel > 0) {
								gameState.players.get(p).shieldLevel -= 1;
							}
							if (gameState.players.get(p).shieldLevel <= 0) {
								gameState.loe.add(gameState.players.get(p).explode());
								// destroyedPlayers.add(p);
								madeChange++;
							}
						}
					}
					gameState.loa.removeAll(destroyed);
					gameState.loa.addAll(tempLoa);
					gameState.lop.removeAll(destroyed);
					for (String p : destroyedPlayers) {
						gameState.players.remove(p);
					}
					if ((destroyed.size() != 0) || (destroyedPlayers.size() != 0)) {
						System.out.println("Got a hit! " + (destroyed.size() + destroyedPlayers.size()));
					}
					ArrayList<Explosion> exKeepers = new ArrayList<Explosion>();
					for (Explosion ex : gameState.loe) {
						if (ex.shouldILive(System.currentTimeMillis())) {
							exKeepers.add(ex);
						}
						madeChange++;
					}
					gameState.loe = exKeepers;
					if (gameState.playersChanged() || madeChange > 0) {
						String txt = gameState.serialize();
						gameState.los.clear();
						ClientOutgoing.offer(txt);
					}
				}
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					Event event = eventQueue.take();
					handleAsteroidQuantity();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	static class TimerEventProducer implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				eventQueue.offer(new TimerEvent());
			}
		}

	}

	static class ClientOutgoing implements Runnable {

		static Set<ClientOutgoing> activeConversations = Collections.synchronizedSet(new HashSet<ClientOutgoing>());

		private Socket s;
		private BlockingQueue<String> serializationQueue = new ArrayBlockingQueue<String>(1);

		ClientOutgoing(Socket s) {
			this.s = s;
		}

		static void offer(String serializedWorld) {
			for (ClientOutgoing co : activeConversations) {
				co.serializationQueue.offer(serializedWorld);
			}
		}

		@Override
		public void run() {
			activeConversations.add(this);
			while (true) {
				try {
					String txt = this.serializationQueue.take();
					byte[] b;
					try {
						b = NetString.toNetStringBytes(txt);
						this.s.getOutputStream().write(b);
						this.s.getOutputStream().flush();
					} catch (IOException e) {
						break;
					}
				} catch (InterruptedException e) {
				}
			}
			activeConversations.remove(this);
		}

	}

	static class ClientIncoming implements Runnable {

		private Socket s;

		ClientIncoming(Socket s) {
			this.s = s;
		}

		@Override
		public void run() {
			JSONParser p = new JSONParser();
			try {
				while (true) {
					try {
						String txt = NetString.readString(s.getInputStream());
						JSONObject ob = (JSONObject) p.parse(txt);
						String action = (String) ob.get("action");
						String userid = ob.get("userid").toString();
						synchronized (gameState) {
							switch (action) {
							case "connect":
								gameState.connect(userid);
								break;
							case "left":
								gameState.left(userid);
								break;
							case "right":
								gameState.right(userid);
								break;
							case "forward":
								gameState.forward(userid);
								break;
							case "fire":
								gameState.fire(userid);
								break;
							case "disconnect":
								gameState.disconnect(userid);
								break;
							}
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
			}
		}

	}

	public static void main(String[] args) throws IOException {
		System.out.println("Server is running.");
		Thread eventMonitorThread = new Thread(new QueueMonitor());
		eventMonitorThread.start();
		Thread TimerThread = new Thread(new TimerEventProducer());
		TimerThread.start();
		ServerSocket ss = new ServerSocket(8353);
		while (true) {
			Socket s = ss.accept();
			new Thread(new ClientOutgoing(s)).start();
			new Thread(new ClientIncoming(s)).start();
		}
	}

}