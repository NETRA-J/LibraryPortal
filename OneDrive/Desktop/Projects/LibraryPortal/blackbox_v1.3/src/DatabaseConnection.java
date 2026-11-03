import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Blackbox_v1.1";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Class.forName(DRIVER_NAME); // Not strictly necessary for modern JDBC drivers
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                createTables();
                System.out.println("Database connection established successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
        return connection;
    }

    private static void createTables() {
        try (Statement stmt = connection.createStatement()) {
            String users = """
            CREATE TABLE IF NOT EXISTS Users (
                UserID INT AUTO_INCREMENT PRIMARY KEY,
                Name VARCHAR(100) NOT NULL,
                Email VARCHAR(100) NOT NULL UNIQUE,
                Role ENUM('Admin', 'User') NOT NULL
            );
        """;

            String books = """
            CREATE TABLE IF NOT EXISTS Books (
                BookID INT AUTO_INCREMENT PRIMARY KEY,
                Title VARCHAR(200) NOT NULL,
                Author VARCHAR(100),
                Publisher VARCHAR(100),
                Year INT,
                ISBN VARCHAR(50) UNIQUE, -- Added UNIQUE constraint for ISBN
                Price DECIMAL(10,2) DEFAULT 0.00, -- Added default price
                TotalSold INT DEFAULT 0,
                TotalRented INT DEFAULT 0,
                AvailableCopies INT DEFAULT 1 -- Added for stock management
            );
        """;

            String rents = """
            CREATE TABLE IF NOT EXISTS Rents (
                RentID INT AUTO_INCREMENT PRIMARY KEY,
                BookID INT,
                UserID INT,
                RentDate DATE NOT NULL,
                ReturnDate DATE,
                FOREIGN KEY (BookID) REFERENCES Books(BookID) ON DELETE CASCADE,
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
                UNIQUE (BookID, UserID, ReturnDate) -- Ensures a user can't rent the same book twice without returning it
            );
        """;

            String purchase = """
            CREATE TABLE IF NOT EXISTS Purchase (
                PurchaseID INT PRIMARY KEY AUTO_INCREMENT,
                BookID INT NOT NULL,
                UserID INT NOT NULL,
                Quantity INT NOT NULL,
                TotalAmount DECIMAL(10,2) NOT NULL,
                PurchaseDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (BookID) REFERENCES Books(BookID) ON DELETE CASCADE,
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE
            );
        """;

            String billing = """
            CREATE TABLE IF NOT EXISTS Billing (
                 BillID INT PRIMARY KEY AUTO_INCREMENT,
                 UserID INT NOT NULL,
                 BookID INT NOT NULL,
                 Type ENUM('Purchase','Rent','Return') NOT NULL,
                 Amount DECIMAL(10,2) NOT NULL,
                 BillDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                 FOREIGN KEY (UserID) REFERENCES Users(UserID)  ON DELETE CASCADE,
                 FOREIGN KEY (BookID) REFERENCES Books(BookID)  ON DELETE CASCADE
            );
        """;

            // New table for reading history
            String readHistory = """
            CREATE TABLE IF NOT EXISTS ReadHistory (
                ReadID INT AUTO_INCREMENT PRIMARY KEY,
                UserID INT NOT NULL,
                BookID INT NOT NULL,
                ReadDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE,
                FOREIGN KEY (BookID) REFERENCES Books(BookID) ON DELETE CASCADE,
                UNIQUE (UserID, BookID, ReadDate) -- Prevent duplicate entries for same read event
            );
            """;


            stmt.executeUpdate(users);
            stmt.executeUpdate(books);
            stmt.executeUpdate(rents);
            stmt.executeUpdate(purchase);
            stmt.executeUpdate(billing);
            stmt.executeUpdate(readHistory); // Execute the new table creation

            System.out.println("Tables created successfully!");

        } catch (SQLException e) {
            System.err.println("Table creation error: " + e.getMessage());
        }
    }

}
