# Notification News App

This Java desktop application comprises a server and client, using RMI's distributed objects paradigm to push notifications from the server to the client.

To run:
* Make sure Google's GSON library is on the build path for all compilations.
* Compile all files.
* Run the RegistryManager.
* Run the  NewsTickerServerStarter, passing in the location of the machine running the Registry Manager as an argument (eg. localhost).
* Run the NewsTickerClientStarter, also passing in the location of the machine running the Registry Manager.

You can now fetch stories using the server application and send them to all clients subscribed to the relevant topic.

This application is powered by the API from <http://newsapi.org>.

