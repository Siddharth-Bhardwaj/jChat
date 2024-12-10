package dto;

import java.time.LocalDateTime;

public class Message {
	private int id;
	private int senderId;
	private int conversationId;
	private String content;
	private LocalDateTime timestamp;

	public Message(int messageId, int senderId, int conversationId, String messageContent, LocalDateTime timestamp) {
		this.id = messageId;
		this.senderId = senderId;
		this.conversationId = conversationId;
		this.content = messageContent;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}

	public int getSenderId() {
		return senderId;
	}

	public int getConversationId() {
		return conversationId;
	}

	public String getContent() {
		return content;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
