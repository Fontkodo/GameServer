package gameserver;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.*;

public class GameServer {

	private static int connectionCounter;

	public static void conversation(Socket socket) throws IOException, ParseException {
		BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
		while (true) {
			String content = NetString.readString(bis);
			System.out.println("Received: " + content);
			JSONParser p = new JSONParser();
			JSONObject ob = (JSONObject) p.parse(content);
			ob.put("connection", connectionCounter++);
			byte[] tempBytes = NetString.toNetStringBytes(ob.toJSONString());
			socket.getOutputStream().write(tempBytes);
			socket.getOutputStream().flush();
			System.out.println(ob.toJSONString());
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		ServerSocket serverSocket = new ServerSocket(9999);
		while (true) {
			Socket s = serverSocket.accept();
			System.out.println(serverSocket);
			try {
				conversation(s);
				//s.close();
			} catch (Exception e) {
				System.err.println(e);
			}
		}

	}

}
