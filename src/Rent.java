import java.sql.Date;

public class Rent {
    int rentID;
    int bookID;
    int userID;
    Date rentDate;
    Date returnDate;

    public int getRentID() {
        return rentID;
    }

    public void setRentID(int rentID) {
        this.rentID = rentID;
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

    public Date getRentDate() {
        return rentDate;
    }

    public void setRentDate(Date rentDate) {
        this.rentDate = rentDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    Rent(int rentID, int bookID, int userID, Date rentDate) {
        this.rentID = rentID;
        this.bookID = bookID;
        this.userID = userID;
        this.rentDate = rentDate;
        this.returnDate = null;
    }
}
