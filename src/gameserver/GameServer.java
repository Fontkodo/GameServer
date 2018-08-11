package gameserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import fontkodo.netstring.NetString;

public class GameServer {

	static long width = 1400;
	static long height = 800;

	static class GameStateMutator implements Runnable {

		final GameState gameState;

		GameStateMutator(GameState gameState) {
			this.gameState = gameState;
		}

		void updateState() throws IOException {

			final long currentTimeMillis = System.currentTimeMillis();
			boolean changed = false;

			synchronized (gameState) {

				ArrayList<Asteroid> keepers = new ArrayList<Asteroid>();
				for (Asteroid a : gameState.loa) {
					if (a.inBounds(width, height)) {
						keepers.add(a);
					} else {
						changed = true;
					}
				}

				{
					double totalArea = keepers.stream().mapToDouble(a -> a.getArea()).sum();
					if(totalArea < 40_000) {
						while(totalArea < 50_000) {
							Asteroid a = AsteroidFactory.makeAsteroid();
							keepers.add(a);
							totalArea += a.getArea();
							changed = true;
						}
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
				ArrayList<Asteroid> tempLoa = new ArrayList<Asteroid>();
				List<Player> playersToDestroy = new ArrayList<Player>();
				for (Photon ph : gameState.lop) {
					for (Asteroid a : gameState.loa) {
						if (a.inContactWith(ph)) {
							gameState.loe.add(a.explode());
							tempLoa.addAll(a.giveBirth());
							destroyed.add(a);
							destroyed.add(ph);
							ph.player.score += 1;
							gameState.updatePossibleHighScore(ph.player.userid, ph.player.score);
							changed = true;
						}
					}
					for (Player p : gameState.players.values()) {
						if (p.inContactWith(ph) && (ph.player != p && p.vulnerable())) {
							destroyed.add(ph);
							changed = true;
							if (p.shieldLevel > 0) {
								p.shieldLevel -= 1;
								p.lastInjury = currentTimeMillis;
							}
							if (p.shieldLevel <= 0) {
								gameState.loe.add(p.explode());
								playersToDestroy.add(p);
								break;
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
								p.lastInjury = currentTimeMillis;
							}
							if (p.shieldLevel <= 0) {
								gameState.loe.add(p.explode());
								playersToDestroy.add(p);
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
				if (playersToDestroy.size() > 0) {
					Map<String, Player> newPlayers = new HashMap<String, Player>();
					for (String k : gameState.players.keySet()) {
						Player p = gameState.players.get(k);
						if (!playersToDestroy.contains(p)) {
							newPlayers.put(k, p);
						}
					}
					gameState.players = newPlayers;
				}

				ArrayList<Explosion> exKeepers = new ArrayList<Explosion>();
				for (Explosion ex : gameState.loe) {
					if (ex.shouldILive(currentTimeMillis)) {
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
					Thread.sleep(1);
					updateState();
				} catch (IOException | InterruptedException e) {
					;
				}
			}
		}

	}

	static class ClientOutgoing implements Runnable {

		final private static Set<ClientOutgoing> activeConversations = new HashSet<ClientOutgoing>();

		final private Socket s;
		final private BlockingQueue<String> serializationQueue = new ArrayBlockingQueue<String>(1);

		ClientOutgoing(Socket s) {
			this.s = s;
		}

		static void offer(String serializedWorld) {
			synchronized (activeConversations) {
				for (ClientOutgoing co : activeConversations) {
					co.serializationQueue.poll();
					co.serializationQueue.offer(serializedWorld);
				}
			}
		}

		@Override
		public void run() {

			synchronized (activeConversations) {
				activeConversations.add(this);
			}

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
				synchronized (activeConversations) {
					activeConversations.remove(this);
				}
				// System.out.println("terminating server side of port " + s.getPort());
			}
		}

	}

	static class ClientIncoming implements Runnable {

		final private Socket s;
		final private GameState gameState;

		ClientIncoming(Socket s, GameState gameState) {
			this.s = s;
			this.gameState = gameState;
		}

		@Override
		public void run() {
			final int session = this.hashCode();
			String userid = null;
			JSONParser p = new JSONParser();
			try {
				while (true) {
					String txt = NetString.readString(s.getInputStream());
					JSONObject ob = (JSONObject) p.parse(txt);
					String action = (String) ob.get("action");
					userid = ob.get("userid").toString();
					synchronized (gameState) {
						switch (action) {
						case "connect":
							gameState.connect(userid);
							System.out.println(userid + " joined at " + new Date() + " session " + session);
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
						default:
							System.err.println("unknown client command [" + gameState + "]");
						}
					}
				}
			} catch (Exception e) {
				// System.out.println("terminating client side of port " + s.getPort());
			}
			if (userid != null) {
				try {
					synchronized (gameState) {
						gameState.disconnect(userid);
					}
					System.out.println(userid + " left at " + new Date() + " session " + session);
				} catch (IOException e) {
				}
			}
		}

	}

	public static void main(String[] args) throws IOException {
		System.out.println("Server is running.");
		GameState gameState = new GameState();
		new Thread(new GameStateMutator(gameState)).start();
		ServerSocket ss = new ServerSocket(6081, 0, InetAddress.getByName("127.0.0.1"));
		while (true) {
			Socket s = ss.accept();
			new Thread(new ClientOutgoing(s)).start();
			new Thread(new ClientIncoming(s, gameState)).start();
		}
	}

}