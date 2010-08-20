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
 * Uploads provided sequence file to PlasMapper.
 * 
 * @author Piotr Buczek
 */
public class PlasMapperUploader
{

    private final static String DEFAULT_PLASMAPPER_URL = "http://localhost:8082/PlasMapper";

    private static Properties createDefaultProperties()
    {
        Properties result = new Properties();
        result.setProperty("vendor", "Amersham%20Pharmacia");
        result.setProperty("showOption", "1,2,3,4,5,6,7,8,9");
        result.setProperty("restriction", "1");
        result.setProperty("orfLen", "200");
        result.setProperty("strand", "1,2");
        result.setProperty("featureName1", "");
        result.setProperty("featureName2", "");
        result.setProperty("featureName3", "");
        result.setProperty("featureName4", "");
        result.setProperty("featureName5", "");
        result.setProperty("featureName6", "");
        result.setProperty("dir1", "1");
        result.setProperty("dir2", "1");
        result.setProperty("dir3", "1");
        result.setProperty("dir4", "1");
        result.setProperty("dir5", "1");
        result.setProperty("dir6", "1");
        result.setProperty("category1", "origin_of_replication");
        result.setProperty("category2", "origin_of_replication");
        result.setProperty("category3", "origin_of_replication");
        result.setProperty("category4", "origin_of_replication");
        result.setProperty("category5", "origin_of_replication");
        result.setProperty("category6", "origin_of_replication");
        result.setProperty("stop1", "");
        result.setProperty("stop2", "");
        result.setProperty("stop3", "");
        result.setProperty("stop4", "");
        result.setProperty("stop5", "");
        result.setProperty("stop6", "");
        result.setProperty("scheme", "0");
        result.setProperty("shading", "0");
        result.setProperty("labColor", "0");
        result.setProperty("labelBox", "1");
        result.setProperty("labels", "0");
        result.setProperty("innerLabels", "0");
        result.setProperty("legend", "0");
        result.setProperty("arrow", "0");
        result.setProperty("tickMark", "0");
        result.setProperty("mapTitle", "");
        result.setProperty("comment", "Created using PlasMapper");
        result.setProperty("imageFormat", "PNG");
        result.setProperty("imageSize", "900 x 900");
        result.setProperty("backbone", "medium");
        result.setProperty("arc", "medium");
        // special: result of request == relative path to the PNG/GB file
        result.setProperty("biomoby", "true");
        return result;
    }

    public enum PlasMapperService
    {

        GRAPHIC_MAP("/servlet/DrawVectorMap"), GENEBANK_OUTPUT("/servlet/GenbankOutput");

        private final String servletPath;

        PlasMapperService(String servletPath)
        {
            this.servletPath = servletPath;
        }

        String getServletPath()
        {
            return servletPath;
        }

    }

    private static final String LIST_SEPARATOR = ",";

    private final static String FILE_PART_NAME = "fastaFile";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PlasMapperUploader.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, PlasMapperUploader.class);

    public static void main(String[] args)
    {
        final Properties p = createDefaultProperties();
        final PlasMapperUploader uploader = new PlasMapperUploader(DEFAULT_PLASMAPPER_URL, p);
        final File seqFile = new File("PRS316.gb");
        for (PlasMapperService service : PlasMapperService.values())
        {
            String response = uploader.upload(seqFile, service);
            System.out.println(String.format("Response of %s service: '%s'", service, response));
        }
    }

    private final String baseUrl;

    private final Properties properties;

    public PlasMapperUploader(String baseUrl, Properties properties)
    {
        this.baseUrl = baseUrl;
        this.properties = properties;
    }

    public PlasMapperUploader(String baseUrl)
    {
        this(baseUrl, createDefaultProperties());
    }

    /**
     * Makes an HTTP multipart POST request with given file.
     * 
     * @param seqFile file to be uploaded
     * @param service service to be used for upload
     * @return the server's response to the request depending on the service (path to output image
     *         or sequence in genebank format)
     */
    public String upload(File seqFile, PlasMapperService service)
    {
        final PostMethod post = new PostMethod(baseUrl + service.getServletPath());
        try
        {
            Part filePart = new FilePart(FILE_PART_NAME, seqFile);
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
            if (response.endsWith("\n"))
            {
                response = response.substring(0, response.lastIndexOf("\n"));
            }
            operationLog.info(String.format("Response of %s service: '%s'", service, response));
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

}
