/**
 * Created by marro on 11/12/2016.
 */
public class NewsTickerServerStarter {

    public static void main(String[] args) {
        NotificationFramework.initialize(args[0]);
        NewsTickerServer server = new NewsTickerServer();
        server.start();
    }
}
