import java.util.ArrayList;
import java.util.HashMap;

public class start_read_demultiplex_stats {

    public static void main(String[] args) {

        String XMLfile =
                "/Users/kohleman/Documents/workspace/datastore_server/targets/read-demultiplex-stats-nextseq/" +
                        "150724_J00121_0017_AH2VYMBBXX.sv/ConversionStats.xml";
        ArrayList<read_demultiplex_stats.Statistics> stat = null;
        try {
            stat = read_demultiplex_stats.importXmlAndCalculateStatistics(XMLfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashMap<Integer, read_demultiplex_stats.LaneStatistics> ls = read_demultiplex_stats.calculateTotalLaneStatistics(stat);
//        System.out.println(stat);
//        System.out.println(ls);
    }
}
