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

/**
 * Created by marro on 11/12/2016.
 */
public class NewsTickerServer {

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

    public void publishEvent() {
        try {
            Topic topic = latestEvent.getTopic();
            NotificationSource source = sources.get(topic.getCode());
            Notification<NewsEvent> notification = new Notification<>(latestEvent);
            source.publishToSource(notification);
        } catch (RemoteException re) {
            JOptionPane.showMessageDialog(gui, re.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void getStoryFromTopic(Topic topic) {
        NewsEvent event = null;
        try {
            String jsonString = getJSONFromURL(new URL(buildQueryString(topic)));
            JsonParser parser = new JsonParser();
            JsonObject jo = parser.parse(jsonString).getAsJsonObject();
            String headline = jo.get("articles").getAsJsonArray().get(0).getAsJsonObject().get("title").getAsString();
            System.out.println(headline);
            System.out.println(jsonString);
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
        if (this.latestEvent != null) {
            SwingUtilities.invokeLater(() -> gui.displayLatestEvent(latestEvent));
        }
    }

    private String buildQueryString(Topic topic) {
        StringBuilder queryString = new StringBuilder();
        String finalQuery = "";
        queryString.append("https://newsapi.org/v1/");
        queryString.append("sources?").append(topic.getCode());

        try {
            String jsonString = getJSONFromURL(new URL(queryString.toString()));
            String sourceID = getRandomSource(jsonString);
            finalQuery = buildQueryString(sourceID);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return finalQuery;
    }

    private String buildQueryString(String newsSource) {
        StringBuilder queryString = new StringBuilder();
        queryString.append("https://newsapi.org/v1/");
        queryString.append("articles?source=").append(newsSource).append("&apikey=").append(apiKey);
        return queryString.toString();
    }

    private String getRandomSource(String jsonSources) {
        JsonParser parser = new JsonParser();
        JsonObject sourcesObj = parser.parse(jsonSources).getAsJsonObject();
        JsonArray sourcesArray = sourcesObj.get("sources").getAsJsonArray();

        JsonObject source = sourcesArray.get(rand.nextInt(sourcesArray.size())).getAsJsonObject();

        return source.get("id").getAsString();
    }

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
