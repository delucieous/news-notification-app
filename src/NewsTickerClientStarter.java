/**
 * Created by marro on 09/12/2016.
 */
public class NewsTickerClientStarter {

    public static void main(String[] args) {
        NotificationFramework.initialize(args[0]);
        NewsTickerClient client = new NewsTickerClient();
        client.start();
    }
}
