/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.bsse.cisd.plasmid.plasmapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Uploads provided file to PlasMapper.
 * 
 * @author Piotr Buczek
 */
public class PlasMapperUploader
{
    // http://www.java-tips.org/other-api-tips/httpclient/how-to-use-multipart-post-method-for-uploading.html

    private final static String PLASMAPPER_URL = "http://localhost:8082/PlasMapper";

    private final static String GRAPHIC_MAP_SERVLET_PATH = "/servlet/DrawVectorMap";

    private static final String LIST_SEPARATOR = ",";

    private final static String FILE_PART_NAME = "fastaFile";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PlasMapperUploader.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, PlasMapperUploader.class);

    public static void main(String[] args)
    {
        Properties p = new Properties();
        p.setProperty("vendor", "Amersham%20Pharmacia");
        p.setProperty("Submit", "Graphic Map");
        p.setProperty("showOption", "1,2,3,4,5,6,7,8,9");
        p.setProperty("restriction", "1");
        p.setProperty("orfLen", "200");
        p.setProperty("strand", "1,2");
        p.setProperty("featureName1", "");
        p.setProperty("featureName2", "");
        p.setProperty("featureName3", "");
        p.setProperty("featureName4", "");
        p.setProperty("featureName5", "");
        p.setProperty("featureName6", "");
        p.setProperty("dir1", "1");
        p.setProperty("dir2", "1");
        p.setProperty("dir3", "1");
        p.setProperty("dir4", "1");
        p.setProperty("dir5", "1");
        p.setProperty("dir6", "1");
        p.setProperty("category1", "origin_of_replication");
        p.setProperty("category2", "origin_of_replication");
        p.setProperty("category3", "origin_of_replication");
        p.setProperty("category4", "origin_of_replication");
        p.setProperty("category5", "origin_of_replication");
        p.setProperty("category6", "origin_of_replication");
        p.setProperty("stop1", "");
        p.setProperty("stop2", "");
        p.setProperty("stop3", "");
        p.setProperty("stop4", "");
        p.setProperty("stop5", "");
        p.setProperty("stop6", "");
        p.setProperty("scheme", "0");
        p.setProperty("shading", "0");
        p.setProperty("labColor", "0");
        p.setProperty("labelBox", "1");
        p.setProperty("labels", "0");
        p.setProperty("innerLabels", "0");
        p.setProperty("legend", "0");
        p.setProperty("arrow", "0");
        p.setProperty("tickMark", "0");
        p.setProperty("mapTitle", "");
        p.setProperty("comment", "Created using PlasMapper");
        p.setProperty("imageFormat", "PNG");
        p.setProperty("imageSize", "900 x 900");
        p.setProperty("backbone", "medium");
        p.setProperty("arc", "medium");
        p.setProperty("biomoby", "true"); // special: result of request == relative path to PNG file

        PlasMapperUploader uploader = new PlasMapperUploader(p);
        uploader.upload(new File("PRS316.gb"));
    }

    private Properties properties;

    public PlasMapperUploader(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * Makes an HTTP multipart POST request with given file.
     * 
     * @return path to output image or null if upload failed / the server's response to the request
     */
    public String upload(File gbFile)
    {
        assert gbFile.getName().toLowerCase().endsWith(".gb");
        assert gbFile.exists();

        final PostMethod post = new PostMethod(getServiceURL());
        try
        {
            Part filePart = new FilePart(FILE_PART_NAME, gbFile);
            Part[] parts = createParts(filePart, properties);
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
            HttpClient client = new HttpClient();
            int statusCode = client.executeMethod(post);
            if (statusCode != HttpStatus.SC_OK)
            {
                notificationLog.error(String.format("Multipart POST failed: "
                        + post.getStatusLine()));
                throw new IOExceptionUnchecked(new IOException("Multipart POST failed: "
                        + post.getStatusLine()));
            }
            String response = post.getResponseBodyAsString();
            operationLog.info(String.format("Response of service: '%s'", response));
            return response;
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            post.releaseConnection();
        }
    }

    /**
     * Creates parts of multipart request containing all given properties and <code>filePart</code>.
     */
    @SuppressWarnings("unchecked")
    private static Part[] createParts(Part filePart, Properties properties)
    {
        assert filePart != null : "Unspecified filePart";
        assert properties != null : "Unspecified properties";

        final List<Part> parts = new ArrayList<Part>();
        parts.add(filePart);
        for (final Enumeration<String> enumeration =
                (Enumeration<String>) properties.propertyNames(); enumeration.hasMoreElements(); /**/)
        {
            final String key = enumeration.nextElement();
            String[] values = properties.getProperty(key).split(LIST_SEPARATOR);
            for (String value : values)
            {
                parts.add(new StringPart(key, StringUtils.trim(value)));
            }
        }
        return parts.toArray(new Part[0]);
    }

    private String getServiceURL()
    {
        return PLASMAPPER_URL + GRAPHIC_MAP_SERVLET_PATH;
    }

}
