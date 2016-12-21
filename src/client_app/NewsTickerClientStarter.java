package client_app;

import notification_framework.NotificationFramework;

/*
A simple entry point for the client application
 */
public class NewsTickerClientStarter {

    public static void main(String[] args) {
        NotificationFramework.initialize(args[0]);
        NewsTickerClient client = new NewsTickerClient();
        client.start();
    }
}
