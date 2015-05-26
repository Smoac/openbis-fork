package ch.ethz.ssdm.eln;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;

public class PlasmapperConnector
{
    public static void main(String[] args) throws Exception {
        //URL to the server without the dash '/'
        String serverURL = "http://wishart.biology.ualberta.ca";
        //Path to input fasta file
        String fastaInputFilePath = "FRP1955.fasta";
        //Path to output svg file
        String svgOutputFilePath = "FRP1955.svg";
        //Path to output html file
        String htmlOutputFilePath = "FRP1955.html";
        
        downloadPlasmidMap(
                serverURL,
                fastaInputFilePath,
                svgOutputFilePath,
                htmlOutputFilePath);
    }
    
    
    private static final String VECTOR_MAP_URL = "/PlasMapper/servlet/DrawVectorMap";
    
    public static void downloadPlasmidMap(
            final String serverURL,
            final String fastaInputFilePath,
            final String svgOutputFilePath,
            final String htmlOutputFilePath) {
        
//        System.out.println("Asking service " + serverURL + VECTOR_MAP_URL + " to generate the SVG file from " + fastaInputFile);
        
        PostMethod method = new PostMethod(serverURL + VECTOR_MAP_URL);
        
        try {
            Part[] parts = getPlasmidFormOptions(fastaInputFilePath);
            
            MultipartRequestEntity requestEntity = new MultipartRequestEntity(parts, method.getParams());
            method.setRequestEntity(requestEntity);
            HttpClient client = new HttpClient();
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK)
            {
                throw new RuntimeException("Status Code was " + statusCode + " instead of " + HttpStatus.SC_OK);
            }
            
            String svgFileURLPath = method.getResponseBodyAsString();
            if (svgFileURLPath == null || !svgFileURLPath.contains("PlasMapper")) {
                throw new RuntimeException("PlasMapper service failed returning incorrect path to file: " + svgFileURLPath);
            } else if (svgFileURLPath.endsWith("\r\n"))
            {
                svgFileURLPath = svgFileURLPath.substring(0, svgFileURLPath.lastIndexOf("\r\n"));
            } else if (svgFileURLPath.endsWith("\n"))
            {
                svgFileURLPath = svgFileURLPath.substring(0, svgFileURLPath.lastIndexOf("\n"));
            }
//            System.out.println("Downloading SVG file: " + serverURL + "/" + svgFileURLPath + " to " + svgOutputFile);
            URL svgFileURL = new URL(serverURL + "/" + svgFileURLPath);
            
            File svgOutputFile = new File(svgOutputFilePath);
            FileUtils.copyURLToFile(svgFileURL, svgOutputFile);
//            System.out.println("Generating HTML file: " + htmlOutputFile);
            String htmlFileContents = "<html>"
                                    + "<head>"
                                    + "<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\">"
                                    + "<title>PlasMapper - Graphic Map</title>"
                                    + "</head>"
                                    + "<body>"
                                    + "<embed src=\"" + svgOutputFile.getName() + "\" type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\" id=\"Panel\" height=\"1010\" width=\"1010\">"
                                    + "<br>"
                                    + "<a href=\"" + svgOutputFile.getName() + "\" target=\"_blank\">Download Link</a>"
                                    + "</body>"
                                    + "</html>";
            FileUtils.writeStringToFile(new File(htmlOutputFilePath), htmlFileContents, "UTF-8");
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            method.releaseConnection();
        }
    }
    
    
    private static Part[] getPlasmidFormOptions(final String fastaInputFile) throws FileNotFoundException
    {
        File fasta = new File(fastaInputFile);
        List<Part> parts = new ArrayList<Part>();
        parts.add(new FilePart("fastaFile", fasta));
        parts.add(new StringPart("vendor", "Amersham%20Pharmacia"));
        parts.add(new StringPart("showOption", "1"));
        parts.add(new StringPart("showOption", "2"));
        parts.add(new StringPart("showOption", "3"));
        parts.add(new StringPart("showOption", "4"));
        parts.add(new StringPart("showOption", "5"));
        parts.add(new StringPart("showOption", "6"));
        parts.add(new StringPart("showOption", "7"));
        parts.add(new StringPart("showOption", "8"));
        parts.add(new StringPart("showOption", "9"));
        parts.add(new StringPart("restriction", "1"));
        parts.add(new StringPart("orfLen", "200"));
        parts.add(new StringPart("strand", "1"));
        parts.add(new StringPart("strand", "2"));
        parts.add(new StringPart("dir1", "1"));
        parts.add(new StringPart("dir2", "1"));
        parts.add(new StringPart("dir3", "1"));
        parts.add(new StringPart("dir4", "1"));
        parts.add(new StringPart("dir5", "1"));
        parts.add(new StringPart("dir6", "1"));
        parts.add(new StringPart("category1", "origin_of_replication"));
        parts.add(new StringPart("category2", "origin_of_replication"));
        parts.add(new StringPart("category3", "origin_of_replication"));
        parts.add(new StringPart("category4", "origin_of_replication"));
        parts.add(new StringPart("category5", "origin_of_replication"));
        parts.add(new StringPart("category6", "origin_of_replication"));
        parts.add(new StringPart("scheme", "0"));
        parts.add(new StringPart("shading", "0"));
        parts.add(new StringPart("labColor", "0"));
        parts.add(new StringPart("labelBox", "1"));
        parts.add(new StringPart("labels", "0"));
        parts.add(new StringPart("innerLabels", "0"));
        parts.add(new StringPart("legend", "0"));
        parts.add(new StringPart("arrow", "0"));
        parts.add(new StringPart("tickMark", "0"));
        parts.add(new StringPart("mapTitle", ""));
        parts.add(new StringPart("comment", "Created using PlasMapper"));
        parts.add(new StringPart("imageFormat", "SVG"));
        parts.add(new StringPart("imageSize", "1000 x 1000"));
        parts.add(new StringPart("backbone", "medium"));
        parts.add(new StringPart("arc", "medium"));
        parts.add(new StringPart("biomoby", "true"));
        return parts.toArray(new Part[0]);
    }
}
