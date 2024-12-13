package util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
	public static final String MESSAGE_DELIMITER = "~";

	public static String createMessage(int senderId, String senderUserName, int conversationId, String messageContent,
			LocalDateTime timestamp, List<Integer> conversationMembers) {
		String message = senderId + MESSAGE_DELIMITER + senderUserName + MESSAGE_DELIMITER + conversationId + MESSAGE_DELIMITER
				+ messageContent + MESSAGE_DELIMITER + timestamp;
		for (int member : conversationMembers) {
			message += MESSAGE_DELIMITER + member;
		}
		return message;
	}
	
	public static String[] parseMessage(String message) {
		return message.split(MESSAGE_DELIMITER);
	}
	
	public static int getSenderId(String[] messageParts) {
		return Integer.parseInt(messageParts[0]);
	}
	
	public static String getSenderUserName(String[] messageParts) {
		return messageParts[1];
	}
	
	public static int getConversationId(String[] messageParts) {
		return Integer.parseInt(messageParts[2]);
	}
	
	public static String getMessageContent(String[] messageParts) {
		return messageParts[3];
	}
	
	public static LocalDateTime getTimestamp(String[] messageParts) {
		return LocalDateTime.parse(messageParts[4]);
	}
	
	public static List<Integer> getConversationMembers(String[] messageParts) {
		List<Integer> members = new ArrayList<>();
		for (int i = 5; i < messageParts.length; i++) {
			members.add(Integer.parseInt(messageParts[i]));
		}
		return members;
	}
}
