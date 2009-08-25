import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author walshs
 */

// to add
// - ignore blank lines - unresolved problem
// - list DataSet properties
// - use log4j
// - use args4j
// -
//

class Tsv2openBIS
{

    public static void main(String args[])
    {

        File d = new File(".");
        try
        {
            String pwd = d.getCanonicalPath();
            System.out.println("Working dir : " + pwd);
        } catch (IOException ioe)
        {

            System.err.println(ioe.getMessage());
        }

        TsvMangler mangler = null;

        try
        {

            mangler = getTsvMangler(args);
            mangler.writeDataForDssUploadOneDirPerRowStyle();
          
            mangler.writeSamplesFile();
            mangler.writeServiceDotProperties();
            mangler.writeDssMkdir();
            mangler.writeSamplePropertyLoader();
            mangler.writeDataSetPropertyLoader();

        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            System.out.println("Usage follows.....\n\n");
            printUsage();
        }
        System.out.println("Done.\n");
    }

    public static void printArgs(String args[])
    {
        for (String s : args)
        {
            System.out.println(s);
        }
    }

    public static String stripValue(String s)
    {
        String[] s1 = s.split("=");
        // System.out.println("value is " + s1[1]);
        return s1[1];
    }

    public static TsvMangler getTsvMangler(String args[]) throws Exception
    {

        Integer head = null;
        String file = null;
        String dsn = null;
        String expt = null;
        String project = null;
        ArrayList<Integer> igal = new ArrayList<Integer>(); // ignore ArrayList
        ArrayList<Integer> kcal = new ArrayList<Integer>(); // key columns ArrayList
        HashMap<Integer, String> dspts = new HashMap<Integer, String>(); // DataSet Property Types
        HashMap<Integer, String> spts = new HashMap<Integer, String>(); // Sample Property Types

        for (String s : args)
        {
            if (s.matches("head=\\d{1}"))
            {
                head = Integer.parseInt(stripValue(s));

            } else if (s.matches("ignore=[\\d|,]*"))
            {
                String[] igs = (stripValue(s)).split(",");

                for (String i : igs)
                {
                    igal.add(Integer.parseInt(i));
                }

            } else if (s.matches("file=.*"))
            {
                file = stripValue(s);

            } else if (s.matches("project=.*"))
            {
                project = stripValue(s);
            }  
            else if (s.matches("dataSetName=.*"))
            {
                dsn = stripValue(s);

            } else if (s.matches("expt=\\w+"))
            {
                expt = stripValue(s);
            } else if (s.matches("keyCols=[\\d|,]+"))
            {

                String[] kcs = (stripValue(s)).split(",");

                for (String i : kcs)
                {
                    kcal.add(Integer.parseInt(i));
                }

            } else if (s.matches("dataSetPropCols=\\S+"))
            {

                String[] foo = (stripValue(s)).split(",");

                for (String i : foo)
                {
                    String[] bits = i.split(":");
                    dspts.put(Integer.parseInt(bits[0]), bits[1]);
                }
                // System.out.println(dspts.toString());
            }
            else if (s.matches("samplePropCols=\\S+"))
            {

                String[] foo = (stripValue(s)).split(",");

                for (String i : foo)
                {
                    String[] bits = i.split(":");
                    spts.put(Integer.parseInt(bits[0]), bits[1]);
                }
                // System.out.println(dspts.toString());
            }
            else
            {
                // Exception for unrecognised arguments
                throw new Exception("Can't parse args");
            }
        }

        if (head == null)
        {
            throw new Exception("<head> not set");
        }
        if (dsn == null)
        {
            throw new Exception("<dataSetName> not set");
        }
        if (file == null)
        {
            throw new Exception("<file> not set");
        }
        if (expt == null)
        {
            throw new Exception("<expt> not set");
        }
        if (project == null)
        {
            throw new Exception("<project> not set");
        }

        TsvMangler mangler = new TsvMangler(head, igal, file, kcal, expt, dspts, dsn, spts, project);

        return mangler;
    }

    public static void printUsage()
    {

        System.out
                .println("------------------------------------------------------------------------------------------------------------------------------------\n"
                        + "Description : Program to convert TSV file into a file and directory struture suitable for upload by OpenBIS Data Store Server (DSS).\n"
                        + "              It also writes DSS upload thread configuration suitable for this TSV for inclusion in service-properties\n"
                        + "\n"
                        + "Usage :\n"
                        + "\n"
                        + "\te.g. Tsv2openBIS head=1 ignore=2,3 keyCols=2,4,5 project=AGRON-OMICS samplePropCols=4:USER.AGRON-OMICS_ECOTYPE dataSetName=PHENOTYPE_FEATURE_SET dataSetPropCols=1:USER.PROTOCOL,5:USER.SOWING_DATE file=/tmp/my.tsv expt=PILOT_COMAPARTIVE_EXPERIMENT\n"
                        + "\n"
                        + "<head> is the row to use for results.txt headings. i.e. 1 means the first row\n"
                        + "\n"
                        + "<keyCols> is a comma separated list of columns from which construct the OpenBIS sampe key\n"
                        + "\n"
                        + "<file> file is the name of the TSV file \n"
                        + "\n"
                        + "<expt> A string representing the experiment which is only used as part of the path for output files at the top level\n"
                        + "\n"
                        + "<project> A string representing the project. It is used as part of the sample prefix i.e. CISD:/<project>/SAMPLE_1\n"
                        + "\n"
                        + "All of <head>,<ignore>,<file>,<expt>,<project> are mandatory arguments. The following list of arguments are optional :\n"
                        + "\n"
                        + "<ignore> can be set to nothing (i.e. ignore='') in the case that all lines are either a heading or data to be processed\n"
                        + "\n"
                        + "<dataSetPropCols> is a comma separated list of key-value pairs which give configuration to write DataSet Property Types\n"
                        + "                  e.g. 5:USER.SOWING_DATE means use column number 5 values for the USER.SOWING_DATE DataSet Property\n"
                        + "                  clearly, column number 1 means the first column in the spreadsheet\n"
                        + ""
                        + "<samplePropCols> is a comma separated list of key-value pairs which give configuration to write Sample Property Types\n"
                        + "                  e.g. 4:USER.AGRON-OMICS_ECOTYPE means use column number 4 values for the USER.AGRON-OMICS_ECOTYPE DataSet Property\n"
                        + "                  clearly, column number 1 means the first column in the spreadsheet\n");
    }

}
