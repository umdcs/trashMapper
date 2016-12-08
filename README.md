This is a git repostory for the UMD CS4531 EPA Trashmapping application.  
This app has been developed in the andorid platform.

#Fuctionality of the application:

1. This application enables users to signup and login (username and password).
2. This app will allow users upload the pictures of trash/trash bin they see (either take a picture or pick a picture in the gallery), and add descriptions (e.g. type of trash) to it. 
3. After adding picture and description of the trash, click on the "Go To Map" button which will take the users to the Google Map View. 
4. On the map, user's current location will be shown on the map. The related marker associated with the current user name and that piece of trash will appear on a map as well for others to view. 
5. Once the marker gets clicked, it will lead the user to a new display activity where the picture of trash/trash bin, along with the detailed description of the trash/trash bin will be shown. 
6. In the display activity, there are two buttons: "Like" and "Dislike" at the bottom. When viewing the trash other peple uploaded, the user can choose to like or dislike that piece of trash. If one peice of trash has 4 more dislikes than likes, that piece of trash will be removed from the map.

#Branch and version info:
The working branch is the current default branch "oldCompleteMap"
The Android Studio version last used is 2.2.2

#To run the application:

Part 1. Node Server
  To run the node server, type in the following commands in the Node directory of the project:
  1. start mongodb: mongod
  2. install dependencies: npm install
  3. run node server: node index.js
        
Part 2. Android

  1. Install android studio
  2. Communication with server:

2.1 If you want to test this app on your localhost, you should go to the HttpAsyncTask class and change the address field at the bottom to your local IP address or server address. ie: "http://192.168.1.1:4321". And make sure port number is 4321 in the index.js file under the Node directory.
        
2.2 To reconnect to the lempo server (the port is currently unaccessible), change the address field in the HttpAsyncTask back to "https://lempo.d.umn.edu:8193". Change the port number in the index.js to 8192 as well. 

##This project was created by: Matt Jallen, Yichen Wei, Jake Pulkkinen, and Kun Li.
