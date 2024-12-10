package dto;

public class User {
	private int id;
	private String username;
	private String firstName;
	private String lastName;

	public User(int userId, String username, String email, String lastName) {
		this.id = userId;
		this.username = username;
		this.firstName = email;
		this.lastName = lastName;
	}

	public int getUserId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
}
