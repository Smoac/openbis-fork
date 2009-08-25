import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 *
 * @author walshs
 */


// to add
// 1. ignore blank lines - unresolved problem
// 4. list DataSet properties

// use log4j
// use args4j


class Tsv2upload
{

    public static void main(String args[])
    {

        
        File d = new File(".");
        try {
            String pwd = d.getCanonicalPath();
            System.out.println("Working dir : " + pwd);
        } catch (IOException ioe) {
            
           System.err.println(ioe.getMessage());
        }
            
            
        TsvUploader up = null;

        try
        {

            up = getUploader(args);
            up.writeDataForDssUpload();
            up.writeSamplesFile();
            up.writeServiceDotProperties();
            up.writeDssMkdir();

        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            System.out.println("Usage follows.....\n\n");
            printUsage();
        }
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

    public static TsvUploader getUploader(String args[]) throws Exception
    {

        Integer head = null;
        String file = null;
        String tag = null;
        ArrayList<Integer> igal = new ArrayList<Integer>(); // ignore ArrayList
        ArrayList<Integer> kcal = new ArrayList<Integer>(); // key columns ArrayList

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

            } else if (s.matches("tag=\\w+"))
            {
                tag = stripValue(s);
            }
            else if (s.matches("keyCols=[\\d|,]+"))
            {

                String[] kcs = (stripValue(s)).split(",");

                for (String i : kcs)
                {
                    kcal.add(Integer.parseInt(i));
                }

            } else
            {
                // Exception for unrecognised arguments
                throw new Exception("Can't parse args");
            }
        }

        if (head == null)
        {
            throw new Exception("<head> not set");
        }
        if (file == null)
        {
            throw new Exception("<file> not set");
        }
        if (tag == null)
        {
            throw new Exception("<tag> not set");
        }

        TsvUploader up = new TsvUploader(head, igal, file, kcal, tag);

        return up;
    }

    public static void printUsage()
    {

        System.out
                .println("------------------------------------------------------------------------------------------------------------------------------------\n"
                        + "Description : Program to convert TSV file into a file and directory struture suitable for upload by OpenBIS Data Store Server (DSS).\n"
                        + "              It also writes DSS upload thread configuration suitable for this TSV for inclusion in service-properties\n\n"
                        + "Usage :\n\n"
                        + "\te.g. Tsv2upload head=1 ignore=2,3 keyCols=2,4,5 file=/tmp/my.tsv tag=PCE_SHARED\n\n"
                        + "<head> is the row to use for data-set-property. i.e. 1 means the first row\n"
                        + "<keyCols> is a comma separated list of columns from which construct the OpenBIS sampe key\n"
                        + "<file> file is the name of the TSV file \n\n"
                        + "<tag> a user defined tag which is used as part of the path for output files"
                        + "All of <head>,<ignore>,<file> are mandatory arguments.\n"
                        + "<ignore> can be set to nothing (i.e. ignore='') in the case that all lines are either a heading or data to be processed\n");

    }

}
