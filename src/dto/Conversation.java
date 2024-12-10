package dto;

public class Conversation {
	private int id;
	private String conversationName;
	private boolean isGroupChat;

	public Conversation(int conversationId, String conversationName, boolean isGroupChat) {
		this.id = conversationId;
		this.conversationName = conversationName;
		this.isGroupChat = isGroupChat;
	}

	public int getConversationId() {
		return id;
	}

	public String getConversationName() {
		return conversationName;
	}

	public boolean isGroupChat() {
		return isGroupChat;
	}
	
	public void setConversationName(String conversationName) {
		this.conversationName = conversationName;
	}
}
