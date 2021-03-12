package ch.ethz.sis.monitor;

public class MonitorTest
{
    public static void main(String[] args) {
        String URL = "https://openbis-eln-peter.ethz.ch/";
        String TIMEOUT = "30000";
        String user = "admin";
        String password = "changeit";
        String datasetPermId = "20150407224113301-26972";
        Monitor.main(URL, TIMEOUT, user, password, datasetPermId);
    }
}
