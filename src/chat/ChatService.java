package chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import dto.Conversation;
import dto.Message;
import dto.User;
import util.DatabaseUtils;

public class ChatService {
	private Connection connection;

	public ChatService() {
		try {
			this.connection = DatabaseUtils.getConnection();
		} catch (SQLException e) {
			throw new RuntimeException("Database connection failed", e);
		}
	}

	public int createConversation(String conversationName, boolean isGroupChat) {
		String sql = "INSERT INTO conversations (conversation_name, is_group_chat) " + "VALUES (?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, conversationName);
			pstmt.setBoolean(2, isGroupChat);

			pstmt.executeUpdate();

			try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				}
				throw new SQLException("Creating conversation failed, no ID returned.");
			}
		} catch (SQLException e) {
			throw new RuntimeException("Conversation creation failed", e);
		}
	}

	public void addUserToConversation(int userId, int conversationId) {
		String sql = "INSERT INTO user_conversations (user_id, conversation_id) " + "VALUES (?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, conversationId);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Adding user to conversation failed", e);
		}
	}

	public void sendMessage(int senderId, int conversationId, String messageContent) {
		String sql = "INSERT INTO messages (sender_id, conversation_id, content, timestamp) " + "VALUES (?, ?, ?, ?)";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, senderId);
			pstmt.setInt(2, conversationId);
			pstmt.setString(3, messageContent);
			pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));

			pstmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Sending message failed", e);
		}
	}

	public List<Message> getConversationMessages(int conversationId) {
		List<Message> messages = new ArrayList<>();

		String sql = "SELECT m.id, m.sender_id, u.username, m.content, m.timestamp " + "FROM messages m "
				+ "JOIN users u ON m.sender_id = u.id " + "WHERE m.conversation_id = ? " + "ORDER BY m.timestamp";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, conversationId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Message message = new Message(rs.getInt("id"), rs.getInt("sender_id"), rs.getInt("conversation_id"),
							rs.getString("content"), rs.getTimestamp("timestamp").toLocalDateTime());
					messages.add(message);
				}
			}

			return messages;
		} catch (SQLException e) {
			throw new RuntimeException("Retrieving messages failed", e);
		}
	}

	public List<User> searchUsers(String searchTerm) {
		List<User> users = new ArrayList<>();

		String sql = "SELECT id, username, first_name, last_name FROM users "
				+ "WHERE username LIKE ? OR first_name LIKE ? OR last_name LIKE ?";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			String likePattern = "%" + searchTerm + "%";
			pstmt.setString(1, likePattern);
			pstmt.setString(2, likePattern);
			pstmt.setString(3, likePattern);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("first_name"),
							rs.getString("last_name"));
					users.add(user);
				}
			}

			return users;
		} catch (SQLException e) {
			throw new RuntimeException("User search failed", e);
		}
	}

	public List<Conversation> getUserConversations(int userId) {
		List<Conversation> conversations = new ArrayList<>();

		String sql = "SELECT c.conversation_id, c.conversation_name, c.is_group_chat " + "FROM conversations c "
				+ "JOIN user_conversations uc ON c.conversation_id = uc.conversation_id " + "WHERE uc.user_id = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setInt(1, userId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Conversation conversation = new Conversation(rs.getInt("conversation_id"),
							rs.getString("conversation_name"), rs.getBoolean("is_group_chat"));
					conversations.add(conversation);
				}
			}

			return conversations;
		} catch (SQLException e) {
			throw new RuntimeException("Retrieving user conversations failed", e);
		}
	}

}