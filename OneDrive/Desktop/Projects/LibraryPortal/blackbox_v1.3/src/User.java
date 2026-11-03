import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class User {
    int userID;
    String name;
    String email;
    // ArrayList to store read book history in memory
    List<String> readedBookHistory = new ArrayList<>();
    List<Integer> readedBookHistoryId = new ArrayList<>();

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
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

    User(int userID, String name, String email) {
        this.userID = userID;
        this.name = name;
        this.email = email;
    }

    public void userLogin(Scanner sc, Connection con) {
        try {
            System.out.print("Enter User Email: ");
            String email = sc.nextLine();

            if (email.trim().isEmpty() || !email.contains("@")) {
                System.out.println("Invalid email format.");
                return;
            }

            String sql = "SELECT UserID, Name, Email FROM Users WHERE Email = ? AND Role = 'User'";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        this.userID = rs.getInt("UserID");
                        this.name = rs.getString("Name");
                        this.email = rs.getString("Email");
                        System.out.println("User login successful!");
                        userMenu(sc, con);
                    } else {
                        System.out.println("Invalid User credentials or role.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during user login: " + e.getMessage());
        }
    }

    private void userMenu(Scanner sc, Connection con) throws SQLException {
        while (true) {
            System.out.println("\nUser Menu:");
            System.out.println("1. View Available Books");
            System.out.println("2. Read Book");
            System.out.println("3. Rent Book");
            System.out.println("4. Return Book");
            System.out.println("5. Purchase Book");
            System.out.println("6. View Read History");
            System.out.println("7. Logout");
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
                    Book.viewBooks(con);
                    break;
                case 2:
                    Book.viewBooks(con);
                    readBook(sc, con);
                    break;
                case 3:
                    Book.viewBooks(con);
                    rentBook(sc, con);
                    break;
                case 4:
                    viewRentedBooks(con);
                    returnBook(sc, con);
                    break;
                case 5:
                    purchaseBook(sc, con);
                    break;
                case 6:
                    viewReadHistory(con); // Changed method name
                    break;
                case 7:
                    System.out.println("Logging out User...");
                    return; // Logout and return to main menu
                default:
                    System.out.println("Invalid choice! Please enter a number between 1 and 7.");
            }
        }
    }

    void readBook(Scanner sc, Connection con) throws SQLException {
        System.out.print("Enter Book ID to mark as read: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number for Book ID.");
            sc.nextLine();
            return;
        }
        int readBookId = sc.nextInt();
        sc.nextLine(); // Consume newline

        if (!isValidBook(readBookId, con)) {
            System.out.println("Book not found with ID: " + readBookId);
            return;
        }

        String bookTitle = Book.getBookTitle(readBookId, con);
        if (bookTitle != null) {
            String sql = "INSERT INTO ReadHistory (UserID, BookID) VALUES (?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, this.userID);
                pstmt.setInt(2, readBookId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book '" + bookTitle + "' added to your reading history!");
                } else {
                    System.out.println("Failed to add book to reading history.");
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                    System.out.println("You have already marked this book as read recently.");
                } else {
                    System.err.println("Error adding book to reading history: " + e.getMessage());
                }
            }
        }
    }

    void rentBook(Scanner sc, Connection con) {
        try {
            System.out.print("Enter Book ID to rent: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number for Book ID.");
                sc.nextLine();
                return;
            }
            int rentBookId = sc.nextInt();
            sc.nextLine(); // Consume newline

            // Validate Book ID
            if (!isValidBook(rentBookId, con)) {
                System.out.println("Book not found with ID: " + rentBookId);
                return;
            }

            // Check available copies
            int availableCopies = Book.getAvailableCopies(rentBookId, con);
            if (availableCopies <= 0) {
                System.out.println("No copies of this book are currently available for rent.");
                return;
            }

            // Check if user already has this book rented and not returned
            if (isBookCurrentlyRentedByUser(rentBookId, this.userID, con)) {
                System.out.println("You already have an unreturned copy of this book rented.");
                return;
            }

            LocalDate date = LocalDate.now();
            java.sql.Date rentDate = java.sql.Date.valueOf(date);

            con.setAutoCommit(false); // Start transaction

            String rentSql = "INSERT INTO Rents(BookID, UserID, RentDate, ReturnDate) VALUES(?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(rentSql)) {
                pstmt.setInt(1, rentBookId);
                pstmt.setInt(2, this.userID);
                pstmt.setDate(3, rentDate);
                pstmt.setDate(4, null); // ReturnDate is null initially
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Update book copies and total rented
                    if (Book.updateBookCopies(rentBookId, 1, "rent", con)) {
                        // Insert billing record for rent (fixed amount for rent)
                        Billing.insertBilling(this.userID, rentBookId, "Rent", 10.0,con);
                        con.commit(); // Commit transaction
                        System.out.println("Book rented successfully!");
                    } else {
                        con.rollback(); // Rollback if book copies update fails
                        System.out.println("Failed to update book copies. Rent cancelled.");
                    }
                } else {
                    con.rollback(); // Rollback if rent insertion fails
                    System.out.println("Failed to rent book.");
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine(); // Consume invalid input
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } catch (SQLException e) {
            System.err.println("Error renting book: " + e.getMessage());
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { System.err.println("Error resetting auto-commit: " + e.getMessage()); }
        }
    }

    void returnBook(Scanner sc, Connection con) {
        try {
            System.out.print("Enter Book ID to return: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number for Book ID.");
                sc.nextLine();
                return;
            }
            int returnBookId = sc.nextInt();
            sc.nextLine(); // Consume newline

            // Validate Book ID
            if (!isValidBook(returnBookId, con)) {
                System.out.println("Book not found with ID: " + returnBookId);
                return;
            }

            // Check if the user actually rented this book and it's not yet returned
            String checkRentSql = "SELECT RentID FROM Rents WHERE BookID = ? AND UserID = ? AND ReturnDate IS NULL";
            int rentId = -1;
            try (PreparedStatement pstmt = con.prepareStatement(checkRentSql)) {
                pstmt.setInt(1, returnBookId);
                pstmt.setInt(2, this.userID);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        rentId = rs.getInt("RentID");
                    } else {
                        System.out.println("You do not have an unreturned copy of this book rented.");
                        return;
                    }
                }
            }

            LocalDate rDate = LocalDate.now();
            java.sql.Date returnDate = java.sql.Date.valueOf(rDate);

            con.setAutoCommit(false); // Start transaction

            String updateRentSql = "UPDATE Rents SET ReturnDate = ? WHERE RentID = ?";

            try (PreparedStatement pstmt = con.prepareStatement(updateRentSql)) {
                pstmt.setDate(1, returnDate);
                pstmt.setInt(2, rentId);
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Update book copies and total rented
                    if (Book.updateBookCopies(returnBookId, 1, "return", con)) {
                        // Insert billing record for return
                        Billing.insertBilling(this.userID, returnBookId, "Return", 0.0,con);
                        con.commit(); // Commit transaction
                        System.out.println("Book returned successfully!");
                    } else {
                        con.rollback(); // Rollback if book copies update fails
                        System.out.println("Failed to update book copies. Return cancelled.");
                    }
                } else {
                    con.rollback(); // Rollback if rent update fails
                    System.out.println("Failed to return book. No rent record updated.");
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine(); // Consume invalid input
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } catch (SQLException e) {
            System.err.println("Error returning book: " + e.getMessage());
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { System.err.println("Error resetting auto-commit: " + e.getMessage()); }
        }
    }

    void purchaseBook(Scanner sc, Connection con) {
        try {
            System.out.print("Enter Book ID to purchase: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number for Book ID.");
                sc.nextLine();
                return;
            }
            int bookId = sc.nextInt();
            sc.nextLine(); // Consume newline

            // Validate Book ID
            if (!isValidBook(bookId, con)) {
                System.out.println("Book not found with ID: " + bookId);
                return;
            }

            System.out.print("Enter quantity: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number for quantity.");
                sc.nextLine();
                return;
            }
            int quantity = sc.nextInt();
            sc.nextLine(); // Consume newline

            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                return;
            }

            // Check available copies
            int availableCopies = Book.getAvailableCopies(bookId, con);
            if (availableCopies == -1) { // Book not found (already checked by isValidBook)
                System.out.println("Error retrieving book copies.");
                return;
            }
            if (availableCopies < quantity) {
                System.out.println("Not enough copies available. Only " + availableCopies + " left.");
                return;
            }

            // Get book price and calculate total amount
            double bookPrice = Book.getBookPrice(bookId, con);
            if (bookPrice == -1.0) { // Book not found (already checked by isValidBook)
                System.out.println("Error retrieving book price.");
                return;
            }
            double totalAmount = bookPrice * quantity;

            con.setAutoCommit(false); // Start transaction

            // Insert purchase record
            Purchase.insertPurchase(bookId, this.userID, quantity, totalAmount, con); // Pass connection

            // Update book copies and total sold
            if (Book.updateBookCopies(bookId, quantity, "purchase", con)) {
                // Insert billing record for purchase
                Billing.insertBilling(this.userID, bookId, "Purchase", totalAmount, con); // Pass connection
                con.commit(); // Commit transaction
                System.out.println("Book(s) purchased successfully! Total amount: $" + String.format("%.2f", totalAmount));
            } else {
                con.rollback(); // Rollback if book copies update fails
                System.out.println("Failed to update book copies. Purchase cancelled.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine(); // Consume invalid input
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } catch (SQLException e) {
            System.err.println("Error purchasing book: " + e.getMessage());
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during purchase: " + e.getMessage());
            try { con.rollback(); } catch (SQLException rb) { System.err.println("Rollback failed: " + rb.getMessage()); }
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { System.err.println("Error resetting auto-commit: " + e.getMessage()); }
        }
    }

    void viewReadHistory(Connection con) {
        // Clear existing history before populating
        readedBookHistory.clear();
        readedBookHistoryId.clear();
        
        System.out.println("\n--- Your Read History ---");
        String sql = "SELECT b.Title, b.BookID, rh.ReadDate FROM ReadHistory rh JOIN Books b ON rh.BookID = b.BookID WHERE rh.UserID = ? ORDER BY rh.ReadDate DESC";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, this.userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("You haven't marked any books as read yet.");
                    return;
                }
                while (rs.next()) {
                    String title = rs.getString("Title");
                    int bookId = rs.getInt("BookID");
                    
                    // Populate the ArrayLists with read book data
                    readedBookHistory.add(title);
                    readedBookHistoryId.add(bookId);
                    
                    System.out.println("Title: " + title + " (Read on: " + rs.getTimestamp("ReadDate") + ")");
                }
                System.out.println("Total books read: " + readedBookHistory.size());
                
                // Save read history to file
                saveReadHistoryToFile();
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving read history: " + e.getMessage());
        }
        System.out.println("-------------------------");
    }

    // Method to save read history to history.txt file
    public void saveReadHistoryToFile() {
        String filename = "history.txt";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("=== Read Book History ===\n");
            writer.write("User: " + this.name + " (ID: " + this.userID + ")\n");
            writer.write("Total Books Read: " + readedBookHistory.size() + "\n\n");
            
            if (readedBookHistory.isEmpty()) {
                writer.write("No books have been read yet.\n");
            } else {
                for (int i = 0; i < readedBookHistory.size(); i++) {
                    writer.write((i + 1) + ". " + readedBookHistory.get(i) + 
                                 " (Book ID: " + readedBookHistoryId.get(i) + ")\n");
                }
            }
            writer.write("\n=== End of History ===\n");
            System.out.println("Read history saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving read history to file: " + e.getMessage());
        }
    }

    // Method to read history from history.txt file
    public void readReadHistoryFromFile() {
        String filename = "history.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            System.out.println("\n--- Reading History from File ---");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("--- End of File ---");
        } catch (IOException e) {
            System.err.println("Error reading read history from file: " + e.getMessage());
        }
    }

    // Helper method to check if a user ID is valid
    private boolean isValidUser(int userId, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE UserID = ? AND Role = 'User'";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    void viewRentedBooks(Connection con) {
        System.out.println("\n--- Your Currently Rented Books ---");
        String sql = "SELECT b.Title, b.Author, r.RentDate FROM Rents r JOIN Books b ON r.BookID = b.BookID WHERE r.UserID = ? AND r.ReturnDate IS NULL ORDER BY r.RentDate DESC";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, this.userID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("You currently have no books rented.");
                    return;
                }
                while (rs.next()) {
                    System.out.println("Title: " + rs.getString("Title"));
                    System.out.println("Author: " + rs.getString("Author"));
                    System.out.println("Rented On: " + rs.getDate("RentDate"));
                    System.out.println("-------------------------");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rented books: " + e.getMessage());
        }
        System.out.println("-----------------------------------");
    }

    // Helper method to check if a book ID is valid
    private boolean isValidBook(int bookId, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Books WHERE BookID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Helper method to check if a user currently has an unreturned copy of a book
    private boolean isBookCurrentlyRentedByUser(int bookId, int userId, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Rents WHERE BookID = ? AND UserID = ? AND ReturnDate IS NULL";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
