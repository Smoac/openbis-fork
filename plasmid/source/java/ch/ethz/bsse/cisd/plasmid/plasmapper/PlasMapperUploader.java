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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Uploads provided sequence file to PlasMapper.
 * 
 * @author Piotr Buczek
 */
public class PlasMapperUploader
{

    private static final String CRLF = "\r\n";

    private static final String BOUNDARY = "MMMMM___MP_BOUNDARY___MMMMM";

    private final static String DEFAULT_PLASMAPPER_URL = "http://wishart.biology.ualberta.ca/PlasMapper";

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
        result.setProperty("imageFormat", "SVG");
        result.setProperty("imageSize", "1000 x 1000");
        result.setProperty("backbone", "medium");
        result.setProperty("arc", "medium");
        // special: result of request == relative path to the SVG/GB file
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

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PlasMapperUploader.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            PlasMapperUploader.class);

    public static void main(String[] args)
    {
        final Properties p = createDefaultProperties();
        final PlasMapperUploader uploader = new PlasMapperUploader(DEFAULT_PLASMAPPER_URL, p);
        final File seqFile = new File("source/core-plugins/eln-lims/1/dss/reporting-plugins/newbrowserapi/lib/plasmapper-source/FRP1955.fasta");
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
    // Synchronization is needed because PlasMapper servlet is not thread safe (see LMS-2086)
    public synchronized String upload(File seqFile, PlasMapperService service)
    {
        String url = baseUrl + service.getServletPath();
        try
        {
            HttpClient httpClient = JettyHttpClientFactory.getHttpClient();
            Request request = httpClient.newRequest(url).method("POST");
            for (Enumeration<?> enumeration = properties.propertyNames(); enumeration.hasMoreElements();)
            {
                final String key = (String) enumeration.nextElement();
                String[] values = properties.getProperty(key).split(LIST_SEPARATOR);
                for (String value : values)
                {
                    request.param(key, StringUtils.trim(value));
                }
            }
            String fileContent = FileUtilities.loadToString(seqFile);
            ContentProvider content = new StringContentProvider("--" + BOUNDARY + CRLF
                    + "Content-Disposition: form-data; name=\"" + FILE_PART_NAME + "\"; filename=\"" 
                    + seqFile.getName() + "\"" + CRLF
                    + "Content-Type: application/octet-stream" + CRLF + CRLF
                    + fileContent + CRLF + "--" + BOUNDARY + "--" + CRLF);
            request.content(content, "multipart/form-data; boundary=" + BOUNDARY);
            ContentResponse contentResponse = request.send();
            String responseAsString = contentResponse.getContentAsString();
            int status = contentResponse.getStatus();
            if (status != HttpStatus.Code.OK.getCode())
            {
                notificationLog.error("Multipart POST failed: " + status + " " + responseAsString);
                throw new IOExceptionUnchecked(new IOException("Multipart POST failed: "
                        + status));
            }
            operationLog.info(String.format("Response of %s service: '%s'", service, responseAsString));
            return responseAsString;
        } catch (final Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

}
