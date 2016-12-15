/*
A simple entry point for the server application
 */
public class NewsTickerServerStarter {

    public static void main(String[] args) {
        NotificationFramework.initialize(args[0]);
        NewsTickerServer server = new NewsTickerServer();
        server.start();
    }
}
