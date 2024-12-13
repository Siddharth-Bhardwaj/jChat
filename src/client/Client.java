package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.SwingUtilities;

import auth.AuthGUI;
import chat.ChatGUI;
import dto.User;
import util.MessageUtils;

public class Client implements Runnable {

	private User user;
	private boolean isConnected = false;
	private Socket socket;
	private PrintWriter socketOutputWriter;
	private BufferedReader socketInputReader;
	private ChatGUI chatWindow;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		AuthGUI authGUI = new AuthGUI();
		authGUI.setVisible(true);

		while (true) {
			if (!authGUI.isVisible() || !authGUI.isDisplayable()) {
				if (authGUI.getUser() != null) {
					user = authGUI.getUser();

					try {
						socket = new Socket("localhost", 9999);
						socketOutputWriter = new PrintWriter(socket.getOutputStream(), true);
						socketOutputWriter.println(user.getUserId() + "," + user.getUsername());
						socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						isConnected = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			}
			Thread.yield();
		}

		if (isConnected) {
			openChatWindow();
			while (socket.isConnected()) {
				try {
					String message = socketInputReader.readLine();
					if (message == null || message.isEmpty()) {
						continue;
					}
					String[] messageParts = MessageUtils.parseMessage(message);
					System.out.println("Message received: " + message);
					chatWindow.receiveMessage(messageParts);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void openChatWindow() {
		SwingUtilities.invokeLater(() -> {
			chatWindow = new ChatGUI(user.getUsername(), user.getUserId(), socketOutputWriter, socketInputReader);
			chatWindow.setVisible(true);
		});
	}

}
