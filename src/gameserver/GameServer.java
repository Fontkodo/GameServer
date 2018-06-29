package gameserver;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

import fontkodo.netstring.*;

public class GameServer {
	
	private static int connectionCounter;
	
	public static void conversation(Socket socket) throws IOException, ParseException {
		OutputStream os = socket.getOutputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		char [] stuff = new char[100_000];
		int n = br.read(stuff);
		CharArrayReader car = new CharArrayReader(stuff, 0, n);
		JSONParser p = new JSONParser();
		JSONObject ob = (JSONObject) p.parse(car);
		ob.put("connection", connectionCounter++);
		byte[] tempBytes = NetString.toNetStringBytes(ob.toJSONString());
		socket.getOutputStream().write(tempBytes);
		socket.getOutputStream().flush();
		System.out.println(ob.toJSONString());
	}

	public static void main(String[] args) throws IOException, ParseException {
		ServerSocket serverSocket = new ServerSocket(9999);
		while(true) {
			Socket s = serverSocket.accept();
			System.out.println(serverSocket);
			conversation(s);
			s.close();
		}

	}

}
