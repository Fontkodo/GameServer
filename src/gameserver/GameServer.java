package gameserver;

import java.io.*;
import java.net.*;

import org.json.simple.*;
import org.json.simple.parser.*;

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
		OutputStreamWriter obw = new OutputStreamWriter(socket.getOutputStream());
		obw.write(ob.toJSONString());
		obw.flush();
		System.out.println(ob);
	}

	public static void main(String[] args) throws IOException, ParseException {
		ServerSocket serverSocket = new ServerSocket(8353);
		while(true) {
			Socket s = serverSocket.accept();
			System.out.println(serverSocket);
			conversation(s);
			s.close();
		}

	}

}
