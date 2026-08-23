import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Book {
    int bookID;
    String title;
    String author;
    String publisher;
    int year;
    String isbn;
    double price; // Added price
    int availableCopies; // Added available copies

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    Book(int bookID, String title, String author, String publisher, int year, String isbn) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.isbn = isbn;
    }

    // Constructor with all fields
    Book(int bookID, String title, String author, String publisher, int year, String isbn, double price, int availableCopies) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.isbn = isbn;
        this.price = price;
        this.availableCopies = availableCopies;
    }

    // Static methods for book management
    public static void addBook(Scanner sc, Connection con) {
        try {
            System.out.print("Enter Book Title: ");
            String title = sc.nextLine();
            if (title.trim().isEmpty()) {
                System.out.println("Book title cannot be empty.");
                return;
            }

            System.out.print("Enter Book Author: ");
            String author = sc.nextLine();

            System.out.print("Enter Book Publisher: ");
            String publisher = sc.nextLine();

            int year = 0;
            System.out.print("Enter Book Year: ");
            try {
                year = sc.nextInt();
                if (year < 1000 || year > 2100) { // Basic year validation
                    System.out.println("Invalid year. Please enter a realistic year.");
                    sc.nextLine(); // Consume invalid input
                    return;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input for year. Please enter a number.");
                sc.nextLine(); // Consume invalid input
                return;
            }
            sc.nextLine(); // Consume newline

            System.out.print("Enter Book ISBN: ");
            String isbn = sc.nextLine();
            if (isbn.trim().isEmpty()) {
                System.out.println("Book ISBN cannot be empty.");
                return;
            }
            // Basic ISBN format check (can be more complex)
            if (!isbn.matches("^[0-9X]{10}$|^[0-9]{13}$")) {
                System.out.println("Invalid ISBN format. Must be 10 or 13 digits (with X for 10-digit).");
                return;
            }

            // Check for duplicate ISBN
            if (isIsbnDuplicate(isbn, con)) {
                System.out.println("A book with this ISBN already exists.");
                return;
            }

            double price = 0.0;
            System.out.print("Enter Book Price: ");
            try {
                price = sc.nextDouble();
                if (price < 0) {
                    System.out.println("Price cannot be negative.");
                    sc.nextLine();
                    return;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input for price. Please enter a number.");
                sc.nextLine();
                return;
            }
            sc.nextLine(); // Consume newline

            int availableCopies = 0;
            System.out.print("Enter Number of Available Copies: ");
            try {
                availableCopies = sc.nextInt();
                if (availableCopies < 0) {
                    System.out.println("Available copies cannot be negative.");
                    sc.nextLine();
                    return;
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input for available copies. Please enter a number.");
                sc.nextLine();
                return;
            }
            sc.nextLine(); // Consume newline


            String sql = "INSERT INTO Books (Title, Author, Publisher, Year, ISBN, Price, AvailableCopies) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setString(1, title);
                pstmt.setString(2, author);
                pstmt.setString(3, publisher);
                pstmt.setInt(4, year);
                pstmt.setString(5, isbn);
                pstmt.setDouble(6, price);
                pstmt.setInt(7, availableCopies);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book added successfully!");
                } else {
                    System.out.println("Failed to add book.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding book: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public static void viewBooks(Connection con) {
        String sql = "SELECT BookID, Title, Author, Publisher, Year, ISBN, Price, AvailableCopies FROM Books";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (!rs.isBeforeFirst()) { // Check if ResultSet is empty
                System.out.println("No books available in the library.");
                return;
            }
            System.out.println("\n--- Available Books ---");
            while (rs.next()) {
                System.out.println("Book ID: " + rs.getInt("BookID"));
                System.out.println("Title: " + rs.getString("Title"));
                System.out.println("Author: " + rs.getString("Author"));
                System.out.println("Publisher: " + rs.getString("Publisher"));
                System.out.println("Year: " + rs.getInt("Year"));
                System.out.println("ISBN: " + rs.getString("ISBN"));
                System.out.println("Price: $" + String.format("%.2f", rs.getDouble("Price")));
                System.out.println("Available Copies: " + rs.getInt("AvailableCopies"));
                System.out.println("-----------------------");
            }
        } catch (SQLException e) {
            System.out.println("Error viewing books: " + e.getMessage());
        }
    }

    public static void deleteBook(Scanner sc, Connection con) {
        try {
            System.out.print("Enter Book ID to delete: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number for Book ID.");
                sc.nextLine(); // Consume invalid input
                return;
            }
            int bookId = sc.nextInt();
            sc.nextLine(); // Consume newline

            // Check if the book exists
            String checkSql = "SELECT COUNT(*) FROM Books WHERE BookID = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkSql)) {
                checkStmt.setInt(1, bookId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Book not found with ID: " + bookId);
                        return;
                    }
                }
            }

            // Delete the book
            String sql = "DELETE FROM Books WHERE BookID = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, bookId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book deleted successfully!");
                } else {
                    System.out.println("Book could not be deleted. No rows affected.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public static String getBookTitle(int bookId, Connection con) {
        String sql = "SELECT Title FROM Books WHERE BookID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, bookId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Title");
                } else {
                    // System.out.println("Book not found with ID: " + bookId); // Removed to avoid redundant message
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting book title: " + e.getMessage());
            return null;
        }
    }

    public static double getBookPrice(int bookId, Connection con) {
        String sql = "SELECT Price FROM Books WHERE BookID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, bookId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("Price");
                } else {
                    return -1.0; // Indicate book not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting book price: " + e.getMessage());
            return -1.0;
        }
    }

    public static int getAvailableCopies(int bookId, Connection con) {
        String sql = "SELECT AvailableCopies FROM Books WHERE BookID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, bookId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("AvailableCopies");
                } else {
                    return -1; // Indicate book not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting available copies: " + e.getMessage());
            return -1;
        }
    }

    public static boolean updateBookCopies(int bookId, int change, String type, Connection con) {
        String sql = "";
        if (type.equalsIgnoreCase("rent")) {
            sql = "UPDATE Books SET AvailableCopies = AvailableCopies - ?, TotalRented = TotalRented + ? WHERE BookID = ?";
        } else if (type.equalsIgnoreCase("return")) {
            sql = "UPDATE Books SET AvailableCopies = AvailableCopies + ?, TotalRented = TotalRented - ? WHERE BookID = ?";
        } else if (type.equalsIgnoreCase("purchase")) {
            sql = "UPDATE Books SET AvailableCopies = AvailableCopies - ?, TotalSold = TotalSold + ? WHERE BookID = ?";
        } else {
            System.err.println("Invalid update type for book copies.");
            return false;
        }

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, change);
            pstmt.setInt(2, change);
            pstmt.setInt(3, bookId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book copies: " + e.getMessage());
            return false;
        }
    }

    private static boolean isIsbnDuplicate(String isbn, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Books WHERE ISBN = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
