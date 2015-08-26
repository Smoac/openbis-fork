import java.util.ArrayList;
import java.util.HashMap;

import static org.testng.AssertJUnit.assertEquals;

public class read_demultiplex_statsTest {

    private String XMLfile =
            "/Users/kohleman/Documents/workspace/datastore_server/targets/read-demultiplex-stats-nextseq/" +
                    "150724_J00121_0017_AH2VYMBBXX.sv/ConversionStats.xml";


    @org.testng.annotations.BeforeMethod
    public void setUp() throws Exception {
    }

    @org.testng.annotations.AfterMethod
    public void tearDown() throws Exception {

    }

    @org.testng.annotations.Test
    public void testImportXmlAndCalculateStatistics() throws Exception {
        ArrayList<read_demultiplex_stats.Statistics> stat = read_demultiplex_stats.importXmlAndCalculateStatistics(XMLfile);
        assertEquals(stat.size(), 39);
        read_demultiplex_stats.Statistics eigth_element = stat.get(8);
        assertEquals(eigth_element.Flowcell, "H2VYMBBXX");
        assertEquals(eigth_element.Barcode, "GCCAATA");
        assertEquals(eigth_element.Sum_RawClusterCount.intValue(), 27850973);
        assertEquals(eigth_element.Sum_RawYield, 8.410993846E9);
        assertEquals(eigth_element.Percentage_PfYieldQ30_PfYield, 87.6792784423112);
        assertEquals(eigth_element.Fraction_PfQualityScoreSum_PfYield, 37.40267477898711);
        assertEquals(eigth_element.Percentage_RawClusterCount_AllRawClusterCounts, 5.770060255141701);
    }

    @org.testng.annotations.Test
    public void testImportXmlAndCalculateStatistics_overall() throws Exception {
        ArrayList<read_demultiplex_stats.Statistics> stat = read_demultiplex_stats.importXmlAndCalculateStatistics(XMLfile);

        Double percentage = 0.0;
        for (read_demultiplex_stats.Statistics element : stat) {
            if (element.Lane.equals(6)) {
                percentage += element.Percentage_RawClusterCount_AllRawClusterCounts;
            }
        }
        assertEquals(100.0, (double)Math.round(percentage * 1000) / 1000);

    }

    @org.testng.annotations.Test
    public void testCalculateTotalLaneStatistics() throws Exception {
        ArrayList<read_demultiplex_stats.Statistics> stat =
                read_demultiplex_stats.importXmlAndCalculateStatistics(XMLfile);

        HashMap<Integer, read_demultiplex_stats.LaneStatistics> lanesStatistics =
                read_demultiplex_stats.calculateTotalLaneStatistics(stat);

        read_demultiplex_stats.LaneStatistics lane2 = lanesStatistics.get(2);
        assertEquals(lane2.Sum_RawClusterCount, 4.826808E8);
        assertEquals(lane2.Sum_PfClusterCount.intValue(), 332293484);
        assertEquals(lane2.Percentage_PfYieldQ30_PfYield, 86.14969791102051);
        assertEquals(lane2.Percentage_PfClusterCount_RawClusterCount, 92.63902299583091);
    }
}