# gossamer-client-android
TCSS450 Mobile Application Project: Android GSM social app Gossamer

By Maxfield England and Elijah Freeman

This app serves as a social community for users identifying as LGBTQIA+.

Currently working on: 
Implementing user management via account creation.


# Tasks remaining on backlog:

## High Priority:

###### Backend:
* Create getProfile service to view user profiles
* Create editProfile service to change profile text, tags and displayname

###### Frontend:

* Create main screen featuring at least chronological posts
* Create sidebar with access to user’s profile
* Create profile page displaying at least name, profile
* Create edit profile page 


## Medium Priority:
###### Backend:
* Create post delete service to remove posts from the table
* Create comment table with add and delete services
* Create service to read comments from a particular PostID
* Create post get service searching based on both PostsTags and PostBody

###### Frontend:
* Read posts flagged as anonymous without the poster information
* Include post comment button and implementation
* Load comments that correspond to a particular Post
* Search posts based on PostBody and PostTags. 


## Low Priority:
###### Backend:
* Create field for administrative users in Users table that has universal access to post deletion
* Create user search service based on display name and/or UsersTags
* Implement Events table with EventAdd and EventDelete services
* Implement EventGet service to get events (particularly by day).
* Implement UsersSavedPosts table to link users and posts
* Implement some form of location data to Events table
* Implement images in profiles and posts…. somehow
* Create a DailyMessages table, with add and get services. Configure selection service to first search for a designated day, and otherwise pick at random specifically from undated messages.
* Find a way to coordinate random DailyMessage selection in a manner that’s consistent for all users.

###### Frontend:
* Include administrative user delete options on posts. 
* Search posts based on DisplayName and/or UserTags.
* Create an Event calendar to display upcoming events. 
* Create a location button to filter events based on location. 
* Create a settings panel with toggle for light/dark theme. 
* Create an Event screen with a post new event button. 
* Create an option to save posts. 
* Allow pictures to be appended to text posts. 
* Create a daily message view on the main screen. 
* Create a direct messaging screen with messaging ability. 
* Create an option to add image to profile page. 

Extended goals and information found below:
https://drive.google.com/file/d/1pt1YAZmzE9lKDM3LY34Cr_6uacWDptwm/view?usp=sharing

Task list:
https://docs.google.com/document/d/14FCgNorSTrOBw0j9kYRw_GQeIFjyA-U3TVVUV_w-qSk/edit?usp=sharing
