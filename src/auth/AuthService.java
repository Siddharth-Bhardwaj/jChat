package auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DatabaseUtils;
import util.PasswordUtils;

public class AuthService {

	public boolean registerUser(String username, String password, String firstName, String lastName) {
		String salt = PasswordUtils.generateSalt();
		String hashedPassword = PasswordUtils.hashPassword(password, salt);

		try (Connection conn = DatabaseUtils.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(
						"INSERT INTO users (username, password, salt, first_name, last_name) VALUES (?, ?, ?, ?, ?)")) {

			pstmt.setString(1, username);
			pstmt.setString(2, hashedPassword);
			pstmt.setString(3, salt);
			pstmt.setString(4, firstName);
			pstmt.setString(5, lastName);

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Integer authenticateUser(String username, String password) {
		try (Connection conn = DatabaseUtils.getConnection();
				PreparedStatement pstmt = conn
						.prepareStatement("SELECT id, password, salt FROM users WHERE username = ?")) {

			pstmt.setString(1, username);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					String storedHash = rs.getString("password");
					String storedSalt = rs.getString("salt");

					if (PasswordUtils.verifyPassword(password, storedHash, storedSalt)) {
						return rs.getInt("id");
					}
				}
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
