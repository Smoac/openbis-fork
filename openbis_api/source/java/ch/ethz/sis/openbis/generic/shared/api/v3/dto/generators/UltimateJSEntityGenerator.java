package ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UltimateJSEntityGenerator
{
    private static final String API_PROJECT_SOURCE_FOLDER = "/Users/juanf/Documents/workspace/openbis_api/source/java/";
    private static final String[] CLASSES_TO_CONVERT = new String[]{
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentTypeFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleTypeFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag",
        "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions"
    };
    
    private static final String JSTEST_PROJECT_SOURCE_FOLDER = "/Users/juanf/Documents/workspace/js-test/servers/common/core-plugins/tests/1/as/webapps/openbis-v3-api-test/html/dto/";
    
    public static void main(String[] args) throws IOException {
        for (String classToConvert:CLASSES_TO_CONVERT) {
            String jsClass = translateFromJavaToJS(readFileAsString(API_PROJECT_SOURCE_FOLDER + classToConvert.replace('.', '/') + ".java"));
            System.out.println(jsClass);
        }
    }
    
    private static final String translateFromJavaToJS(String toTranslate) {
        return toTranslate
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
    }
    
    private static final String readFileAsString(String filePath) throws IOException {
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
