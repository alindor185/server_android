Project Overview
This repository contains the Android frontend application and its connection to the backend server for a YouTube-like platform. The backend is developed using Node.js and MongoDB, with the frontend focusing on video playback, user interactions, and search functionality.

Prerequisites
Node.js
MongoDB
MongoDB Setup
Open your MongoDB GUI or terminal.

Create a New Database:

Database Name: youtube
Collection Names: videos, comments, users
Add Collections:

videos, comments, users
Import JSON Data:

videos.json for the videos collection
comments.json for the comments collection
users.json for the users collection
Backend Setup
Clone the repository from the following link: Backend GitHub Link.

Navigate to the server directory:

bash
Copy code
cd server
Start the backend server:

bash
Copy code
node server.js
Frontend Setup
Clone the Android project into Android Studio.

Tap the green run button (>) in Android Studio to launch the app.

Important Notes:
There is a loading wheel that appears when videos are being fetched from the server.
Searching for videos can be done by clicking on the upper left side of the screen.
Reconnecting and Refreshing: Switching to dark mode, connecting as a new user, or disconnecting and reconnecting will load 20 new videos.
Server Features and User Actions
User Actions:
Register
Update Profile
Login
Video Actions:
Upload Video
Delete Video
Edit Video
View Count
Comments:
Add Comments
Edit Comments
Delete Comments
Conclusion
This project provides a YouTube-like platform with video upload, editing, and comment management functionality. Make sure to follow the steps for MongoDB setup and backend startup to ensure the application runs smoothly.










