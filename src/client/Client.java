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
	private PrintWriter socketOutputWriter;
	private BufferedReader socketInputReader;
	private ChatGUI chatWindow;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		AuthGUI authGUI = new AuthGUI();
		authGUI.setVisible(true);

		while (authGUI.isVisible() && authGUI.isDisplayable()) {
			try {
				Thread.sleep(100); // Poll less frequently
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}

		if (authGUI.getUser() != null) {
			user = authGUI.getUser();
			try (Socket socket = new Socket("localhost", 9999)) {
				socketOutputWriter = new PrintWriter(socket.getOutputStream(), true);
				socketInputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				isConnected = true;

				socketOutputWriter.println(user.getUserId() + "," + user.getUsername());
				openChatWindow();

				while (socket.isConnected()) {
					String message = socketInputReader.readLine();
					if (message != null && !message.isEmpty()) {
						String[] messageParts = MessageUtils.parseMessage(message);
						int senderId = MessageUtils.getSenderId(messageParts);
						if (senderId > 0) {
							SwingUtilities.invokeLater(() -> chatWindow.receiveMessage(messageParts));
						} else {
							SwingUtilities.invokeLater(() -> chatWindow.addGroup(messageParts));
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
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
