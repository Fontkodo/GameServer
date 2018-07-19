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
	static BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(1);

	static class GameState {
		List<Asteroid> loa = new ArrayList<Asteroid>();

		String serialize() {
			JSONObject ob = new JSONObject();
			JSONArray ar = new JSONArray();
			for (Asteroid a : loa) {
				ar.add(a.toJSON());
			}
			ob.put("SpaceObjects", ar);
			JSONObject tempDim = new JSONObject();
			tempDim.put("Width", width);
			tempDim.put("Height", height);
			ob.put("Dimensions", tempDim);
			return ob.toJSONString();
		}
	}

	static class QueueMonitor implements Runnable {
		GameState gamestate = new GameState();

		void handleAsteroidQuantity() throws IOException {
			int madeChange = 0;
			ArrayList<Asteroid> keepers = new ArrayList<Asteroid>();
			for (Asteroid a : gamestate.loa) {
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
			gamestate.loa = keepers;
			if (madeChange > 0) {
				// System.out.println(gamestate.serialize());
				String txt = gamestate.serialize();
				ClientConversation.offer(txt);
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					Event event = eventQueue.take();
					if (event instanceof UserEvent) {
						for (int i = 0; i < 10; i++) {
							gamestate.loa.add(AsteroidFactory.makeAsteroid());
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

	static class ClientConversation implements Runnable {
		
		static Set<ClientConversation> activeConversations = Collections.synchronizedSet(new HashSet<ClientConversation>());

		private Socket s;
		private BlockingQueue<String> serializationQueue = new ArrayBlockingQueue<String>(1);

		ClientConversation(Socket s) {
			this.s = s;
		}
		
		static void offer(String serializedWorld) {
			for(ClientConversation cc : activeConversations) {
				cc.serializationQueue.offer(serializedWorld);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			activeConversations.remove(this);
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
			Thread t = new Thread(new ClientConversation(s));
			t.start();
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
