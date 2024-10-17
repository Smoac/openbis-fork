package ch.ethz.sis.rdf.main.xlsx;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

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

    private static final Pattern PATTERN_LINE_NUMBER = Pattern.compile("line: \\d+");
    private static final Pattern PATTERN_SHEET_NUMBER = Pattern.compile("sheet: \\d+");



    public static ExcelImportMessage from(UserFailureException userFailureException){
        try
        {
            Matcher matcherLine = PATTERN_LINE_NUMBER.matcher(userFailureException.getMessage());
            Matcher matcherSheet = PATTERN_SHEET_NUMBER.matcher(userFailureException.getMessage());
            if (!matcherLine.find()){
                return null;
            }
            if (!matcherSheet.find()){
                return null;
            }

            Integer line = Integer.parseInt(matcherLine.group().replace("line: ", "")) ;
            Integer sheet = Integer.parseInt(matcherSheet.group().replace("sheet: ", "")) ;

            String c = userFailureException.getMessage().split("message: ")[1];

            return new ExcelImportMessage(c, sheet, line);
        } catch (RuntimeException e){
            System.out.println("Cannot extract excel data from \"" + userFailureException.getMessage() + "\"");
            e.printStackTrace();
            return null;
        }
    }
}
