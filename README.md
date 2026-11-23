# Gameathon

Gameathon is a web application for managing a personal collection of board games, built using Java and the Spring Boot 
framework. It allows users to keep track of the games they own and create or join families so they can easily see which 
games their family members already have.
---

# How to Run Gameathon

## Requirements
Install the following software before running Gameathon:
- Java JDK 25 -- used to compile and run the program
- Git 2.55.0 -- used to download the source code
- MySQL 8.0.46 -- used to store program data

These are the versions used to develop and test Gameathon. Other versions may work, but have not been tested.
The versions listed above are the recommended versions.

## Check your installed versions

Open a terminal (Command Prompt on Windows, or Terminal on mac/Linux) and run the following commands;

### Check Java:
```
java -version
```
This will display the installed Java version. The recommended version is Java 25.

### Check Git:
```
git --version
```
This will display the installed Git version. The recommended version is 2.55.0

### Check MySQL:

To check the installed MySQL client version, run:
```
mysql --version
```
To check the MySQL server version, first connect to your MySQL server:
```
mysql -u root -p
```
Enter your MySQL password when prompted. Once connected, run:
```
SELECT VERSION();
```
This displays the version of the MySQL server you are connected to.

---
# Setting Up SQL DataBase

## 0. Install SQL if not installed

Download and install MySQL from the official website and follow the setup wizard: https://dev.mysql.com/downloads/mysql/

- Make sure to choose a version appropiate for your system.
- Make sure to remember the root password you enter during setup.

## 1. Clone the repo
Open a terminal and run the following commands.
```
git clone https://github.com/Stormfire-CS/Gameathon.git
cd Gameathon
```
## 2. Create the MySQL database
Gameathon requires a lcoal MySQL database.

## 3. Create the MySQL Database

Make sure your local MySQL server is running.

Connect to MySQL:
Open a terminal and connect to your local MySQL server:
```
mysql -u root -p
```
Enter the root password you created during the MySQL installation process.

If the mysql command is not recoqnized, you may need to try opening the MySQL command line installed with MySQL.

### Create the database
Once connected to MySQL then run:
```
CREATE DATABASE game_tracker;
```
(game_tracker is the default database name, however, if desired, the database can be named something else. Be sure to update the application/properties file as explained later in step 4)
### Create a Database User
Gameathon needs a MySQL account with permission to access the game_tracker database.
- The default account is named gt_app (gt_app can also be renamed and updated in the application/properties file in step 4)
- And the default password is Max12 (Max12 can also be renamed and updated in the application/properties file in step 4)
```
CREATE USER 'gt_app'@'localhost' IDENTIFIED BY 'Max12';
```
Then grant the account permission to use the Gameathon database:
```
GRANT ALL PRIVILEGES ON game_tracker.* TO 'gt_app'@'localhost';
```
## 4. Configure Your Database Credentials
Open the following file in the project:
```
src/main/resources/application.properties
```
Find the database configuration. It should contain settings for the database URL, username, and password.

For example, the default configureation looks like this:
```
spring.application.name=gameathon
spring.datasource.url=jdbc:mysql://localhost/game_tracker
spring.datasource.username = gt_app
spring.datasource.password = Max12
spring.jpa.hibernate.ddl-auto=update
```
If you did not use the default database name, username, or password, then now update the fields to match the values you entered.
## 5. Run Gameathon

### Windows
From the project directory, run:
```
.\gradlew.bat bootRun
```
### mac / Linux
From the project directory, run:
```
./gradlew bootRun
```
The first run may take several minutes. When Spring Boot starts successfully, look for a message indicating that the application
has started.

### Open Gameathon

Once the application has started, open your web browser and enter

http://localhost:8080 

into the url.

# Getting started with Gameathon

## 1. Create an account
Open Gameathon in your browser.
If you do not already have an account, navigate to the sign-up page and enter the required information.

## 2. Log in
If you just created an account you area automatically logged in.
If you did not just create an account, navigate to the login page and enter your credentials.

## 3. Add games to your collection
Navigate to your game collection by clicking on View My Stuff, and then Edit My Collection.
From there follow the instructions to add a game to your collection.

## 4. Create a family
If you want to share the information about your collection with other users, use the family feature
to create a family. To do so, navigate to the View My Family Page and then select Create a Family.
From there follow the instructions to create your own family.

## 5. Join a family
You can request to join an existing family if you know which family you want to join.
1. First get the family ID from the family administer or from another user in the family
2. Next navigate toe the View My Family page and then select Join a Family
3. Enter the family ID in the available slot, and then click request to join
4. Once you have requested to join the family admin will then be able to approve your request and 
allow you to join the family, or reject your request.

## 6. View your family's games
Once you belong to a family, go to View My Family and you can view what games your family owns, and 
which family member owns them.