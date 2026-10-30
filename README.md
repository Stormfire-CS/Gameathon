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

## Check your installed versions

Open a terminal (Command Prompt on Windows, or Terminal on mac/Linux) and run the following commands;

Check Java:
```
java -version
```
This will display the installed Java version. Make sure it is Java 25.

Check Git:
```
git --version
```
This will display the installed Git version, ensure it is 2.55.0

Check MySQL
```
SELECT VERSION();
```

---

## Mac / Linux / Windows

## 1. Clone the repo
```
git clone https://github.com/Stormfire-CS/Gameathon.git
cd Gameathon
```
## 2. Create the MySQL database
Gameathon requires a lcoal MySQL database.

### Start MySQL

Make sure your local MySQL server is running.

Connect using MySQL Shell:
```
mysqlsh
```
At the MySQL Shell prompt, switch to SQL mode:
```
\sql
```
Connect to your local MySQL server. Replace root with your MySQL username if necessary:
```
\connect root@localhost
```
Enter your MySQL password when prompted.

### Create the database

```
CREATE DATABASE game_tracker;
```

### Configure your database credentials

Open:
```
src/main/resources/application.properties
```
Find the database configuration. It should contain settings for the database URL, username, and password.

For example, a local MySQL conf:
```
spring.datasource.url=jdbc:mysql://localhost/game_tracker
spring.datasource.username = gt_app
spring.datasource.password = Password01
```
Replace the username and password with credentials for your local MySQL account.

## 3. Run Gameathon

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