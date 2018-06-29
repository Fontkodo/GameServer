package gameserver;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.*;

public class GameServer {

	private static int connectionCounter;

	static class Conversation implements Runnable {

		Socket socket;
		static int counter;

		Conversation(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			counter++;
			try {
				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				while (true) {
					String content = NetString.readString(bis);
					System.out.println("Received: " + content);
					JSONParser p = new JSONParser();
					JSONObject ob = (JSONObject) p.parse(content);
					ob.put("transactions", connectionCounter++);
					ob.put("clients", counter);
					byte[] tempBytes = NetString.toNetStringBytes(ob.toJSONString());
					socket.getOutputStream().write(tempBytes);
					socket.getOutputStream().flush();
					System.out.println(ob.toJSONString());
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {

					}
				}
			} catch (Exception e) {

			} finally {
				counter--;
			}
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		ServerSocket serverSocket = new ServerSocket(9999);
		while (true) {
			Socket s = serverSocket.accept();
			Runnable r = new Conversation(s);
			Thread t = new Thread(r);
			t.start();
		}

	}

}
