package auth;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import chat.ChatGUI;

public class AuthGUI extends JFrame {
    private AuthService authService;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton signupButton;

    public AuthGUI() {
        authService = new AuthService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("jChat - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("jChat", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");
        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        loginButton.addActionListener(e -> loginUser());
        signupButton.addActionListener(e -> openSignupDialog());

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(usernamePanel);
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }

    private void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password", 
                "Login Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (authService.authenticateUser(username, password)) {
                openChatWindow(username);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invalid username or password", 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Login error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSignupDialog() {
        JDialog signupDialog = new JDialog(this, "Sign Up", true);
        signupDialog.setSize(400, 400);
        signupDialog.setLocationRelativeTo(this);

        JPanel signupPanel = new JPanel();
        signupPanel.setLayout(new BoxLayout(signupPanel, BoxLayout.Y_AXIS));
        signupPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField newFirstNameField = new JTextField(20);
        JLabel newFirstNameLabel = new JLabel("First Name:");

        JTextField newLastNameField = new JTextField(20);
        JLabel newLastNameLabel = new JLabel("Last Name:");

        JTextField newUsernameField = new JTextField(20);
        JLabel newUsernameLabel = new JLabel("Username:");

        JPasswordField newPasswordField = new JPasswordField(20);
        JLabel newPasswordLabel = new JLabel("Password:");

        JButton confirmSignupButton = new JButton("Create Account");
        confirmSignupButton.addActionListener(e -> {
        	String firstName = newFirstNameField.getText();
        	String lastName = newLastNameField.getText();
            String username = newUsernameField.getText();
            String password = new String(newPasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty()) {
                JOptionPane.showMessageDialog(signupDialog, 
                    "Please fill all required fields", 
                    "Signup Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (authService.registerUser(username, password, firstName, lastName)) {
                    JOptionPane.showMessageDialog(signupDialog, "Account Created Successfully!");
                    signupDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(signupDialog, 
                        "Username already exists", 
                        "Signup Failed", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(signupDialog, 
                    "Signup error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        signupPanel.add(newFirstNameLabel);
        signupPanel.add(newFirstNameField);
        signupPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        signupPanel.add(newLastNameLabel);
        signupPanel.add(newLastNameField);
        signupPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        signupPanel.add(newUsernameLabel);
        signupPanel.add(newUsernameField);
        signupPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        signupPanel.add(newPasswordLabel);
        signupPanel.add(newPasswordField);
        signupPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        signupPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        signupPanel.add(confirmSignupButton);

        signupDialog.add(signupPanel);
        signupDialog.setVisible(true);
    }

    private void openChatWindow(String username) {
    	// Close the login window
        this.dispose();

        // Open the chat window on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ChatGUI chatWindow = new ChatGUI(username);
            chatWindow.setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AuthGUI().setVisible(true);
        });
    }
}
