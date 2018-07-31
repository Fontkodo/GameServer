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
		List<SpaceObject> loa = new ArrayList<SpaceObject>();
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

		boolean playersChanged() {
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
			for (String key : players.keySet()) {
				Player p = players.get(key);
				ar.add(p.toJSON());
			}
			ob.put("SpaceObjects", ar);
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
			Velocity newVel = new Velocity(p.vel.x + (Math.cos(p.currentRotation))/1000, p.vel.y - (Math.sin(p.currentRotation))/1000);
			long elapsedTime = System.currentTimeMillis() - p.timestamp;
			double dx = p.vel.x*elapsedTime;
			double dy = p.vel.y*elapsedTime;
			Player newP = new Player(newVel,
					new Point2D(p.loc.getX() + dx, p.loc.getY() + dy),
					p.rotvel,
					p.userid);
			newP.currentRotation = p.currentRotation;
			players.replace(p.userid, newP);
		}

		void fire(String userid) throws IOException {
			Player p = getPlayer(userid);
		}
	}

	static GameState gameState = new GameState();

	static class QueueMonitor implements Runnable {

		void handleAsteroidQuantity() throws IOException {
			int madeChange = 0;
			ArrayList<SpaceObject> keepers = new ArrayList<SpaceObject>();
			for (SpaceObject a : gameState.loa) {
				if (a.inBounds(width, height)) {
					keepers.add(a);
				} else {
					madeChange++;
				}
			}
			while (keepers.size() < 5) {
				keepers.add(AsteroidFactory.makeAsteroid());
				madeChange++;
			}
			gameState.loa = keepers;
			if (madeChange > 0 || gameState.playersChanged()) {
				// System.out.println(gamestate.serialize());
				String txt = gameState.serialize();
				ClientOutgoing.offer(txt);
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					Event event = eventQueue.take();
					if (event instanceof UserEvent) {
						for (int i = 0; i < 10; i++) {
							gameState.loa.add(AsteroidFactory.makeAsteroid());
							System.out.println("Adding asteroid per user request");
						}
					}
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
						System.out.println(ob);
						String action = (String) ob.get("action");
						String userid = ob.get("userid").toString();
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

// public class GameServer {
//
// private static int connectionCounter;
//
// static class Conversation implements Runnable {
//
// Socket socket;
// static int counter;
//
// Conversation(Socket socket) {
// this.socket = socket;
// }
//
// @Override
// public void run() {
// counter++;
// try {
// BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
// while (true) {
// String content = NetString.readString(bis);
// System.out.println("Received: " + content);
// JSONParser p = new JSONParser();
// JSONObject ob = (JSONObject) p.parse(content);
// ob.put("transactions", connectionCounter++);
// ob.put("clients", counter);
// byte[] tempBytes = NetString.toNetStringBytes(ob.toJSONString());
// socket.getOutputStream().write(tempBytes);
// socket.getOutputStream().flush();
// System.out.println(ob.toJSONString());
// try {
// Thread.sleep(2000);
// } catch (InterruptedException e) {
//
// }
// }
// } catch (Exception e) {
//
// } finally {
// counter--;
// }
// }
// }
//
// public static void main(String[] args) throws IOException, ParseException {
// ServerSocket serverSocket = new ServerSocket(9999);
// while (true) {
// Socket s = serverSocket.accept();
// Runnable r = new Conversation(s);
// Thread t = new Thread(r);
// t.start();
// }
//
// }
//
// }
