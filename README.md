# Sophomore-API-Project

Recent update: The chatroom this project used to join moved to a new location, so this project won't run 

-------------------------

This project joins an IRC chat room and monitors the messages for key words like "ISS", "space", or "weather" to know which 
of 2 particular tasks it should carry out. The two tasks are: 1, list the time of the next ISS flyover or 2, list the current 
temperature, weather, and the daily highs/lows. 

After hearing a keyword, it will prompt the user for their zipcode so that it knows where to search, and will use that zipcode in 
an API call to acquire the relevant data. It will then parse the json result and send the answers in the chatroom in a more readable 
and human format. 

