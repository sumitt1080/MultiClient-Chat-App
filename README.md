# Chat Application

[![Java](https://img.shields.io/badge/Java-orange?style=flat&logo=java&logoColor=white&link=https://github.com/sumitt1080)](https://github.com/sumitt1080)

A real-time GUI based chat application is implemented using **Java** and **multi-threading** where communication takes place over **socket** connection and data flows bi-directionally between Server and multiple Clients, also configured persistent queue to prevent data loss and provide offline message storage capability.

## Instructions on compiling and running the project:

IDE Used: **VS Code** 

Language: **Java 8**

- Clone the repo and use any of your IDE.
- Navigate src/main/ServerMain.java. Run it. If prompted for Firewall access, please click 'Allow access', else continue.
- When Server GUI get opened, Click 'Start Server' to start the server on local machine.
- Now navigate src/main/ClientMain.java. Run it. You can run it multiple time to open more than 1 client GUI.
- When Client GUI get opened. Click 'Connect' and then enter a username and press 'Login'. You will get a success dialog box.
- In Client GUI, at below right, you will get to see all registered user and an additional broadcast option. 
- Select a username from above list to chat personally. Select 'broadcast' to send your message to all logged in clients.

## Screenshots
![serverMain](https://user-images.githubusercontent.com/51052011/105521102-7be6b600-5d01-11eb-8b98-618b6313daa0.jpg) &nbsp; ![Client1](https://user-images.githubusercontent.com/51052011/105521206-97ea5780-5d01-11eb-8204-10e4e5347889.jpg)
 
 ![ServerOn](https://user-images.githubusercontent.com/51052011/105521257-a59fdd00-5d01-11eb-9da8-cfd406936bf9.jpg) &nbsp; ![ClientOn](https://user-images.githubusercontent.com/51052011/105521304-b51f2600-5d01-11eb-80b8-88fab92c1657.jpg) <br>
## Note
I had tested it with 5 simultaneous clients. If u find any misbehaviour on more number of client please let me know.
