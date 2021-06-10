import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jibble.pircbot.*;
import org.json.JSONObject;
import org.json.JSONArray;

//extend the main pircbot class - This can only be done AFTER you import the jar file 
	//This class is the main logic of your pircbot, this is where you will implement any functionality 
public class bot extends PircBot {
	
	static String subject = ""; // easily accessible variable to store user choices
	
	//constructor 
	public bot(){
			this.setName("myWeatherBot"); //this is the name the bot will use to join the IRC server	
		}
	
	//every time a message is sent, this method will be called and this information will be passed on
	//this is how you read a message from the channel 
	public void onMessage(String channel, String sender, 
			String login, String hostname, String message){
				
		// Response to greeting keyword: greets the user back
		if (message.toLowerCase().contains("hello") || message.toLowerCase().contains("hey") ) {
			sendMessage(channel, "Hey " + sender + "! "); // sends friendly greeting to user
		}

		// Response to keyword about weather: sets the subject to "weather",
		// confirms with the user about the selection
		// Prompts the user to enter the location of weather they want
		else if (message.toLowerCase().contains("weather") || message.toLowerCase().contains("temp") || 
				message.toLowerCase().contains("temperature") || message.toLowerCase().contains("humidity")
				|| message.toLowerCase().contains("forecast")) {
			subject = "weather";
			sendMessage(channel, "It looks like you're asking for the weather. Please enter the city name or zip code of the location you're looking into:");
		}
		
		// Response to keyword about ISS: sets the subject to "iss"
		// confirms with the user about the selection
		// Prompts the user to enter the number of upcoming ISS flyovers they want to see
		else if(message.toLowerCase().contains("space") || message.toLowerCase().contains("iss")
				|| message.toLowerCase().contains("rocket")) {
			subject = "iss";
			sendMessage(channel, "Please enter the number of upcoming flyovers you'd like to see (max 100): ");
		}
		
		// if the user didn't give one of the above keywords in their most recent message:
		// checks to see if the subject is already set to weather
		// if it is, it reads in the location from the user and finds the weather
		// and it clears the subject
		else if(subject == "weather") {
			subject = ""; // clears the subject
			if(Character.isDigit(message.charAt(1))) {
				// if it's a zip code, run weatherByZip
				weatherByZip(message, channel);
			}
			
			else {
				// otherwise, run weatherByCity
				weatherByCity(message, channel);
			}
		}
		
		// if the user didn't give one of the above keywords in their most recent message:
		// checks to see if the subject is already set to iss
		// if it is, it reads in the number of flyovers and prints them out	
		// and it clears the subject
		else if(subject == "iss") {
			subject = "";
			int num = Integer.parseInt(message);
			getFlyoverResults(num, channel);	
		}	

		// if the user didn't enter a keyword and didn't already have the subject set before 
		// entering a valid message for that subject, the bot tells the user they don't know
		// what is going on, and to try again
		else {
			sendMessage(channel, "I couldn't tell if you were looking for the weather or upcoming ISS flyover times. Please try again.");
		}			
	}	
		

	// Causes the weather to be printed using the zip code 
	void weatherByZip(String zip, String channel){			
		// generates the valid API url
		String url = "http://api.openweathermap.org/data/2.5/weather?zip="
				+ zip + "&APPID=a658f23a67b7fb004906d3cbfb17081d";
		// runs printWeather function on the result of getResponse
		printWeather(getResponse(url), channel);
		
	}
	
	// Causes the weather to be printed using the city name
	void weatherByCity(String city, String channel) {
		// generates a valid API url
		String url = "http://api.openweathermap.org/data/2.5/weather?q="
				+ city + "&APPID=a658f23a67b7fb004906d3cbfb17081d";	
		// runs PrintWeather function on the result of getResponse
		printWeather(getResponse(url), channel);	
	}	
	
	// Returns the response from an API url
	JSONObject getResponse(String url) {
		try {
			URL obj = new URL(url); // creates URL object with given url
			// opens a connection to that url
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// BufferedReader reads in all the input from the URL
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;	// creates a string to hold info
			// creates a stringBuffer to hold all the info
			StringBuffer response = new StringBuffer(); 

			// while there is still data
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine); // append that data to response
			} in .close();		
			// Converts the StringBuffer into a JSONObject
			JSONObject myresponse = new JSONObject(response.toString());
			
			return myresponse; // returns myresponse
			
		} catch(Exception e) {
			System.out.println(e); // error stuff
			System.exit(1);
			JSONObject error = new JSONObject(1);
			return error;
		}		
	}

	// Converts Kelvin to Fahrenheit, returns an int
	static int tempK2F(double kTemp) {
		double fTemp = ((kTemp - 273.15) * 1.8 + 32); 
		return (int)fTemp; 
	}
	
	// uses the response, prints out the structured weather result
	void printWeather(JSONObject myresponse, String channel) {
		
		try {	
			// creates a JSONObject for the main object
			JSONObject main_object = new JSONObject(myresponse.getJSONObject("main").toString());
			// creates a JSONArray for the weather array
			JSONArray weather_array = myresponse.getJSONArray("weather");
			// creates a JSONObject for the weather object from the weather array
			JSONObject weather_object = weather_array.getJSONObject(0);
		   
			// The following prints out the formatted weather 
			// Current Weather Type: <weather (e.g. cloudy)>, Humidity:  
			// Current Temp: , Low: , High:
			sendMessage(channel, "Current Weather Type: " + weather_object.getString("description")
				+ ", Humidity: " + main_object.getInt("humidity"));
			sendMessage(channel, "Current Temp: " + tempK2F(main_object.getDouble("temp"))
				+ ", Low: " + tempK2F(main_object.getDouble("temp_min"))
		   					+ ", High: " + tempK2F(main_object.getDouble("temp_max")));	   
			
		}catch(Exception e) {
			System.out.println(e); // prints the exceptions
		}		
	}
	
	// uses the response, prints out the structured flyover times
	void getFlyoverResults(int n, String channel) {
		
		// creates a valid API url
		String url = "http://api.open-notify.org/iss-pass.json?lat=34&lon=98&n=" + n;
		
		try {
			JSONObject myresponse = getResponse(url); // returns a JSONObject with the response
			
			// The following gets the JSONArray of response
			JSONArray response_array = myresponse.getJSONArray("response");

			// Creates JSONObject array of the flyovers, Date array of the dates, and 
			// int array of the durations
			JSONObject [] flyover = new JSONObject[n];
			Date [] flyoverDate = new Date[n];
			int [] flyoverDuration = new int[n];
			
			// populate the flyover array
			for(int i = 0; i < n; i++) {
				// The following gets the Object of the Array
				flyover[i] = response_array.getJSONObject(i);
			}

			long gmtToCtOffset = 21600; // the number of seconds that Central Time is 
			// behind Greenwich Mean Time
			
			// The following prints out the formatted flyover times 
			// Flyover: <time and date of flyover>     <duration>
			for(int i = 0; i<n;i++) {
				flyoverDate[i] = new Date((flyover[i].getLong("risetime") - gmtToCtOffset)*1000);
				flyoverDuration[i] = flyover[i].getInt("duration");

				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				sendMessage(channel, df.format(flyoverDate[i]) + "\t     Time of flyover: " 
				+ flyoverDuration[i] / 60 + " minute(s) and " + flyoverDuration[i] % 60 + " second(s).\n");
			}
			
		} catch(Exception e) {
			System.out.println(e); // Prints out exceptions
		}			
	}	
}
