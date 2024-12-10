package chat;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class ChatGUI extends JFrame {
	private String currentUser;
    private JTabbedPane chatTabs;
    private JButton createGroupChatButton;
    private JButton findUserButton;
    private Map<String, ChatPanel> chatPanels = new HashMap<>();

    public ChatGUI(String username) {
        this.currentUser = username;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Chat Application - " + currentUser);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main layout
        setLayout(new BorderLayout());

        // Side panel for actions
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        
        createGroupChatButton = new JButton("Create Group Chat");
        findUserButton = new JButton("Find User");
        
        sidePanel.add(createGroupChatButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(findUserButton);

        // Tabbed pane for chats
        chatTabs = new JTabbedPane();

        // Add action listeners
        createGroupChatButton.addActionListener(e -> createGroupChat());
        findUserButton.addActionListener(e -> findAndStartChat());

        // Add components to frame
        add(sidePanel, BorderLayout.WEST);
        add(chatTabs, BorderLayout.CENTER);

        // Add default welcome chat
        addWelcomeChat();
    }

    private void addWelcomeChat() {
        ChatPanel welcomePanel = new ChatPanel("Welcome", true);
        welcomePanel.appendMessage("System", "Welcome to the Chat Application, " + currentUser + "!");
        chatPanels.put("Welcome", welcomePanel);
        chatTabs.addTab("Welcome", welcomePanel);
    }

    private void createGroupChat() {
        String groupName = JOptionPane.showInputDialog(this, "Enter Group Chat Name:");
        if (groupName != null && !groupName.trim().isEmpty()) {
            ChatPanel groupChat = new ChatPanel(groupName, true);
            chatPanels.put(groupName, groupChat);
            chatTabs.addTab(groupName, groupChat);
            chatTabs.setSelectedComponent(groupChat);
        }
    }

    private void findAndStartChat() {
        String targetUser = JOptionPane.showInputDialog(this, "Enter Username to Chat:");
        if (targetUser != null && !targetUser.trim().isEmpty()) {
            startPrivateChat(targetUser);
        }
    }

    private void startPrivateChat(String targetUser) {
        // Check if chat already exists
        if (chatPanels.containsKey(targetUser)) {
            chatTabs.setSelectedComponent(chatPanels.get(targetUser));
            return;
        }

        ChatPanel privateChat = new ChatPanel(targetUser, false);
        chatPanels.put(targetUser, privateChat);
        chatTabs.addTab(targetUser, privateChat);
        chatTabs.setSelectedComponent(privateChat);
    }

    // Inner class for individual chat panels
    private class ChatPanel extends JPanel {
        private JTextPane chatArea;
        private JTextField messageField;
        private JButton sendButton;
        private String chatName;
        private boolean isGroupChat;
        private DefaultStyledDocument doc;

        public ChatPanel(String chatName, boolean isGroupChat) {
            this.chatName = chatName;
            this.isGroupChat = isGroupChat;
            
            setLayout(new BorderLayout());

            // Chat display area
            chatArea = new JTextPane();
            chatArea.setEditable(false);
            doc = (DefaultStyledDocument) chatArea.getDocument();
            JScrollPane scrollPane = new JScrollPane(chatArea);

            // Message input area
            JPanel messagePanel = new JPanel(new BorderLayout());
            messageField = new JTextField();
            sendButton = new JButton("Send");

            messagePanel.add(messageField, BorderLayout.CENTER);
            messagePanel.add(sendButton, BorderLayout.EAST);

            // Add action listeners
            sendButton.addActionListener(e -> sendMessage());
            messageField.addActionListener(e -> sendMessage());

            // Add components
            add(scrollPane, BorderLayout.CENTER);
            add(messagePanel, BorderLayout.SOUTH);
        }

        private void sendMessage() {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                appendMessage(currentUser, message);
                messageField.setText("");

                // TODO: Implement actual message sending logic
                // This would typically involve:
                // 1. Sending message to server
                // 2. Persisting message in database
                // 3. Routing to correct recipient(s)
            }
        }

        public void appendMessage(String sender, String message) {
            try {
                // Timestamp
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String timestamp = sdf.format(new Date());

                // Create styles
                StyleContext sc = StyleContext.getDefaultStyleContext();
                AttributeSet senderStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, 
                    StyleConstants.Foreground, Color.BLUE);
                AttributeSet messageStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, 
                    StyleConstants.Foreground, Color.BLACK);
                AttributeSet timestampStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, 
                    StyleConstants.Foreground, Color.GRAY);

                // Insert timestamp
                doc.insertString(doc.getLength(), 
                    "[" + timestamp + "] ", 
                    timestampStyle);

                // Insert sender
                doc.insertString(doc.getLength(), 
                    sender + ": ", 
                    senderStyle);

                // Insert message
                doc.insertString(doc.getLength(), 
                    message + "\n", 
                    messageStyle);

                // Scroll to bottom
                chatArea.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}
