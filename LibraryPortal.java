

import java.sql.*;
import java.util.*;


public class LibraryPortal {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        LibraryPortal portal = new LibraryPortal();
        
        try {
            Connection con = DatabaseConnection.getConnection();
        while (true) {
            System.out.println("1. Sign Up");
            System.out.println("2. Admin Login");
            System.out.println("3. User Login");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    Admin signupAdmin = new Admin(0, "", "");
                    signupAdmin.addUser(sc, con);
                    break;
                case 2:
                    Admin admin = new Admin(0, "", "");
                    admin.adminLogin(sc, con);
                    break;
                case 3:
                    User user = new User(0, "", "");
                    user.userLogin(sc, con);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return; // Exit the while loop and terminate the program
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
