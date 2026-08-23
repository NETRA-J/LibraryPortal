import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Billing {
    int billID;
    int userID;
    int bookID;
    String type;
    double amount;
    Timestamp billDate;

    public int getBillID() {
        return billID;
    }

    public void setBillID(int billID) {
        this.billID = billID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getBillDate() {
        return billDate;
    }

    public void setBillDate(Timestamp billDate) {
        this.billDate = billDate;
    }

    Billing(int billID, int userID, int bookID, String type, double amount, Timestamp billDate) {
        this.billID = billID;
        this.userID = userID;
        this.bookID = bookID;
        this.type = type;
        this.amount = amount;
        this.billDate = billDate;
    }

    // Modified to accept Connection for transaction management
    public static void insertBilling(int userID, int bookID, String type, double amount, Connection con) throws SQLException {
        String sql = "INSERT INTO Billing (UserID, BookID, Type, Amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, bookID);
            pstmt.setString(3, type);
            pstmt.setDouble(4, amount);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert billing record.");
            }
            // System.out.println("Billing record inserted successfully! Rows affected: " + rowsAffected); // Removed for cleaner output
        }
    }
}
