import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Purchase {
    int purchaseID;
    int bookID;
    int userID;
    int quantity;
    double totalAmount;
    Timestamp purchaseDate;

    public int getPurchaseID() {
        return purchaseID;
    }

    public void setPurchaseID(int purchaseID) {
        this.purchaseID = purchaseID;
    }

    public int getBookID() {
        return bookID;
    }

    public void setBookID(int bookID) {
        this.bookID = bookID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Timestamp purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    Purchase(int purchaseID, int bookID, int userID, int quantity, double totalAmount, Timestamp purchaseDate) {
        this.purchaseID = purchaseID;
        this.bookID = bookID;
        this.userID = userID;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.purchaseDate = purchaseDate;
    }

    // Modified to accept Connection for transaction management
    public static void insertPurchase(int bookID, int userID, int quantity, double totalAmount, Connection con) throws SQLException {
        String sql = "INSERT INTO Purchase (BookID, UserID, Quantity, TotalAmount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, bookID);
            pstmt.setInt(2, userID);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, totalAmount);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert purchase record.");
            }
            // System.out.println("Purchase record inserted successfully! Rows affected: " + rowsAffected); // Removed for cleaner output
        }
    }
}
