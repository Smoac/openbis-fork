/*
@copyright: 2012 ETH Zuerich, CISD

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

@note: This Class is in Jython importable as Java-Library after compiling it.
       In compilation 4 Class files arise:
       - read_demultiplex_stats.class
       - read_demultiplex_stats$SampleItem.class
       - read_demultiplex_stats$Sample.class
       - read_demultiplex_stats$Statistics.class
*/

import java.util.List;
import java.util.ArrayList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader; 
import java.io.FileInputStream; 
import java.io.FileNotFoundException;
import java.io.InputStream;

//##########################################################

public final class read_demultiplex_stats {

  public List<Statistics> samplestatisticslist = new ArrayList<Statistics>();

  public read_demultiplex_stats() {
    // This Class-Constructor is unnecessary???
  }

  public List<Statistics> importXMLdata_and_calculateStatistics(String XMLfile) {
    /*Parse corresponding XML file, put all values into a memory structure, calculate statistics overall samples.
      Output: samplestatisticslist (list of Sample-Statistics-Objects)
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
    */

    try {

      // temporary variables (frequently changing during XML-read-in):
      String errormessage = "";
      int event = 0;
      int skip = 0;
      String curflowcellname = "";
      String curprojectname = "";
      String cursamplename = "";
      String curbarcodename = "";
      double curlanenumber = Double.NaN;  // obvious double, but could be turned into int
      double curtilenumber = Double.NaN;  // obvious double, but could be turned into int
      Sample cursample = null;
      Statistics curstatistics = null;
      SampleItem currawitem = null;
      SampleItem curpfitem = null;

      InputStream xmlfile = new FileInputStream(XMLfile);
      XMLInputFactory xmlfactory = XMLInputFactory.newInstance();
      XMLStreamReader xmlparser = xmlfactory.createXMLStreamReader(xmlfile);

      // Start-Tag "Stats":
      event = xmlparser.nextTag();  // Assumption: just white space or comments are aside explicit start-tag
      if (event != XMLStreamConstants.START_ELEMENT || !xmlparser.getLocalName().equals("Stats")) {
        errormessage = "STRANGE ERROR IN METHOD importXMLdata_and_calculateStatistics WHEN READING IN XMLFILE. => CHECK CODE AND XMLFILE-STRUCTURE!";
        //System.out.println(errormessage);
        throw new Exception(errormessage);
      }
      //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));

      // Loop over potential Tags "Flowcell":
      event = xmlparser.nextTag();  // Assumption: just white spaces or comments are aside start- or end-tag
      List<Statistics> samplestatistics = new ArrayList<Statistics>();
      boolean doimport = true;
      while (doimport) {

        // concerning tag Flowcell:
        if (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Flowcell")) {
          //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
          curflowcellname = xmlparser.getAttributeValue(0);  // Assumption: Flowcell-attribute flowcell-id is just string
          event = xmlparser.nextTag();
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Flowcell")) {
          curflowcellname = "";
          event = xmlparser.nextTag();

        // concerning tag Project:
        } else if (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Project")) {
          //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
          if (xmlparser.getAttributeValue(0).equals("all")) {
            // skip the current XML element and all of its following subelements:
            skip = 1;
            while (skip > 0) {
              event = xmlparser.next();
              switch (event) {
                case XMLStreamConstants.END_ELEMENT: skip -= 1;
                                                     break; // break-command after each case is necessary in switch-statement
                case XMLStreamConstants.START_ELEMENT: skip += 1;
                                                       break; // break-command after each case is necessary in switch-statement
                default: skip += 0;  // text elements, spaces, ...
                         break; // break-command after each case is necessary in switch-statement
              }
            }
          } else {
            curprojectname = xmlparser.getAttributeValue(0);  // Assumption: Project-attribute name is just string
          }
          event = xmlparser.nextTag();
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Project")) {
          curprojectname = "";
          event = xmlparser.nextTag();

        // concerning tag Sample:
        } else if (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Sample")) {
          //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
          if (xmlparser.getAttributeValue(0).equals("all")) {
            // skip the current XML element and all of its following subelements:
            skip = 1;
            while (skip > 0) {
              event = xmlparser.next();
              switch (event) {
                case XMLStreamConstants.END_ELEMENT: skip -= 1;
                                                     break; // break-command after each case is necessary in switch-statement
                case XMLStreamConstants.START_ELEMENT: skip += 1;
                                                       break; // break-command after each case is necessary in switch-statement
                default: skip += 0;  // text elements, spaces, ...
                         break; // break-command after each case is necessary in switch-statement
              }
            }
          } else {
            cursamplename = xmlparser.getAttributeValue(0);  // Assumption: Sample-attribute name is just string
          }
          event = xmlparser.nextTag();
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Sample")) {
          cursamplename = "";
          event = xmlparser.nextTag();

        // concerning tag Barcode (which is as well the start/end of Project-/Sample-Entry):
        } else if (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Barcode")) {
          //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
          if (xmlparser.getAttributeValue(0).equals("all")) {
            // skip the current XML element and all of its following subelements:
            skip = 1;
            while (skip > 0) {
              event = xmlparser.next();
              switch (event) {
                case XMLStreamConstants.END_ELEMENT: skip -= 1;
                                                     break; // break-command after each case is necessary in switch-statement
                case XMLStreamConstants.START_ELEMENT: skip += 1;
                                                       break; // break-command after each case is necessary in switch-statement
                default: skip += 0;  // text elements, spaces, ...
                         break; // break-command after each case is necessary in switch-statement
              }
            }
          } else {
            curbarcodename = xmlparser.getAttributeValue(0);  // Assumption: Barcode-attribute name is just string
            cursample = new Sample();
            cursample.Flowcell = curflowcellname;
            cursample.Project = curprojectname;
            cursample.Sample = cursamplename;
            cursample.Barcode = curbarcodename;
          }
          event = xmlparser.nextTag();
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Barcode")) {
          // Statistics 1st step: calculate individual statistics per sample:
          curstatistics = new Statistics(cursample);
          samplestatisticslist.add(curstatistics);
          cursample = null;
          curstatistics = null;
          curbarcodename = "";
          event = xmlparser.nextTag();

        // concerning Lane:
        } else if (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Lane")) {
          //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
          curlanenumber = Double.parseDouble(xmlparser.getAttributeValue(0));  // Assumption:Lane-attribute number is always numeric
          event = xmlparser.nextTag();
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Lane")) {
          curlanenumber = Double.NaN;
          event = xmlparser.nextTag();

        // concerning Tile with all its sub-elements:
        } else if (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Tile")) {
          //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
          curtilenumber = Double.parseDouble(xmlparser.getAttributeValue(0));  // Assumption: Tile-attribute number is always numeric
          // concerning Raw with Assumption: Raw-element is singlechild:
            xmlparser.nextTag();
            //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName());
            currawitem = new SampleItem();
            currawitem.Type = "Raw";
            currawitem.Lane = curlanenumber;
            currawitem.Tile = curtilenumber;
            xmlparser.nextTag();
            currawitem.ClusterCount = Double.parseDouble(xmlparser.getElementText());  // Assumption: ClusterCount-element is numeric singlechild
            //System.out.println("\nValue: ClusterCount=" + currawitem.ClusterCount);
            xmlparser.nextTag();
            while (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Read")) {  // Assumption: at least or more than 1 Read-element
              //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
              xmlparser.nextTag();
              currawitem.YieldList.add(Double.parseDouble(xmlparser.getElementText()));  // Assumption: Yield-element is numeric singlechild
              xmlparser.nextTag();
              currawitem.YieldQ30List.add(Double.parseDouble(xmlparser.getElementText()));  // Assumption: YieldQ30List-element is numeric singlechild
              xmlparser.nextTag();
              currawitem.QualityScoreSumList.add(Double.parseDouble(xmlparser.getElementText()));  // Assumption: QualityScoreSumList-element is numeric singlechild
              xmlparser.nextTag();
              xmlparser.nextTag();
              //System.out.println("Values in Read: Yield=" + currawitem.YieldList.get(currawitem.YieldList.size()-1) + ", YieldQ30=" + currawitem.YieldQ30List.get(currawitem.YieldQ30List.size()-1) + ", QualityScoreSum=" + currawitem.QualityScoreSumList.get(currawitem.QualityScoreSumList.size()-1));
            }
            //System.out.println("\nRaw-SampleItem  " + currawitem);
            cursample.RawList.add(currawitem);
            currawitem = null;
          // concerning Pf with Assumption that entire Pf-element is structured same as Raw:
            xmlparser.nextTag();
            //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName());
            curpfitem = new SampleItem();
            curpfitem.Type = "Pf";
            curpfitem.Lane = curlanenumber;
            curpfitem.Tile = curtilenumber;
            xmlparser.nextTag();
            curpfitem.ClusterCount = Double.parseDouble(xmlparser.getElementText());
            //System.out.println("\nValue: ClusterCount=" + curpfitem.ClusterCount);
            xmlparser.nextTag();
            while (event == XMLStreamConstants.START_ELEMENT && xmlparser.getLocalName().equals("Read")) {
              //System.out.println("\nStart-Element with tag " + xmlparser.getLocalName() + " with " + xmlparser.getAttributeCount() + " attributes with first attribute: " + xmlparser.getAttributeLocalName(0) +" = " + xmlparser.getAttributeValue(0));
              xmlparser.nextTag();
              curpfitem.YieldList.add(Double.parseDouble(xmlparser.getElementText()));
              xmlparser.nextTag();
              curpfitem.YieldQ30List.add(Double.parseDouble(xmlparser.getElementText()));
              xmlparser.nextTag();
              curpfitem.QualityScoreSumList.add(Double.parseDouble(xmlparser.getElementText()));
              xmlparser.nextTag();
              xmlparser.nextTag();
              //System.out.println("Values in Read: Yield=" + curpfitem.YieldList.get(curpfitem.YieldList.size()-1) + ", YieldQ30=" + curpfitem.YieldQ30List.get(curpfitem.YieldQ30List.size()-1) + ", QualityScoreSum=" + curpfitem.QualityScoreSumList.get(curpfitem.QualityScoreSumList.size()-1));
            }
            //System.out.println("\nPf-SampleItem  " + curpfitem);
            cursample.PfList.add(curpfitem);
            curpfitem = null;
          // attain end of current Tile and afterwards continue in next Tile/Lane/Barcode/Sample/Project:
          event = xmlparser.nextTag();
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Tile")) {
          curtilenumber = Double.NaN;
          event = xmlparser.nextTag();

        //  concerning finish of reading in XML or hit upon error due XML content:
        } else if (event == XMLStreamConstants.END_ELEMENT && xmlparser.getLocalName().equals("Stats")) {
          // this final part of while loop is just for analyzing potential errors, but
          // could be removed and changed in:  while xmlparser.getLocalName() != 'Stats'
          doimport = false;
        } else {
          doimport = false;
          errormessage = "STRANGE ERROR IN METHOD importXMLdata_and_calculateStatistics WHEN READING IN XMLFILE. => CHECK CODE AND XMLFILE-STRUCTURE!";
          //System.out.println(errormessage);
          throw new Exception(errormessage);
        }

      }
      xmlparser.close();

    } catch (FileNotFoundException | XMLStreamException e) {
      System.out.println("OCCURRED EXCEPTION " + e.toString());
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      System.out.println("OCCURRED EXCEPTION " + e.toString());
      e.printStackTrace();
    } catch (Exception e) {  // catch any other exception
      System.out.println("OCCURRED EXCEPTION " + e.toString());
      e.printStackTrace();
    } finally {  // anyway adapt statistics and return output (It would be empty list due to constructor of this object)
      // Statistics 2nd step: adapt single sample's statistics including entire list of samples
      for (Statistics s : samplestatisticslist) {
        s.adaptStatisticsWithRespectToAllSamples(samplestatisticslist);
      }
      return samplestatisticslist;
    }

  }

//##########################################################

  public class SampleItem {
    /*
    Object of an item in sample including
    - the corresponding type Raw or Pf
    - the index of corresponding Lane, Tile
    - measured value of ClusterCount and values in Lists (w.r.t. Read) of Yield, YieldQ30, QualityScoreSum
    */

    public String Type = "";
    // Define unknown numerical value. Type double could be exchanged with int, but double is necessary to initialize NaN-value.
    public double Lane = Double.NaN;
    public double Tile = Double.NaN;
    public double ClusterCount = Double.NaN;
    // Define unknown ArrayList of numerical values with changeable List size. Type Double(object) instead of double(primitive) is necessary in Lists.
    public List<Double> YieldList = new ArrayList<Double>();
    public List<Double> YieldQ30List = new ArrayList<Double>();
    public List<Double> QualityScoreSumList = new ArrayList<Double>();

    public String toString() {
      return "Type: " + this.Type + ", Lane: " + (long)this.Lane + ", Tile: " + (long)this.Tile
             + ", ClusterCount: " + (long)this.ClusterCount + ", YieldList: " + this.YieldList
             + ", YieldQ30List: " + this.YieldQ30List + ", QualityScoreSumList: " + this.QualityScoreSumList;
    }

  }

//##########################################################

  public class Sample {
    /*
    Object of an entire sample including
    - the name of Flowcell, Project, Sample, Barcode
    - the list of Raw and Pf SampleItem-Objects
    */

    public String Flowcell = "";
    public String Project = "";
    public String Sample = "";
    public String Barcode = "";
    // Define unknown ArrayList of SampleItems:
    public List<SampleItem> RawList = new ArrayList<SampleItem>();
    public List<SampleItem> PfList = new ArrayList<SampleItem>();

    public String toString() {
      return "Flowcell: " + this.Flowcell + ", Project: " + this.Project + ", Sample: " + this.Sample + ", Barcode: " + this.Barcode
             + ", RawList: " + this.RawList + ", PfList: " + this.PfList;
    }

  }

//##########################################################

  public class Statistics extends Sample {
    /*
    Object of Statistics within one single sample inherited from Sample-Object
    */

    public double Sum_RawClusterCount = Double.NaN;  // obvious double, but could be turned into int
    public double Sum_PfClusterCount = Double.NaN;  // obvious double, but could be turned into int
    public double Sum_RawYield = Double.NaN;
    public double Sum_PfYield = Double.NaN;
    public double Sum_RawYieldQ30 = Double.NaN;
    public double Sum_PfYieldQ30 = Double.NaN;
    public double Sum_RawQualityScoreSum = Double.NaN;
    public double Sum_PfQualityScoreSum = Double.NaN;
    public double Mega_RawYield = Double.NaN;  // obvious double, but could be turned into int
    public double Mega_PfYield = Double.NaN;  // obvious double, but could be turned into int
    public double Percentage_PfYield_RawYield = Double.NaN;
    public double Percentage_PfYieldQ30_PfYield = Double.NaN;
    public double Fraction_PfQualityScoreSum_PfYield = Double.NaN;
    public double Percentage_PfClusterCount_RawClusterCount = Double.NaN;
    public double Percentage_RawClusterCount_AllRawClusterCounts = Double.NaN;

    public Statistics(Sample sample) {
      /*
      Constructor of derived class
      Initialization: Already initialized Sample-Object is necessary argument.
      */

      super();
      Flowcell = sample.Flowcell;
      Project = sample.Project;
      Sample = sample.Sample;
      Barcode = sample.Barcode;
      RawList = sample.RawList;
      PfList = sample.PfList;

      if (RawList.size()>0)  Sum_RawClusterCount = 0;
      for (SampleItem s : RawList)  Sum_RawClusterCount += s.ClusterCount;
      if (PfList.size()>0)  Sum_PfClusterCount = 0;
      for (SampleItem s : PfList)  Sum_PfClusterCount += s.ClusterCount;
      if (RawList.size()>0)  Sum_RawYield = 0;
      for (SampleItem s : RawList)  for (double d : s.YieldList)  Sum_RawYield += d;
      if (PfList.size()>0)  Sum_PfYield = 0;
      for (SampleItem s : PfList)  for (double d : s.YieldList)  Sum_PfYield += d;
      if (RawList.size()>0)  Sum_RawYieldQ30 = 0;
      for (SampleItem s : RawList)  for (double d : s.YieldQ30List)  Sum_RawYieldQ30 += d;
      if (PfList.size()>0)  Sum_PfYieldQ30 = 0;
      for (SampleItem s : PfList)  for (double d : s.YieldQ30List)  Sum_PfYieldQ30 += d;
      if (RawList.size()>0)  Sum_RawQualityScoreSum = 0;
      for (SampleItem s : RawList)  for (double d : s.QualityScoreSumList)  Sum_RawQualityScoreSum += d;
      if (PfList.size()>0)  Sum_PfQualityScoreSum = 0;
      for (SampleItem s : PfList)  for (double d : s.QualityScoreSumList)  Sum_PfQualityScoreSum += d;
//      Mega_RawYield = calculate_MegaUnit(Sum_RawYield);
//      Mega_PfYield = calculate_MegaUnit(Sum_PfYield);

      Mega_RawYield = Sum_RawYield;
      Mega_PfYield = Sum_PfYield;
              
      Percentage_PfYield_RawYield = calculate_Percentage(Sum_PfYield,Sum_RawYield);
      Percentage_PfYieldQ30_PfYield = calculate_Percentage(Sum_PfYieldQ30,Sum_PfYield);
      Fraction_PfQualityScoreSum_PfYield = calculate_Fraction(Sum_PfQualityScoreSum,Sum_PfYield);
      Percentage_PfClusterCount_RawClusterCount = calculate_Percentage(Sum_PfClusterCount,Sum_RawClusterCount);
      // Calculation of attribute "Percentage_RawClusterCount_AllRawClusterCounts" needs statistics of all other included samples. => After initializing this object, apply method: adaptStatisticsWithRespectToAllSamples(statisticslist)
    }

    public String toString() {
      return "Flowcell: " + this.Flowcell + ", Project: " + this.Project + ", Sample: " + this.Sample + ", Barcode: " + this.Barcode
             + ", Raw Clusters: " + (long)this.Sum_RawClusterCount + ", Mbases Raw Yield: " + this.Mega_RawYield
             + ", % Raw Clusters overall: " + this.Percentage_RawClusterCount_AllRawClusterCounts
             + ", Pf Clusters: " + (long)this.Sum_PfClusterCount + ", Mbases Pf Yield: " + this.Mega_PfYield
             + ", % PfYield/RawYield: " + this.Percentage_PfYield_RawYield
             + ", % PfYieldQ30/PfYield: " + this.Percentage_PfYieldQ30_PfYield
             + ", Mean Pf Quality Score: " + this.Fraction_PfQualityScoreSum_PfYield
             + ", % Passes Filtering: " + this.Percentage_PfClusterCount_RawClusterCount;
    }

    public void adaptStatisticsWithRespectToAllSamples(List<Statistics> statisticslist) {
      /*
      This Statistics-Object (corresponding to one sample) is adapted by employing a list of
      Statistics-Objects corresponding to all samples influencing the statistics of single sample.
      Input: statisticslist contains all Statistics-Objects
      */
      if (statisticslist.size()==0) { // here it additionally should be checked, if calling object is included ...
        Percentage_RawClusterCount_AllRawClusterCounts = Double.NaN;
        String errormessage = "INPUT ARGUMENT statisticslist MUST BE LIST OF Statistics OBJECTS INCLUDING THE CALLING OBJECT IN METHOD adaptStatisticsWithRespectToAllSamples!";
        //System.out.println(errormessage);
        throw new IllegalArgumentException(errormessage); // Exception reasonable since otherwise wrong results.
      } else {
        double sum_allrawclustercounts = 0;
        for (Statistics s : statisticslist)  sum_allrawclustercounts += s.Sum_RawClusterCount;
        Percentage_RawClusterCount_AllRawClusterCounts = calculate_Percentage(Sum_RawClusterCount, sum_allrawclustercounts);
      }
    }

    public double calculate_MegaUnit(double x) {
      double z;
      if (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x)) {
        z = Double.NaN;
        String errormessage = "INPUT ARGUMENT WAS UNREASONABLE IN METHOD calculate_MegaUnit!  x = " + x + "  (Values were in Sample " + Sample + ")";
        //System.out.println(errormessage);
        //throw new IllegalArgumentException(errormessage);
      } else {
        z = x/1000000;
      }
      return z;
    }

    public double calculate_Percentage(double x, double y) {
      double z = x/y;
      if (z == Double.POSITIVE_INFINITY || z == Double.NEGATIVE_INFINITY || Double.isNaN(z)) {
        z = Double.NaN;
        String errormessage = "INPUT ARGUMENT WAS UNREASONABLE IN METHOD calculate_Percentage!  x = " + x + ",  y = " + y + "  (Values were in Sample " + Sample + ")";
        //System.out.println(errormessage);
        //throw new IllegalArgumentException(errormessage);
      } else {
        z = 100*z;
      }
      return z;
    }

    public double calculate_Fraction(double x, double y) {
      double z = x/y;
      if (z == Double.POSITIVE_INFINITY || z == Double.NEGATIVE_INFINITY || Double.isNaN(z)) {
        z = Double.NaN;
        String errormessage = "INPUT ARGUMENT WAS UNREASONABLE IN METHOD calculate_Fraction!  x = " + x + ",  y = " + y + "  (Values were in Sample " + Sample + ")";
        //System.out.println(errormessage);
        //throw new IllegalArgumentException(errormessage);
      }
      return z;
    }

    public double roundspecific(double x, int places) {
      double z;
      if (places < 0 || places > 13 || x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x))  {
        z = Double.NaN;
        String errormessage = "INPUT ARGUMENT WAS UNREASONABLE IN METHOD roundspecific!  x = " + x + ",  places = " + places + "  (Values were in Sample " + Sample + ")";
        //System.out.println(errormessage);
        //throw new IllegalArgumentException(errormessage);
      } else {
        double factor = Math.pow(10, places);
        z = Math.round(x * factor);
        z = z / factor;
      }
      return z;
    }

  }

}