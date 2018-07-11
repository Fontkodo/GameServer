package gameserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.*;

public class GameServer {
	static BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(1);
	
	static class GameState {
		List<Asteroid> loa = new ArrayList<Asteroid>();
	}
	
	static class QueueMonitor implements Runnable {
		GameState gamestate = new GameState();
		
		void handleAsteroidQuantity() throws IOException {
			int madeChange = 0;
			ArrayList<Asteroid> keepers = new ArrayList<Asteroid>();
			for (Asteroid a : gamestate.loa) {
				if (a.inBounds(800, 800)) {
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
				System.out.println("Made " + madeChange + " changes.");
				for (Asteroid a : gamestate.loa) {
					System.out.println(a.imgURL);
				}
			}
		}

		@Override
		public void run() {
			while (true) {
				try {
					Event event = eventQueue.take();
					if (event instanceof UserEvent) {
						for (int i = 0 ; i < 10 ; i++) {
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
	
	public static void main(String[] args) throws IOException {
		Thread eventMonitorThread = new Thread(new QueueMonitor());
		eventMonitorThread.start();
		Thread TimerThread = new Thread(new TimerEventProducer());
		TimerThread.start();
		ServerSocket ss = new ServerSocket(8353);
		while (true) {
			Socket s = ss.accept();
			s.close();
			eventQueue.offer(new UserEvent());
			
		}
	}

}

//public class GameServer {
//
//	private static int connectionCounter;
//
//	static class Conversation implements Runnable {
//
//		Socket socket;
//		static int counter;
//
//		Conversation(Socket socket) {
//			this.socket = socket;
//		}
//
//		@Override
//		public void run() {
//			counter++;
//			try {
//				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
//				while (true) {
//					String content = NetString.readString(bis);
//					System.out.println("Received: " + content);
//					JSONParser p = new JSONParser();
//					JSONObject ob = (JSONObject) p.parse(content);
//					ob.put("transactions", connectionCounter++);
//					ob.put("clients", counter);
//					byte[] tempBytes = NetString.toNetStringBytes(ob.toJSONString());
//					socket.getOutputStream().write(tempBytes);
//					socket.getOutputStream().flush();
//					System.out.println(ob.toJSONString());
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e) {
//
//					}
//				}
//			} catch (Exception e) {
//
//			} finally {
//				counter--;
//			}
//		}
//	}
//
//	public static void main(String[] args) throws IOException, ParseException {
//		ServerSocket serverSocket = new ServerSocket(9999);
//		while (true) {
//			Socket s = serverSocket.accept();
//			Runnable r = new Conversation(s);
//			Thread t = new Thread(r);
//			t.start();
//		}
//
//	}
//
//}
