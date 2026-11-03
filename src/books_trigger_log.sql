-- Books Trigger and Log Table Creation Script
-- This script creates a log table and trigger for tracking changes to books

-- Step 1: Add AvailableCopies column if it doesn't exist
-- Note: Based on Book.java, this column should exist but might be missing from the dump
ALTER TABLE books ADD COLUMN IF NOT EXISTS AvailableCopies INT DEFAULT 0;

-- Step 2: Create books_log table for tracking changes
CREATE TABLE IF NOT EXISTS books_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    book_title VARCHAR(200),
    action_type VARCHAR(10) NOT NULL, -- 'INSERT' or 'UPDATE'
    old_total_sold INT DEFAULT NULL,
    new_total_sold INT DEFAULT NULL,
    old_total_rented INT DEFAULT NULL,
    new_total_rented INT DEFAULT NULL,
    old_available_copies INT DEFAULT NULL,
    new_available_copies INT DEFAULT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) DEFAULT CURRENT_USER
);

-- Step 3: Create the trigger for AFTER INSERT and UPDATE
DELIMITER $$

CREATE TRIGGER books_audit_trigger
AFTER INSERT ON books
FOR EACH ROW
BEGIN
    -- Log INSERT operation
    INSERT INTO books_log (
        book_id, 
        book_title, 
        action_type,
        new_total_sold,
        new_total_rented,
        new_available_copies
    ) VALUES (
        NEW.BookID,
        NEW.Title,
        'INSERT',
        NEW.TotalSold,
        NEW.TotalRented,
        NEW.AvailableCopies
    );
END$$

CREATE TRIGGER books_audit_trigger_update
AFTER UPDATE ON books
FOR EACH ROW
BEGIN
    -- Only log if relevant fields have changed
    IF (OLD.TotalSold != NEW.TotalSold OR 
        OLD.TotalRented != NEW.TotalRented OR 
        OLD.AvailableCopies != NEW.AvailableCopies) THEN
        
        INSERT INTO books_log (
            book_id, 
            book_title, 
            action_type,
            old_total_sold,
            new_total_sold,
            old_total_rented,
            new_total_rented,
            old_available_copies,
            new_available_copies
        ) VALUES (
            NEW.BookID,
            NEW.Title,
            'UPDATE',
            OLD.TotalSold,
            NEW.TotalSold,
            OLD.TotalRented,
            NEW.TotalRented,
            OLD.AvailableCopies,
            NEW.AvailableCopies
        );
    END IF;
END$$

DELIMITER ;

-- Step 4: Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_books_log_book_id ON books_log(book_id);
CREATE INDEX IF NOT EXISTS idx_books_log_changed_at ON books_log(changed_at);

-- Step 5: Sample query to view logs
-- SELECT * FROM books_log ORDER BY changed_at DESC;

-- Step 6: Test the trigger with sample operations
/*
-- Test INSERT
INSERT INTO books (Title, Author, Publisher, Year, ISBN, Price, TotalSold, TotalRented, AvailableCopies) 
VALUES ('Test Book', 'Test Author', 'Test Publisher', 2024, '1234567890', 29.99, 0, 0, 10);

-- Test UPDATE
UPDATE books SET TotalSold = 5, AvailableCopies = 5 WHERE BookID = 1;

-- View logs
SELECT * FROM books_log WHERE book_id = 1;
*/
