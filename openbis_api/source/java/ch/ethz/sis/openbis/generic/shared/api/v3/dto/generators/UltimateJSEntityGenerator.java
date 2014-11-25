package ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UltimateJSEntityGenerator
{
    public static void main(String[] args) throws IOException {
        String toTranslate = readFileAsString("/Users/juanf/Documents/workspace/openbis_api/source/java/ch/ethz/sis/openbis/generic/shared/api/v3/dto/entity/attachment/Attachment.java");

        toTranslate = toTranslate
            //Remove Java specific features
            .replaceAll("package.+;", "")
            .replaceAll("import.+;", "")
            .replaceAll("@JsonObject.+\\)", "")
            .replaceAll("@JsonProperty", "")
            .replaceAll("@JsonIgnore", "")
            .replaceAll("@Override", "")
            .replaceAll("private static final long serialVersionUID.+;", "")
            //Translate private parameters to var
            .replaceAll("private .+ ", "var _")
            .replaceAll("this.", "_")
            //Translate Class to function
            .replaceAll("public class ([\\w\\[\\]]+).*", "function $1()")
            //Translate methods
            .replaceAll("public [\\w\\[\\]]+ ", "function ")
            //Remove function parameter types
            .replaceAll("\\([\\w\\[\\]]+ ", "(")
            //Translate Exceptions
            .replaceAll("throw new .+\\(\"(.+)\"\\);", "throw '$1';")
            //Remove Comments
            .replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
        
        System.out.println(toTranslate);
    }
    
    private static String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}
