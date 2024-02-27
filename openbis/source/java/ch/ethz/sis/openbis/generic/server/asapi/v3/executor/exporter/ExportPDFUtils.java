package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.exporter;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExportPDFUtils
{

    static final Pattern hslColorPattern = Pattern.compile("color:hsl\\(.*\\);");
    static final Pattern hslBackgroundColorPattern = Pattern.compile("background-color:hsl\\(.*\\);");

    /*
     * This algorithm to replace HSL to Hex colors has the benefit of having a complexity of O(n)
     * It only transverses the source and destination strings once without creating additional copies
     */
    public static String replaceHSLToHex(String html, String cssProperty, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        StringBuilder builder = null;
        while (matcher.find()) {
            if (builder == null) {
                builder = new StringBuilder(html);
            }
            String[] hslParts = html.substring(matcher.start()+10, matcher.end()-2).replace("%", "").split(",");
            String hex = hslToHex(Float.parseFloat(hslParts[0])/360, Float.parseFloat(hslParts[1])/100, Float.parseFloat(hslParts[2])/100);
            String hexColor = cssProperty + ": " + hex + ";";
            int offset = html.length() - builder.length();
            builder.replace((matcher.start() - offset), (matcher.end() - offset), hexColor);
        }
        if (builder != null) {
            html = builder.toString();
        }
        return html;
    }

    public static String hslToHex(double hue, double saturation, double lightness) {
        // Convert HSL to RGB
        int rgb = Color.HSBtoRGB((float) hue, (float) saturation, (float) lightness);

        // Get the RGB components
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // Convert RGB to Hex
        String hex = String.format("#%02X%02X%02X", red, green, blue);

        return hex;
    }

    static String styleCSS = null;
    public static String addStyleHeader(String replacedHtml) throws IOException
    {
        if (styleCSS == null) {
            InputStream is = ExportPDFUtils.class.getResourceAsStream("ck-editor-styles.css");
            styleCSS = new String(readInputStream(is));
        }

        return replacedHtml.replace("<head></head>", "<head><style>" + styleCSS + "</style></head>");
    }

    public static byte[] readInputStream(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // or any other buffer size you prefer
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        return outputStream.toByteArray();
    }

    public static String insertPagePagebreak(String html, String before) {
        return html.replace(before, "<div class=\"pagebreak\"> </div>" + before);
    }
}
