package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

import util.MessageUtils;

public class ClientHandler implements Runnable {
	private Integer userId;
	private final Server server;
	private final Socket socket;
	private final BufferedReader socketInputReader;
	private final PrintStream socketOutputWriter;
	public ClientHandler(int userId, Socket socket, Server server) {

		try {
			this.userId = userId;
			this.socket = socket;
			this.server = server;
			this.socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.socketOutputWriter = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}

	}

	@Override
	public void run() {
		try {
			while (socket.isConnected()) {
				String message = socketInputReader.readLine();
				if (message == null || message.isEmpty()) {
					continue;
				}
				String[] messageParts = MessageUtils.parseMessage(message);
				String senderUsername = MessageUtils.getSenderUserName(messageParts);
				String messageContent = MessageUtils.getMessageContent(messageParts);
				List<Integer> conversationMembers = MessageUtils.getConversationMembers(messageParts);
				System.out.println("Message received: " + messageContent + " from " + senderUsername);
				for (int member : conversationMembers) {
					if (member != userId && server.getClientHandlers().containsKey(member)) {
						ClientHandler clientHandler = server.getClientHandlers().get(member);
						clientHandler.sendMessage(message);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void sendMessage(String message) {
		socketOutputWriter.println(message);
	}

}
