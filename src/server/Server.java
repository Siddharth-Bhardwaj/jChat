package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {

	private Map<Integer, ClientHandler> clientHandlers = new HashMap<>();
	private boolean isRunning = true;
	private ServerSocket serverSocket;

	@Override
	public void run() {
		while (isRunning) {
			try {
				serverSocket = new ServerSocket(9999);
				serverSocket.setReuseAddress(true);
				while (!serverSocket.isClosed()) {
					Socket conn = serverSocket.accept();
					BufferedReader socketInputReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String[] userInfo = socketInputReader.readLine().split(",");
					Integer userId = Integer.parseInt(userInfo[0]);
					String userName = userInfo[1];
					System.out.println("User connected: " + userId);
					ClientHandler clientHandler = new ClientHandler(userId, conn, this);
					clientHandlers.put(userId, clientHandler);
					new Thread(clientHandler).start();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				isRunning = false;
				e.printStackTrace();
			}
		}
	}

	public Map<Integer, ClientHandler> getClientHandlers() {
		return clientHandlers;
	}

}
