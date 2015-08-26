/*
@copyright: 2015 ETH Zuerich, CISD

@license:
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author: Fabian Gemperle
@autor: Manuel Kohler

@note: This Class is in Jython importable as Java-Library after compiling it.
       In compilation 4 Class files arise:
       - read_demultiplex_stats.class
       - read_demultiplex_stats$SampleItem.class
       - read_demultiplex_stats$Sample.class
       - read_demultiplex_stats$Statistics.class
 */


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//##########################################################

public final class read_demultiplex_stats_miseq_hiseq
{

    public static ArrayList<Statistics> importXmlAndCalculateStatistics(String XMLFile) throws Exception {

        ArrayList<Statistics> stats = executeImportXmlAndCalculateStatistics(XMLFile);
        HashMap<Integer, ArrayList<Statistics>> groupedList = groupByLane(stats);

        for (Statistics s : stats)
            s.adaptStatisticsWithRespectToAllSamples(stats, groupedList);
        return stats;
    }

    /**
     *
     * Streams through a given XML file, parses the data and writes it into a Statistics object
     *
     * @param XMLfile: ConversionStats.xml
     * @return a ArrayList of <Statistics>
     */
    private static  ArrayList<Statistics> executeImportXmlAndCalculateStatistics(String XMLfile) throws Exception {
        ArrayList<Statistics> sampleStatisticsList = new ArrayList<>();

        /*Parse corresponding XML file, put all values into a memory structure, calculate statistics overall samples.
          Output: sampleStatisticsList (list of Sample-Statistics-Objects)
          Input:  XMLfile having structure of file ConversionStats.xml (Example of 1.10.2014):
                  <?xml version="1.0" encoding="utf-8"?>                             => Assumptions about XML structure:
                  <Stats>                                                            => Element is singlechild
                    <Flowcell flowcell-id="H0YVKBGXX">                               => Element is singlechild
                      <Project name="BSSE_QGF_23096_H0YVKBGXX_1">  => Element is one of many children, additionally there is summary-element with attribute name="all"
                        <Sample name="BSSE_QGF_23096_H0YVKBGXX_1_PZ27_PZ33_CelSEQ_"> => Element is singlechild except second summary-element with attribute name="all"
                          <Barcode name="unknown">                                   => Element is singlechild except second summary-element with attribute name="all"
                            <Lane number="1">                                        => Element is one of several children
                              <Tile number="11101">                                  => Element is one of many children
                                <Raw>                                                => Element is singlechild
                                  <ClusterCount>328653</ClusterCount>                => Element is singlechild
                                  <Read number="1">                                  => Element is one of several children
                                    <Yield>24977628</Yield>                          => Element is singlechild
                                    <YieldQ30>16162292</YieldQ30>                    => Element is singlechild
                                    <QualityScoreSum>703070796</QualityScoreSum>     => Element is singlechild
                                  </Read>
                                  <Read number="2">                                  => Element is one of several children
                                    <Yield>24977628</Yield>                          => Element is singlechild
                                    <YieldQ30>16233173</YieldQ30>                    => Element is singlechild
                                    <QualityScoreSum>699507245</QualityScoreSum>     => Element is singlechild
                                  </Read>
                                </Raw>
                                <Pf>                                                 => Element is singlechild
                                  <ClusterCount>302121</ClusterCount>                => Element is singlechild
                                  <Read number="1">                                  => Element is one of several children
                                    <Yield>22961196</Yield>                          => Element is singlechild
                                    <YieldQ30>15842531</YieldQ30>                    => Element is singlechild
                                    <QualityScoreSum>686898532</QualityScoreSum>     => Element is singlechild
                                  </Read>
                                  <Read number="2">                                  => Element is one of several children
                                    <Yield>22961196</Yield>                          => Element is singlechild
                                    <YieldQ30>16233173</YieldQ30>                    => Element is singlechild
                                    <QualityScoreSum>699507245</QualityScoreSum>     => Element is singlechild
                                  </Read>
                                </Pf>
                              </Tile>
                              <Tile number="11102">
                              [...]

                      </Project>
                      <Lane number="2">
                        <TopUnknownBarcodes>
                          <Barcode count="150988002" sequence="NNNNNNN"/>
                          <Barcode count="167095" sequence="CCCCCCC"/>
                          <Barcode count="57859" sequence="CCCGTCC"/>
                          <Barcode count="49993" sequence="CCGATGT"/>
                          <Barcode count="42981" sequence="GTCCCGC"/>
                          <Barcode count="40446" sequence="TAAAATT"/>
                          <Barcode count="39962" sequence="CCAGATC"/>
                          <Barcode count="34688" sequence="GATGTAT"/>
                          <Barcode count="32013" sequence="AAAAAGT"/>
                          <Barcode count="30501" sequence="AGATCAT"/>
                        </TopUnknownBarcodes>
                      </Lane>
                      [...]
        */

//        Map<Integer, Map<String, Integer>> barcodesPerLane = null;

        try {
            String errorMessage;
            int event;
            int skip;
            String flowcellName = "";
            String projectName = "";
            String sampleName = "";
            String barcodeName = "";
            int laneNumber = 0;
            int tileNumber;
            Sample currentSample = null;
            Statistics statistics;
            SampleItem rawItem;
            SampleItem pfItem;
            Map<String, Integer> barcodesMap = new HashMap<>();
            Map<Integer, Map<String, Integer>> barcodesPerLane = new HashMap<>();

                    InputStream xmlFile = new FileInputStream(XMLfile);
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlParser = xmlFactory.createXMLStreamReader(xmlFile);

            // Start-Tag "Stats":
            event = xmlParser.nextTag(); // Assumption: just white space or comments are aside explicit start-tag
            if (event != XMLStreamConstants.START_ELEMENT || !xmlParser.getLocalName().equals("Stats")) {
                errorMessage =
                        "STRANGE ERROR IN METHOD importXmlAndCalculateStatistics WHEN READING IN XMLFILE. => CHECK CODE AND XMLFILE-STRUCTURE! Got " + event;
                throw new Exception(errorMessage);
            }

            // Loop over potential Tags "Flowcell":
            event = xmlParser.nextTag(); // Assumption: just white spaces or comments are aside start- or end-tag
            //            List<Statistics> samplestatistics = new ArrayList<Statistics>();
            boolean doimport = true;
            while (doimport) {
                // concerning tag Flowcell:
                if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Flowcell")) {
                    flowcellName = xmlParser.getAttributeValue(0);
                    event = xmlParser.nextTag();
                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Flowcell")) {
                    flowcellName = "";
                    event = xmlParser.nextTag();

                    // concerning tag Project:
                } else if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Project")) {
                    if (xmlParser.getAttributeValue(0).equals("all")) {
                        //skip the current XML element and all of its following subelements:
                        skip = 1;
                        while (skip > 0) {
                            event = xmlParser.next();
                            switch (event) {
                                case XMLStreamConstants.END_ELEMENT:
                                    skip -= 1;
                                    break;
                                case XMLStreamConstants.START_ELEMENT:
                                    skip += 1;
                                    break;
                                default:
                                    skip += 0; // text elements, spaces, ...
                                    break;
                            }
                        }
                    } else {
                    projectName = xmlParser.getAttributeValue(0);

                    }
                    event = xmlParser.nextTag();
                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Project")) {
                    projectName = "";
                    event = xmlParser.nextTag();

                    // concerning tag Sample:
                } else if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Sample")) {
                    if (xmlParser.getAttributeValue(0).equals("all")) {
                        // skip the current XML element and all of its following subelements:
                        skip = 1;
                        while (skip > 0) {
                            event = xmlParser.next();
                            switch (event) {
                                case XMLStreamConstants.END_ELEMENT:
                                    skip -= 1;
                                    break;
                                case XMLStreamConstants.START_ELEMENT:
                                    skip += 1;
                                    break;
                                default:
                                    skip += 0; // text elements, spaces, ...
                                    break;
                            }
                        }
                    } else {
                        sampleName = xmlParser.getAttributeValue(0);
                    }
                    event = xmlParser.nextTag();
                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Sample")) {
                    sampleName = "";
                    event = xmlParser.nextTag();

                    // concerning tag Barcode (which is as well the start/end of Project-/Sample-Entry):
                } else if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Barcode")) {
                    if (xmlParser.getAttributeValue(0).equals("all")) {
                        // skip the current XML element and all of its following subelements:
                        skip = 1;
                        while (skip > 0) {
                            event = xmlParser.next();
                            switch (event) {
                                case XMLStreamConstants.END_ELEMENT:
                                    skip -= 1;
                                    break;
                                case XMLStreamConstants.START_ELEMENT:
                                    skip += 1;
                                    break;
                                default:
                                    skip += 0; // text elements, spaces, ...
                                    break;
                            }
                        }
                    } else {
                        barcodeName = xmlParser.getAttributeValue(0);
                    }
                    event = xmlParser.nextTag();
                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Barcode")) {
                    barcodeName = "";
                    event = xmlParser.nextTag();


                    // concerning Lane:
                } else if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Lane")) {
//                    System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
                    laneNumber = Integer.parseInt(xmlParser.getAttributeValue(0));

                    // Now we have everything we need, we put it into a Sample
                    currentSample = new Sample();

                    if (sampleName.startsWith("Undetermined")) {
                        currentSample.Sample = sampleName + "_" + flowcellName + "_" + laneNumber;
                    } else {
                        currentSample.Sample = sampleName;
                    }

                    currentSample.Flowcell = flowcellName;
                    currentSample.Lane = laneNumber;
                    currentSample.Project = projectName;
                    currentSample.Barcode = barcodeName;
                    event = xmlParser.nextTag();
//                    System.out.println(currentSample.Sample);


                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Lane")) {
                    // Statistics 1st step: calculate individual statistics per sample:
                    statistics = new Statistics(currentSample);
                    if (!currentSample.Project.equals("")) {
                        sampleStatisticsList.add(statistics);
                    }
                    currentSample = null;
                    statistics = null;

//                    laneNumber = Double.NaN;
                    event = xmlParser.nextTag();

                    // concerning Tile with all its sub-elements:
                } else if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Tile")) {
                    tileNumber = Integer.parseInt(xmlParser.getAttributeValue(0));
                    // concerning Raw with Assumption: Raw-element is singlechild:
                    xmlParser.nextTag();
                    rawItem = new SampleItem();
                    rawItem.Type = "Raw";
                    rawItem.Lane = laneNumber;
                    rawItem.Tile = tileNumber;
                    xmlParser.nextTag();
                    rawItem.ClusterCount = Integer.parseInt(xmlParser.getElementText()); // Assumption: ClusterCount-element is numeric singlechild
                    //System.out.println("\nValue: ClusterCount=" + rawItem.ClusterCount);
                    xmlParser.nextTag();
                    while (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Read")) {
                        //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
                        xmlParser.nextTag();
                        rawItem.YieldList.add(Double.parseDouble(xmlParser.getElementText()));
                        xmlParser.nextTag();
                        rawItem.YieldQ30List.add(Double.parseDouble(xmlParser.getElementText()));
                        xmlParser.nextTag();
                        rawItem.QualityScoreSumList.add(Double.parseDouble(xmlParser.getElementText()));
                        xmlParser.nextTag();
                        xmlParser.nextTag();
                        //System.out.println("Values in Read: Yield=" + rawItem.YieldList.get(rawItem.YieldList.size()-1) + ", YieldQ30=" + rawItem.YieldQ30List.get(rawItem.YieldQ30List.size()-1) + ", QualityScoreSum=" + rawItem.QualityScoreSumList.get(rawItem.QualityScoreSumList.size()-1));
                    }
                    //System.out.println("\nRaw-SampleItem  " + rawItem);
                    currentSample.RawList.add(rawItem);


                    rawItem = null;
                    // concerning Pf with Assumption that entire Pf-element is structured same as Raw:
                    xmlParser.nextTag();
                    //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName());
                    pfItem = new SampleItem();
                    pfItem.Type = "Pf";
                    pfItem.Lane = laneNumber;
                    pfItem.Tile = tileNumber;
                    xmlParser.nextTag();
                    pfItem.ClusterCount = Integer.parseInt(xmlParser.getElementText());
                    //System.out.println("\nValue: ClusterCount=" + pfItem.ClusterCount);
                    xmlParser.nextTag();
                    while (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Read")) {
                        //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
                        xmlParser.nextTag();
                        pfItem.YieldList.add(Double.parseDouble(xmlParser.getElementText()));
                        xmlParser.nextTag();
                        pfItem.YieldQ30List.add(Double.parseDouble(xmlParser.getElementText()));
                        xmlParser.nextTag();
                        pfItem.QualityScoreSumList.add(Double.parseDouble(xmlParser.getElementText()));
                        xmlParser.nextTag();
                        xmlParser.nextTag();
                        //System.out.println("Values in Read: Yield=" + pfItem.YieldList.get(pfItem.YieldList.size()-1) + ", YieldQ30=" + pfItem.YieldQ30List.get(pfItem.YieldQ30List.size()-1) + ", QualityScoreSum=" + pfItem.QualityScoreSumList.get(pfItem.QualityScoreSumList.size()-1));
                    }
                    //System.out.println("\nPf-SampleItem  " + pfItem);
                    currentSample.PfList.add(pfItem);
                    pfItem = null;
                    // attain end of current Tile and afterwards continue in next Tile/Lane/Barcode/Sample/Project:
                    event = xmlParser.nextTag();
                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Tile")) {
                    tileNumber = 0;
                    event = xmlParser.nextTag();


                } else if (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("TopUnknownBarcodes")) {

                    event = xmlParser.nextTag();
                    while (event == XMLStreamConstants.START_ELEMENT && xmlParser.getLocalName().equals("Barcode")) {
                        Integer numberOfBarcodes = Integer.valueOf((xmlParser.getAttributeValue(0)));
                        String barcode = xmlParser.getAttributeValue(1);
                        barcodesMap.put(barcode, numberOfBarcodes);
                        xmlParser.nextTag();
                        event = xmlParser.nextTag();
                    }

                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("TopUnknownBarcodes")) {

                    barcodesPerLane.put(laneNumber, barcodesMap);
                    event = xmlParser.nextTag();

                    //  concerning finish of reading in XML or hit upon error due XML content:
                } else if (event == XMLStreamConstants.END_ELEMENT && xmlParser.getLocalName().equals("Stats")) {
                    // this final part of while loop is just for analyzing potential errors, but
                    // could be removed and changed in:  while xmlparser.getLocalName() != 'Stats'
                    doimport = false;
                } else {
                    doimport = false;
                    System.out.println("Warning: Different XML structure than expectet. Got event: " + event);
                }
            }
            xmlParser.close();

        } catch (FileNotFoundException | XMLStreamException | IllegalArgumentException e) {
            System.out.println("OCCURRED EXCEPTION " + e.toString());
            e.printStackTrace();
        }
        return sampleStatisticsList;
    }

    public static HashMap<Integer, LaneStatistics> calculateTotalLaneStatistics(ArrayList<Statistics> samplestatisticslist)
    {
        HashMap<Integer, LaneStatistics> listOfLaneStatistics= new HashMap<Integer, LaneStatistics>();

        HashMap<Integer, ArrayList<Statistics>> groupedList = groupByLane(samplestatisticslist);

        for (Integer key : groupedList.keySet()) {

            LaneStatistics laneStats = new LaneStatistics(groupedList.get(key));
            laneStats.calculateOverallStats(key);
            listOfLaneStatistics.put(key, laneStats);
        }
        return listOfLaneStatistics;
    }


    private static HashMap<Integer, ArrayList<Statistics>> groupByLane(ArrayList<Statistics> samplestatisticslist) {
        // Group ArrayList by Lane
        HashMap<Integer, ArrayList<Statistics>> groupedList = new HashMap <Integer, ArrayList<Statistics>>();
        for (Statistics s : samplestatisticslist) {

            if (groupedList.containsKey(s.Lane)) {
                groupedList.get(s.Lane).add(s);
            }
            else {
                ArrayList <Statistics> newEntry = new ArrayList<Statistics>();
                newEntry.add(s);
                groupedList.put(s.Lane, newEntry);
            }
        }
        return groupedList;
    }


    // ##########################################################

    private static class SampleItem
    {
        /*
         * Object of an item in sample including - the corresponding type Raw or Pf - the index of corresponding Lane, Tile - measured value of
         * ClusterCount and values in Lists (w.r.t. Read) of Yield, YieldQ30, QualityScoreSum
         */

        public String Type = "";

        public Integer Lane = 0;

        public Integer Tile = 0;

        public Integer ClusterCount = 0;

        // Define unknown ArrayList of numerical values with changeable List size. Type Double(object) instead of double(primitive) is necessary in
        // Lists.
        public List<Double> YieldList = new ArrayList<>();

        public List<Double> YieldQ30List = new ArrayList<>();

        public List<Double> QualityScoreSumList = new ArrayList<>();

        public String toString()
        {
            return "Type: " + this.Type + ", Lane: " + (long) this.Lane + ", Tile: " + (long) this.Tile
                    + ", ClusterCount: " + (long) this.ClusterCount + ", YieldList: " + this.YieldList
                    + ", YieldQ30List: " + this.YieldQ30List + ", QualityScoreSumList: " + this.QualityScoreSumList;
        }

    }

    // ##########################################################

    public static class Sample
    {
        /*
         * Object of an entire sample including - the name of Flowcell, Project, Sample, Barcode - the list of Raw and Pf SampleItem-Objects
         */

        public String Flowcell = "";
        
        public Integer Lane = 0;

        public String Project = "";

        public String Sample = "";

        public String Barcode = "";

        // Define unknown ArrayList of SampleItems:
        public List<SampleItem> RawList = new ArrayList<>();

        public List<SampleItem> PfList = new ArrayList<>();

        public String toString()
        {
            return "Flowcell: " + this.Flowcell + ", Lane: " + this.Lane + ", Project: " + this.Project + ", Sample: " + this.Sample + ", Barcode: " + this.Barcode
                    + ", RawList: " + this.RawList + ", PfList: " + this.PfList;
        }

    }

    // ##########################################################

    public static class Statistics extends Sample
    {
        /*
         * Object of Statistics within one single sample inherited from Sample-Object
         */

        public Integer Sum_RawClusterCount = 0;

        public Integer Sum_PfClusterCount = 0;

        public double Sum_RawYield = Double.NaN;

        public double Sum_PfYield = Double.NaN;

        public double Sum_RawYieldQ30 = Double.NaN;

        public double Sum_PfYieldQ30 = Double.NaN;

        public double Sum_RawQualityScoreSum = Double.NaN;

        public double Sum_PfQualityScoreSum = Double.NaN;

        public double Mega_RawYield = Double.NaN; // obvious double, but could be turned into int

        public double Mega_PfYield = Double.NaN; // obvious double, but could be turned into int

        public double Percentage_PfYield_RawYield = Double.NaN;

        public double Percentage_PfYieldQ30_PfYield = Double.NaN;

        public double Fraction_PfQualityScoreSum_PfYield = Double.NaN;

        public double Percentage_PfClusterCount_RawClusterCount = Double.NaN;

        public double Percentage_RawClusterCount_AllRawClusterCounts = Double.NaN;

        public Statistics(Sample sample)
        {
            /*
             * Constructor of derived class Initialization: Already initialized Sample-Object is necessary argument.
             */

            super();
            Flowcell = sample.Flowcell;
            Lane = sample.Lane;
            Project = sample.Project;
            Sample = sample.Sample;
            Barcode = sample.Barcode;
            RawList = sample.RawList;
            PfList = sample.PfList;

            if (RawList.size() > 0)
                Sum_RawClusterCount = 0;
            for (SampleItem s : RawList)
                Sum_RawClusterCount += s.ClusterCount;
            if (PfList.size() > 0)
                Sum_PfClusterCount = 0;
            for (SampleItem s : PfList)
                Sum_PfClusterCount += s.ClusterCount;
            if (RawList.size() > 0)
                Sum_RawYield = 0;
            for (SampleItem s : RawList)
                for (double d : s.YieldList)
                    Sum_RawYield += d;
            if (PfList.size() > 0)
                Sum_PfYield = 0;
            for (SampleItem s : PfList)
                for (double d : s.YieldList)
                    Sum_PfYield += d;
            if (RawList.size() > 0)
                Sum_RawYieldQ30 = 0;
            for (SampleItem s : RawList)
                for (double d : s.YieldQ30List)
                    Sum_RawYieldQ30 += d;
            if (PfList.size() > 0)
                Sum_PfYieldQ30 = 0;
            for (SampleItem s : PfList)
                for (double d : s.YieldQ30List)
                    Sum_PfYieldQ30 += d;
            if (RawList.size() > 0)
                Sum_RawQualityScoreSum = 0;
            for (SampleItem s : RawList)
                for (double d : s.QualityScoreSumList)
                    Sum_RawQualityScoreSum += d;
            if (PfList.size() > 0)
                Sum_PfQualityScoreSum = 0;
            for (SampleItem s : PfList)
                for (double d : s.QualityScoreSumList)
                    Sum_PfQualityScoreSum += d;
            // Mega_RawYield = calculate_MegaUnit(Sum_RawYield);
            // Mega_PfYield = calculate_MegaUnit(Sum_PfYield);

            Mega_RawYield = Sum_RawYield;
            Mega_PfYield = Sum_PfYield;

            Percentage_PfYield_RawYield = calculate_Percentage(Sum_PfYield, Sum_RawYield);
            Percentage_PfYieldQ30_PfYield = calculate_Percentage(Sum_PfYieldQ30, Sum_PfYield);
            Fraction_PfQualityScoreSum_PfYield = calculate_Fraction(Sum_PfQualityScoreSum, Sum_PfYield);
            Percentage_PfClusterCount_RawClusterCount = calculate_Percentage(Sum_PfClusterCount, Sum_RawClusterCount);
            // Calculation of attribute "Percentage_RawClusterCount_AllRawClusterCounts" needs statistics of all other included samples. => After
            // initializing this object, apply method: adaptStatisticsWithRespectToAllSamples(statisticslist)
        }

        public String toString()
        {
            return "Flowcell: " + this.Flowcell + ", Lane: "+ this.Lane + ", Project: " + this.Project + ", Sample: " + this.Sample + ", Barcode: " + this.Barcode
                    + ", Raw Clusters: " + (long) this.Sum_RawClusterCount + ", Mbases Raw Yield: " + this.Mega_RawYield
                    + ", % Raw Clusters overall: " + this.Percentage_RawClusterCount_AllRawClusterCounts
                    + ", Pf Clusters: " + (long) this.Sum_PfClusterCount + ", Mbases Pf Yield: " + this.Mega_PfYield
                    + ", % PfYield/RawYield: " + this.Percentage_PfYield_RawYield
                    + ", % PfYieldQ30/PfYield: " + this.Percentage_PfYieldQ30_PfYield
                    + ", Mean Pf Quality Score: " + this.Fraction_PfQualityScoreSum_PfYield
                    + ", % Passes Filtering: " + this.Percentage_PfClusterCount_RawClusterCount + "\n";
        }

        public void adaptStatisticsWithRespectToAllSamples(ArrayList<Statistics> statisticslist, HashMap<Integer, ArrayList<Statistics>>  groupedList)
        {
            /*
             * This Statistics-Object (corresponding to one sample) is adapted by employing a list of Statistics-Objects corresponding to all samples
             * influencing the statistics of single sample. Input: statisticslist contains all Statistics-Objects
             */
            if (statisticslist.size() == 0)
            { // here it additionally should be checked, if calling object is included ...
                Percentage_RawClusterCount_AllRawClusterCounts = Double.NaN;
                String errormessage =
                        "INPUT ARGUMENT statisticslist MUST BE LIST OF Statistics OBJECTS INCLUDING THE CALLING OBJECT" +
                                " IN METHOD adaptStatisticsWithRespectToAllSamples!";
                throw new IllegalArgumentException(errormessage); // Exception reasonable since otherwise wrong results.
            } else
            {
                for (Integer key : groupedList.keySet()) {
                    double sumAllRawClusterCounts = 0;
                    ArrayList<Statistics> groupList = groupedList.get(key);
                    for (Statistics element : groupList) {
                        sumAllRawClusterCounts += element.Sum_RawClusterCount;
                    }
                    for (Statistics element : groupList) {
                        element.Percentage_RawClusterCount_AllRawClusterCounts =
                                calculate_Percentage(element.Sum_RawClusterCount, sumAllRawClusterCounts);
                    }
                }
            }
        }


        public double calculate_Percentage(double x, double y)
        {
            double z = x / y;
            if (z == Double.POSITIVE_INFINITY || z == Double.NEGATIVE_INFINITY || Double.isNaN(z))
            {
                z = Double.NaN;
            } else
            {
                z = 100 * z;
            }
            return z;
        }

        public double calculate_Fraction(double x, double y)
        {
            double z = x / y;
            if (z == Double.POSITIVE_INFINITY || z == Double.NEGATIVE_INFINITY || Double.isNaN(z))
            {
                z = Double.NaN;
            }
            return z;
        }
    }

    public static class LaneStatistics
    {
        /*
         * Total Lane Statistics
         */
        public ArrayList<Statistics> statisticsList;

        public Integer Lane = 0;

        public double Sum_RawClusterCount = 0.0;

        public Integer Sum_PfClusterCount = 0;

        public double Sum_RawYield = 0.0;

        public double Sum_PfYield = 0.0;

        public double Sum_RawYieldQ30 = 0.0;

        public double Sum_PfYieldQ30 = 0.0;

        public double Sum_RawQualityScoreSum = 0.0;

        public double Sum_PfQualityScoreSum = 0.0;

        public double Percentage_PfYield_RawYield = 0.0;

        public double Percentage_PfYieldQ30_PfYield = 0.0;

        public double Fraction_PfQualityScoreSum_PfYield = 0.0;

        public double Percentage_PfClusterCount_RawClusterCount = 0.0;

        public double Percentage_RawClusterCount_AllRawClusterCounts = 0.0;

        public double Clusters_PfWithoutNoindex = 0.0;

        public LaneStatistics(ArrayList<Statistics> statisticsList)
        {
            this.statisticsList = statisticsList;
        }

        public void calculateOverallStats(Integer key)
        {
            int sampleNumber = statisticsList.size();
            for (Statistics s : statisticsList) {
                if (!s.Barcode.equals("") || s.Barcode.equals("unknown")) {
                    this.Clusters_PfWithoutNoindex += s.Sum_PfClusterCount;
                }

                this.Sum_RawClusterCount += s.Sum_RawClusterCount;
                this.Sum_PfClusterCount += s.Sum_PfClusterCount;
                this.Sum_RawYield += s.Sum_RawYield;
                this.Sum_PfYield += s.Sum_PfYield;
                this.Sum_RawYieldQ30 += s.Sum_RawYieldQ30;
                this.Sum_PfYieldQ30 += s.Sum_PfYieldQ30;
                this.Sum_RawQualityScoreSum += s.Sum_RawQualityScoreSum;
                this.Sum_PfQualityScoreSum += s.Sum_PfQualityScoreSum;
                this.Percentage_PfYield_RawYield += s.Percentage_PfYield_RawYield;
                if (!Double.isNaN(s.Percentage_PfYieldQ30_PfYield)) {
                    this.Percentage_PfYieldQ30_PfYield += s.Percentage_PfYieldQ30_PfYield;
                }
                this.Percentage_PfClusterCount_RawClusterCount += s.Percentage_PfClusterCount_RawClusterCount;
                this.Fraction_PfQualityScoreSum_PfYield += s.Fraction_PfQualityScoreSum_PfYield;
            }

            this.Lane = key;
            this.Sum_RawQualityScoreSum = this.Sum_RawQualityScoreSum / sampleNumber;
            this.Sum_PfQualityScoreSum = this.Sum_PfQualityScoreSum / sampleNumber;
            this.Percentage_PfYield_RawYield = this.Percentage_PfYield_RawYield / sampleNumber;
            this.Percentage_PfYieldQ30_PfYield = this.Percentage_PfYieldQ30_PfYield / (double) sampleNumber;
            this.Percentage_PfClusterCount_RawClusterCount = this.Percentage_PfClusterCount_RawClusterCount / (double) sampleNumber;
            this.Fraction_PfQualityScoreSum_PfYield = this.Fraction_PfQualityScoreSum_PfYield / (double) sampleNumber;
        }

        public String toString()
        {
            return "Lane " + this.Lane + " Sum_RawClusterCount " + this.Sum_RawClusterCount + " Sum_PfClusterCount " + this.Sum_PfClusterCount +
                    " Percentage_PfYieldQ30_PfYield " + this.Percentage_PfYieldQ30_PfYield +
                    " Percentage_PfClusterCount_RawClusterCount " + this.Percentage_PfClusterCount_RawClusterCount;
        }
    }
}