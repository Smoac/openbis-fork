package ch.ethz.sis.rdf.main.xlsx;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.apache.jena.base.Sys;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelImportMessage
{
    private final String message;
    private final int sheet;

    private final int line;

    public ExcelImportMessage(String message, int sheet, int line)
    {
        this.message = message;
        this.sheet = sheet;
        this.line = line;
    }

    public int getLine()
    {
        return line;
    }

    public int getSheet()
    {
        return sheet;
    }

    public String getMessage()
    {
        return message;
    }

    private static final Pattern p = Pattern.compile("^\\d+");



    public static ExcelImportMessage from(UserFailureException userFailureException){
        try
        {
            String a = userFailureException.getMessage().replace("Exception importing data: sheet:", "");
            Matcher m = p.matcher(a);
            if (m.find())
            {
                String line1 = m.group();
            }
            String sheetString = a.split(" ")[1];
            int sheet = Integer.parseInt(sheetString);
            String b = a.replace(sheetString, "").replace("line: ", "").trim();
            String lineString = b.split(" ")[0];

            int line = Integer.parseInt(lineString);
            String c = b.replace(lineString, "").trim().replace("message: ", "");

            return new ExcelImportMessage(c, sheet, line);
        } catch (RuntimeException e){
            System.out.println("Cannot extract excel data from \"" + userFailureException.getMessage() + "\"");
            e.printStackTrace();
            return null;
        }
    }
}
