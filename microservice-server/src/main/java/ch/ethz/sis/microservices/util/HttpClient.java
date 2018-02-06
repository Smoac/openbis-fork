package ch.ethz.sis.microservices.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

public class HttpClient
{
    public static byte[] doGet(String urlAsString, Map<String, String> parameters) throws Exception
    {
        StringBuilder parametersAsString = new StringBuilder();
        boolean first = true;
        parametersAsString.append("?");
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            if (first)
            {
                first = false;
            } else
            {
                parametersAsString.append("&");
            }
            parametersAsString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            parametersAsString.append("=");
            parametersAsString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        URL url = new URL(urlAsString + parametersAsString);
        URLConnection con = url.openConnection();
        con.setUseCaches(false);

        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("GET");
        http.setDoOutput(true);

        // OutputStream os = con.getOutputStream();
        // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        // writer.write(parametersAsString.toString());
        // writer.flush();
        // writer.close();

        http.connect();

        int responseCode = http.getResponseCode();

        byte[] response = null;
        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            response = getBytesFromInputStream(http.getInputStream());
        } else
        {
            throw new RuntimeException("Response Code: " + responseCode);
        }
        return response;
    }

    private static byte[] getBytesFromInputStream(InputStream is) throws IOException
    {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[0xFFF];

            int len;
            while ((len = is.read(buffer)) != -1)
            {
                os.write(buffer, 0, len);
            }

            os.flush();

            return os.toByteArray();
        }
    }
}
