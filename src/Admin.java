import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Admin {
    int adminID;
    String name;
    String email;

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    Admin(int adminID, String name, String email) {
        this.adminID = adminID;
        this.name = name;
        this.email = email;
    }

    public void adminLogin(Scanner sc, Connection con) {
        try {
            System.out.print("Enter Admin Email: ");
            String email = sc.nextLine();

            if (email.trim().isEmpty()) {
                System.out.println("Email cannot be empty.");
                return;
            }

            String sql = "SELECT UserID, Name, Email FROM Users WHERE Email = ? AND Role = 'Admin'";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        this.adminID = rs.getInt("UserID");
                        this.name = rs.getString("Name");
                        this.email = rs.getString("Email");
                        System.out.println("Admin login successful!");
                        adminMenu(sc, con);
                    } else {
                        System.out.println("Invalid Admin credentials or role.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during admin login: " + e.getMessage());
        }
    }

    private void adminMenu(Scanner sc, Connection con) {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Add Book");
            System.out.println("2. Add User");
            System.out.println("3. View All Books");
            System.out.println("4. Delete Book");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Consume invalid input
                continue;
            }
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    Book.addBook(sc, con);
                    break;
                case 2:
                    addUser(sc, con);
                    break;
                case 3:
                    Book.viewBooks(con);
                    break;
                case 4:
                    Book.deleteBook(sc, con);
                    break;
                case 5:
                    System.out.println("Logging out Admin...");
                    return;
                default:
                    System.out.println("Invalid choice! Please enter a number between 1 and 5.");
            }
        }
    }

    public void addUser(Scanner sc, Connection con) {
        try {
            System.out.print("Enter User Name: ");
            String name = sc.nextLine();
            if (name.trim().isEmpty()) {
                System.out.println("User name cannot be empty.");
                return;
            }

            System.out.print("Enter User Email: ");
            String email = sc.nextLine();
            if (email.trim().isEmpty() || !email.contains("@")) { // Basic email format check
                System.out.println("Invalid email format.");
                return;
            }

            System.out.print("Enter User Role (Admin/User): ");
            String role = sc.nextLine();
            if (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("User")) {
                System.out.println("Invalid role. Role must be 'Admin' or 'User'.");
                return;
            }

            if (role.equalsIgnoreCase("Admin")) {
                String checkAdminSql = "SELECT COUNT(*) FROM Users WHERE Role = 'Admin'";
                try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(checkAdminSql)) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("An Admin already exists. Cannot add another Admin.");
                        return;
                    }
                }
            }

            // Check if email already exists regardless of role
            String checkEmailSql = "SELECT COUNT(*) FROM Users WHERE Email = ?";
            try (PreparedStatement pstmt = con.prepareStatement(checkEmailSql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("User with this email already exists.");
                        return;
                    }
                }
            }

            String sql = "INSERT INTO Users (Name, Email, Role) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, role);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println(role + " added successfully!");
                } else {
                    System.out.println("Failed to add " + role + ".");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
