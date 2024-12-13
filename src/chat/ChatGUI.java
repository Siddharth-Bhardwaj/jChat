package chat;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import dto.Conversation;
import dto.Message;
import dto.User;
import util.MessageUtils;

public class ChatGUI extends JFrame {
	private static final long serialVersionUID = 639938794175271480L;

	private ChatService chatService;
	private String currentUserName;
	private int currentUserId;
	private DefaultListModel<String> chatListModel;
	private JList<String> chatList;
	private JPanel chatDisplayPanel;
	private JButton createGroupChatButton;
	private JButton findUserButton;
	private Map<String, ChatPanel> chatPanels = new HashMap<>();

	public ChatGUI(String username, int userId, PrintWriter socketOutputWriter, BufferedReader socketInputReader) {
		this.currentUserName = username;
		this.currentUserId = userId;
		this.chatService = new ChatService(socketOutputWriter);
		initializeUI();
	}

	private void initializeUI() {
		setTitle("Chat Application - " + currentUserName);
		setSize(800, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setLayout(new BorderLayout());

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BorderLayout());
		sidePanel.setPreferredSize(new Dimension(200, 600));

		chatListModel = new DefaultListModel<>();
		chatList = new JList<>(chatListModel);
		chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				String selectedChat = chatList.getSelectedValue();
				if (selectedChat != null) {
					switchChat(selectedChat);
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

		createGroupChatButton = new JButton("Create Group Chat");
		findUserButton = new JButton("Find User");

		createGroupChatButton.addActionListener(e -> createGroupChat());
		findUserButton.addActionListener(e -> findAndStartChat());

		buttonPanel.add(createGroupChatButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		buttonPanel.add(findUserButton);

		sidePanel.add(new JScrollPane(chatList), BorderLayout.CENTER);
		sidePanel.add(buttonPanel, BorderLayout.SOUTH);

		chatDisplayPanel = new JPanel(new CardLayout());

		loadExistingConversations();
		if (chatListModel.size() > 0) {
			chatList.setSelectedIndex(0);
		}

		add(sidePanel, BorderLayout.WEST);
		add(chatDisplayPanel, BorderLayout.CENTER);
	}

	private void loadExistingConversations() {
		List<Conversation> conversations = chatService.getUserConversations(currentUserId);
		for (Conversation conversation : conversations) {
			List<Message> messages = chatService.getConversationMessages(conversation.getConversationId());
			ChatPanel chatPanel = new ChatPanel(conversation.getConversationId(), conversation.getConversationName(),
					conversation.isGroupChat(), messages);
			chatPanels.put(conversation.getConversationName(), chatPanel);
			chatListModel.addElement(conversation.getConversationName());
			chatDisplayPanel.add(chatPanel, conversation.getConversationName());
		}
	}

	private void createGroupChat() {
		String groupName = JOptionPane.showInputDialog(this, "Enter Group Chat Name:");
		if (groupName != null && !groupName.trim().isEmpty() && !chatPanels.containsKey(groupName)) {
			int conversationId = chatService.createConversation(groupName, true);
			chatService.addUserToConversation(currentUserId, conversationId);

			ChatPanel groupChat = new ChatPanel(conversationId, groupName, true, null);
			chatPanels.put(groupName, groupChat);
			chatListModel.addElement(groupName);
			chatDisplayPanel.add(groupChat, groupName);
			chatList.setSelectedValue(groupName, true);
		} else {
			JOptionPane.showMessageDialog(this, "Invalid or duplicate group name!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void findAndStartChat() {
		String targetUser = JOptionPane.showInputDialog(this, "Enter Username to Chat:");
		if (targetUser != null && !targetUser.trim().isEmpty()) {
			User user = chatService.searchUsers(targetUser);
			if (user == null) {
				JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				startPrivateChat(user);
			}
		}
	}

	private void startPrivateChat(User targetUser) {
		if (chatPanels.containsKey(targetUser.getUsername())) {
			chatList.setSelectedValue(targetUser.getUsername(), true);
			return;
		}

		int conversationId = chatService.createConversation("", false);
		chatService.addUserToConversation(targetUser.getUserId(), conversationId);
		chatService.addUserToConversation(currentUserId, conversationId);

		ChatPanel privateChat = new ChatPanel(conversationId, targetUser.getUsername(), false, null);
		chatPanels.put(targetUser.getUsername(), privateChat);
		chatListModel.addElement(targetUser.getUsername());
		chatDisplayPanel.add(privateChat, targetUser.getUsername());
		chatList.setSelectedValue(targetUser.getUsername(), true);
	}

	private void switchChat(String chatName) {
		if (chatPanels.containsKey(chatName)) {
			CardLayout layout = (CardLayout) chatDisplayPanel.getLayout();
			layout.show(chatDisplayPanel, chatName);
			chatDisplayPanel.repaint();
			chatDisplayPanel.revalidate();
		}
	}
	
	public Map<String, ChatPanel> getChatPanels() {
        return chatPanels;
    }
	
	public void receiveMessage(String[] messageParts) {
		int senderId = MessageUtils.getSenderId(messageParts);
		String senderUsername = MessageUtils.getSenderUserName(messageParts);
		String messageContent = MessageUtils.getMessageContent(messageParts);
		LocalDateTime timestamp = MessageUtils.getTimestamp(messageParts);
		System.out.println("Message received: " + messageContent + " from " + senderUsername);
		if (!chatPanels.containsKey(senderUsername)) {
			User sender = new User(senderId, senderUsername);
			startPrivateChat(sender);
		}
		ChatPanel chatPanel = chatPanels.get(senderUsername);
		chatPanel.appendMessage(senderUsername, messageContent, timestamp);
	}

	protected class ChatPanel extends JPanel {
		private static final long serialVersionUID = 1736199242555770092L;

		private JTextPane chatArea;
		private JTextField messageField;
		private JButton addUsersButton;
		private JButton sendButton;
		private DefaultStyledDocument doc;
		private int conversationId;

		public ChatPanel(int conversationId, String chatName, boolean isGroupChat, List<Message> messages) {
			this.conversationId = conversationId;

			setLayout(new BorderLayout());

			chatArea = new JTextPane();
			chatArea.setEditable(false);
			doc = (DefaultStyledDocument) chatArea.getDocument();
			JScrollPane scrollPane = new JScrollPane(chatArea);

			JPanel messagePanel = new JPanel(new BorderLayout());
			messageField = new JTextField();
			sendButton = new JButton("Send");

			messagePanel.add(messageField, BorderLayout.CENTER);
			messagePanel.add(sendButton, BorderLayout.EAST);

			sendButton.addActionListener(e -> sendMessage());
			messageField.addActionListener(e -> sendMessage());

			if (isGroupChat) {
				addUsersButton = new JButton("Add Users");
				messagePanel.add(addUsersButton, BorderLayout.WEST);

				addUsersButton.addActionListener(e -> {
					String newUsers = JOptionPane.showInputDialog(ChatGUI.this,
							"Enter usernames to add (comma separated):");
					List<String> errorUsers = new ArrayList<>();
					if (newUsers != null && !newUsers.trim().isEmpty()) {
						String[] users = newUsers.split(",");
						for (String user : users) {
							User newUser = chatService.searchUsers(user.trim());
							if (newUser != null) {
								if (!chatService.addUserToConversation(newUser.getUserId(), conversationId)) {
									errorUsers.add(user);
								}
							} else {
								errorUsers.add(user);
							}
						}
						if (errorUsers.size() > 0) {
							JOptionPane.showMessageDialog(ChatGUI.this,
									"Users cannot be added: " + String.join(", ", errorUsers), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}

			if (messages != null && messages.size() > 0) {
				for (Message message : messages) {
					appendMessage(message.getSenderUsername(), message.getContent(), message.getTimestamp());
				}
			}

			add(scrollPane, BorderLayout.CENTER);
			add(messagePanel, BorderLayout.SOUTH);
		}

		private void sendMessage() {
			String message = messageField.getText().trim();
			if (!message.isEmpty()) {
				appendMessage(currentUserName, message, LocalDateTime.now());
				messageField.setText("");

				chatService.sendMessage(currentUserId, currentUserName, conversationId, message);
			}
		}

		public void appendMessage(String sender, String message, LocalDateTime timestamp) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
				String timestampString = sdf.format(Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()));

				StyleContext sc = StyleContext.getDefaultStyleContext();
				AttributeSet senderStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground,
						Color.BLUE);
				AttributeSet messageStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground,
						Color.BLACK);
				AttributeSet timestampStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground,
						Color.GRAY);

				doc.insertString(doc.getLength(), "[" + timestampString + "] ", timestampStyle);
				doc.insertString(doc.getLength(), sender + ": ", senderStyle);
				doc.insertString(doc.getLength(), message + "\n", messageStyle);

				chatArea.setCaretPosition(doc.getLength());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
}
