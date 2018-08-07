package gameserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.*;

public class GameServer {

	static long width = 1400;
	static long height = 800;

	static GameState gameState = new GameState();

	static class GameStateMutator implements Runnable {

		void handleAsteroidQuantity() throws IOException {
			synchronized (gameState) {
				boolean changed = false;
				ArrayList<Asteroid> keepers = new ArrayList<Asteroid>();
				for (Asteroid a : gameState.loa) {
					if (a.inBounds(width, height)) {
						keepers.add(a);
					} else {
						changed = true;
					}
				}
				if (keepers.stream().mapToDouble(a -> a.getArea()).sum() < 40000) {
					while (keepers.stream().mapToDouble(a -> a.getArea()).sum() < 50000) {
						keepers.add(AsteroidFactory.makeAsteroid());
						changed = true;
					}
				}
				gameState.loa = keepers;
				ArrayList<Photon> phKeepers = new ArrayList<Photon>();
				for (Photon ph : gameState.lop) {
					if (ph.inBounds(width, height)) {
						phKeepers.add(ph);
					} else {
						changed = true;
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
							ph.player.score += 1;
							if (gameState.highScore.get(ph.player.userid) < ph.player.score) {
								gameState.highScore.replace(ph.player.userid, ph.player.score);
							}
							changed = true;
						}
					}
					for (String p : gameState.players.keySet()) {
						if (gameState.players.get(p).inContactWith(ph)
								&& (ph.player != gameState.players.get(p) && gameState.players.get(p).vulnerable())) {
							if (gameState.players.get(p).shieldLevel > 0) {
								gameState.players.get(p).shieldLevel -= 1;
								gameState.players.get(p).lastInjury = System.currentTimeMillis();
							}
							if (gameState.players.get(p).shieldLevel <= 0) {
								gameState.loe.add(gameState.players.get(p).explode());
								destroyedPlayers.add(p);
								destroyed.add(ph);
								changed = true;
							}
						}
					}
				}
				ArrayList<Asteroid> tempLoa2 = new ArrayList<Asteroid>();
				for (Player p : gameState.players.values()) {
					for (Asteroid a : keepers) {
						if (p.inContactWith(a) && p.vulnerable() && !a.isGeode()) {
							if (p.shieldLevel > 0) {
								p.shieldLevel -= 1;
								p.lastInjury = System.currentTimeMillis();
							}
							if (p.shieldLevel <= 0) {
								gameState.loe.add(p.explode());
								destroyedPlayers.add(p.userid);
							}
							tempLoa2.addAll(a.giveBirth());
							gameState.loe.add(a.explode());
							keepers.remove(a);
							changed = true;
							break;
						}
						if (p.inContactWith(a) && a.isGeode()) {
							p.score += 1;
							p.photonCount = 100;
							p.fuel = 200;
							p.shieldLevel = 10;
							keepers.remove(a);
							changed = true;
							break;
						}
					}
				}
				keepers.addAll(tempLoa2);
				gameState.loa.removeAll(destroyed);
				gameState.loa.addAll(tempLoa);
				gameState.lop.removeAll(destroyed);
				for (String p : destroyedPlayers) {
					gameState.respawn(p);
				}
				ArrayList<Explosion> exKeepers = new ArrayList<Explosion>();
				for (Explosion ex : gameState.loe) {
					if (ex.shouldILive(System.currentTimeMillis())) {
						exKeepers.add(ex);
					}
					changed = true;
				}
				gameState.loe = exKeepers;
				if (gameState.playersChanged() || changed) {
					String txt = gameState.serialize();
					gameState.los.clear();
					ClientOutgoing.offer(txt);
				}

			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					// eventQueue.take();
					Thread.sleep(10);
					handleAsteroidQuantity();
				} catch (IOException | InterruptedException e) {
					;
				}
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
				co.serializationQueue.poll();
				co.serializationQueue.offer(serializedWorld);
			}
		}

		@Override
		public void run() {
			activeConversations.add(this);
			try {
				while (true) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					do {
						String txt = this.serializationQueue.take();
						byte[] b;
						b = NetString.toNetStringBytes(txt);
						baos.write(b);
					} while (this.serializationQueue.peek() != null);
					this.s.getOutputStream().write(baos.toByteArray());
					this.s.getOutputStream().flush();

				}
			} catch (InterruptedException | IOException e) {
				;
			} finally {
				activeConversations.remove(this);
				System.out.println("terminating server side of port " + s.getPort());
			}
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
						case "backward":
							gameState.backward(userid);
							break;
						case "fire":
							gameState.fire(userid);
							break;
						case "disconnect":
							gameState.disconnect(userid);
							break;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("terminating client side of port " + s.getPort());
			}
		}

	}

	public static void main(String[] args) throws IOException {
		System.out.println("Server is running.");
		Thread eventMonitorThread = new Thread(new GameStateMutator());
		eventMonitorThread.start();
		ServerSocket ss = new ServerSocket(8353);
		while (true) {
			Socket s = ss.accept();
			new Thread(new ClientOutgoing(s)).start();
			new Thread(new ClientIncoming(s)).start();
		}
	}

}