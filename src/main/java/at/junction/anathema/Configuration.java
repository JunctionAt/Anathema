package at.junction.anathema;

public class Configuration {
    private Anathema plugin;
    public String SERVERNAME;
    public String ENDPOINT;
    public String APIKEY;
    public  String BANAPPEND;

    public Configuration(Anathema plugin){
        this.plugin = plugin;
    }

    public void load(){
        SERVERNAME = plugin.getConfig().getString("server");
        ENDPOINT = plugin.getConfig().getString("endpoint");
        APIKEY = plugin.getConfig().getString("ApiKey");
        BANAPPEND = plugin.getConfig().getString("banAppend");

    }
}
