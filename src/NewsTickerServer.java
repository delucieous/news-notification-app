import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/*
This class is responsible for querying the NewsApi.org server to obtain news stories to send as notifications
 */
public class NewsTickerServer {

    //Used to authenticate queries
    private static String apiKey = "9ae379bbbf5947f1a90a18510d8afa6e";
    private HashMap<String, NotificationSource> sources;
    private ArrayList<Topic> registeredTopics;
    private Random rand;
    private RegistryProxyInterface regProxy;
    private NewsEvent latestEvent;
    private ServerGUI gui;

    public NewsTickerServer() {
        sources = new HashMap<>();
        registeredTopics = new ArrayList<>();
        rand = new Random();
    }

    /*
    Contact the registry to get the proxy class and then add the application's enumerated topics to the possible topics
    to advertise
     */
    public void start() {

        try {
            regProxy = (RegistryProxyInterface) Naming.lookup("regProxy");
            for (NewsTopics nt: NewsTopics.values()) {
                Topic topic = nt.getTopic();
                registeredTopics.add(topic);
            }

            gui = new ServerGUI(this, registeredTopics);
            SwingUtilities.invokeLater(gui::initialise);

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /*
    Advertises a topic by creating a new source for it and binding it to the registry
     */
    public void advertiseSource(Topic topic) {
        try {
            NotificationSource source = new NotificationSource(topic, this.regProxy);
            this.regProxy.proxyBind(topic.getCode(), source);
            sources.put(topic.getCode(), source);
            SwingUtilities.invokeLater(() -> gui.removeAdvertisedFromComboBox(topic));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //Publish a news event to the source, where it will be sent to all registered sinks
    public void publishEvent() {
        Topic topic = latestEvent.getTopic();
        NotificationSource source = sources.get(topic.getCode());
        Notification<NewsEvent> notification = new Notification<>(latestEvent);
        source.publishToSource(notification);
    }

    /*
    This method uses Google's GSON library to get a news story for the given topic
    The method has the side effect of setting the "latestEvent" variable to the fetched story so it can then be sent
     */
    public void getStoryFromTopic(Topic topic) {
        NewsEvent event = null;
        try {
            String jsonString = getJSONFromURL(new URL(buildQueryString(topic))); //Build the query and get the result
            JsonParser parser = new JsonParser();
            JsonObject jo = parser.parse(jsonString).getAsJsonObject();
            //Parse the response into a NewsEvent object
            String headline = jo.get("articles").getAsJsonArray().get(0).getAsJsonObject().get("title").getAsString();
            JsonElement description = jo.get("articles").getAsJsonArray().get(0).getAsJsonObject().get("description");
            String textPreview = description.isJsonNull() ? "" : description.getAsString();
            URI url = new URI(jo.get("articles").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString());
            event = new NewsEvent(topic, url, textPreview, headline);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.latestEvent = event;
        //Display the event on the GUI so it can be verified
        if (this.latestEvent != null) {
            SwingUtilities.invokeLater(() -> gui.displayLatestEvent(latestEvent));
        }
    }

    //Uses a string builder to build a string to get articles for a given topic
    private String buildQueryString(Topic topic) {
        StringBuilder queryString = new StringBuilder();
        String finalQuery = "";
        queryString.append("https://newsapi.org/v1/");
        queryString.append("sources?").append(topic.getCode()); //First get the query to find all news sources for a topic

        try {
            //Query the source string and then get a random news outlet from it
            String jsonString = getJSONFromURL(new URL(queryString.toString()));
            String sourceID = getRandomSource(jsonString);
            //Build an articles query for the randomly chosen news source
            finalQuery = buildQueryString(sourceID);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return finalQuery;
    }

    //Overload used to build an articles type query string
    private String buildQueryString(String newsSource) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("https://newsapi.org/v1/");
        queryString.append("articles?source=").append(newsSource).append("&apikey=").append(apiKey);
        return queryString.toString();
    }

    //Choose a random source from a response to a sources query
    private String getRandomSource(String jsonSources) {
        JsonParser parser = new JsonParser();
        JsonObject sourcesObj = parser.parse(jsonSources).getAsJsonObject();
        JsonArray sourcesArray = sourcesObj.get("sources").getAsJsonArray();

        JsonObject source = sourcesArray.get(rand.nextInt(sourcesArray.size())).getAsJsonObject();

        return source.get("id").getAsString();
    }

    //Gets the JSON response from a particular query URL
    private String getJSONFromURL(URL url) {
        StringBuilder jsonText = new StringBuilder();
        String line;
        try(BufferedReader read = new BufferedReader(new InputStreamReader(url.openStream()))) {
            while((line = read.readLine()) != null) {
                jsonText.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return jsonText.toString();

    }

}
