-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 21, 2025 at 12:59 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `librarysystem`
--

-- --------------------------------------------------------

--
-- Table structure for table `billing`
--

CREATE TABLE `billing` (
  `BillID` int(11) NOT NULL,
  `UserID` int(11) NOT NULL,
  `BookID` int(11) NOT NULL,
  `Type` enum('Purchase','Rent','Return') NOT NULL,
  `Amount` decimal(10,2) NOT NULL,
  `BillDate` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `billing`
--

INSERT INTO `billing` (`BillID`, `UserID`, `BookID`, `Type`, `Amount`, `BillDate`) VALUES
(1, 1, 1, 'Rent', 10.00, '2025-08-21 10:30:55'),
(2, 1, 1, 'Rent', 10.00, '2025-08-21 10:39:43'),
(3, 1, 1, 'Rent', 10.00, '2025-08-21 10:45:27'),
(4, 1, 1, 'Return', 0.00, '2025-08-21 10:48:14'),
(5, 2, 1, 'Purchase', 100.00, '2025-08-21 10:51:32');

-- --------------------------------------------------------

--
-- Table structure for table `books`
--

CREATE TABLE `books` (
  `BookID` int(11) NOT NULL,
  `Title` varchar(200) DEFAULT NULL,
  `Author` varchar(100) DEFAULT NULL,
  `Publisher` varchar(100) DEFAULT NULL,
  `Year` int(11) DEFAULT NULL,
  `ISBN` varchar(50) DEFAULT NULL,
  `Price` decimal(10,2) DEFAULT NULL,
  `TotalSold` int(11) DEFAULT 0,
  `TotalRented` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `books`
--

INSERT INTO `books` (`BookID`, `Title`, `Author`, `Publisher`, `Year`, `ISBN`, `Price`, `TotalSold`, `TotalRented`) VALUES
(1, 'java', 'dd', 'dedded', 2024, '1234567891011', NULL, 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `purchase`
--

CREATE TABLE `purchase` (
  `PurchaseID` int(11) NOT NULL,
  `BookID` int(11) NOT NULL,
  `UserID` int(11) NOT NULL,
  `Quantity` int(11) NOT NULL,
  `TotalAmount` decimal(10,2) NOT NULL,
  `PurchaseDate` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `purchase`
--

INSERT INTO `purchase` (`PurchaseID`, `BookID`, `UserID`, `Quantity`, `TotalAmount`, `PurchaseDate`) VALUES
(1, 1, 2, 1, 100.00, '2025-08-21 10:51:32');

-- --------------------------------------------------------

--
-- Table structure for table `rents`
--

CREATE TABLE `rents` (
  `RentID` int(11) NOT NULL,
  `BookID` int(11) DEFAULT NULL,
  `UserID` int(11) DEFAULT NULL,
  `RentDate` date NOT NULL,
  `ReturnDate` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `rents`
--

INSERT INTO `rents` (`RentID`, `BookID`, `UserID`, `RentDate`, `ReturnDate`) VALUES
(1, 1, 1, '2025-08-21', '2025-08-21'),
(5, 1, 1, '2025-08-21', '2025-08-21');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `UserID` int(11) NOT NULL,
  `Name` varchar(100) NOT NULL,
  `Email` varchar(100) NOT NULL,
  `Role` enum('Admin','User') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`UserID`, `Name`, `Email`, `Role`) VALUES
(1, 'raj', 'raj123', 'Admin'),
(2, 'kavy', 'kavy123', 'User');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `billing`
--
ALTER TABLE `billing`
  ADD PRIMARY KEY (`BillID`),
  ADD KEY `UserID` (`UserID`),
  ADD KEY `BookID` (`BookID`);

--
-- Indexes for table `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`BookID`);

--
-- Indexes for table `purchase`
--
ALTER TABLE `purchase`
  ADD PRIMARY KEY (`PurchaseID`),
  ADD KEY `BookID` (`BookID`),
  ADD KEY `UserID` (`UserID`);

--
-- Indexes for table `rents`
--
ALTER TABLE `rents`
  ADD PRIMARY KEY (`RentID`),
  ADD KEY `BookID` (`BookID`),
  ADD KEY `UserID` (`UserID`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`UserID`),
  ADD UNIQUE KEY `Email` (`Email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `billing`
--
ALTER TABLE `billing`
  MODIFY `BillID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `books`
--
ALTER TABLE `books`
  MODIFY `BookID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `purchase`
--
ALTER TABLE `purchase`
  MODIFY `PurchaseID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `rents`
--
ALTER TABLE `rents`
  MODIFY `RentID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `UserID` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `billing`
--
ALTER TABLE `billing`
  ADD CONSTRAINT `billing_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE,
  ADD CONSTRAINT `billing_ibfk_2` FOREIGN KEY (`BookID`) REFERENCES `books` (`BookID`) ON DELETE CASCADE;

--
-- Constraints for table `purchase`
--
ALTER TABLE `purchase`
  ADD CONSTRAINT `purchase_ibfk_1` FOREIGN KEY (`BookID`) REFERENCES `books` (`BookID`) ON DELETE CASCADE,
  ADD CONSTRAINT `purchase_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE;

--
-- Constraints for table `rents`
--
ALTER TABLE `rents`
  ADD CONSTRAINT `rents_ibfk_1` FOREIGN KEY (`BookID`) REFERENCES `books` (`BookID`) ON DELETE CASCADE,
  ADD CONSTRAINT `rents_ibfk_2` FOREIGN KEY (`UserID`) REFERENCES `users` (`UserID`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
