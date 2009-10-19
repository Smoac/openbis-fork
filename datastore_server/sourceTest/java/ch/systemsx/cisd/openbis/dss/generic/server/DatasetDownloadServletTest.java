/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;
import static org.testng.AssertJUnit.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;

/**
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServletTest
{
    private static final String REQUEST_URI_PREFIX =
            "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/";

    private static final String EXPIRATION_MESSAGE =
            "<html><body>Download session expired.</body></html>";

    private static final String LOGGER_NAME = "OPERATION.DatasetDownloadServlet";

    private static final String LOG_INFO = "INFO  " + LOGGER_NAME + " - ";

    private static final String LOG_ERROR = "ERROR " + LOGGER_NAME + " - ";

    private static final File TEST_FOLDER = new File("targets/unit-test/store");

    private static final String EXAMPLE_DATA_SET_FOLDER_NAME = "data set #123";

    private static final File EXAMPLE_DATA_SET_FOLDER =
            new File(TEST_FOLDER, EXAMPLE_DATA_SET_FOLDER_NAME);

    private static final String EXAMPLE_FILE_NAME = "read me @home.txt";

    private static final File EXAMPLE_FILE = new File(EXAMPLE_DATA_SET_FOLDER, EXAMPLE_FILE_NAME);

    private static final String EXAMPLE_FILE_CONTENT = "Hello world!";

    private static final String EXAMPLE_DATA_SET_SUB_FOLDER_NAME = "+ s % ! # @";

    private static final String ESCAPED_EXAMPLE_DATA_SET_SUB_FOLDER_NAME =
            encode(EXAMPLE_DATA_SET_SUB_FOLDER_NAME);

    private static final File EXAMPLE_DATA_SET_SUB_FOLDER =
            new File(EXAMPLE_DATA_SET_FOLDER, EXAMPLE_DATA_SET_SUB_FOLDER_NAME);

    private static final String EXAMPLE_SESSION_ID = "AV76CF";

    private static final String EXAMPLE_DATA_SET_CODE = "1234-1";

    private static final String SAMPLE_CODE = "SAMPLE-S";

    private static final String EXPERIMENT_CODE = "EPERIMENT-E";

    private static final String GROUP_CODE = "GROUP-G";

    private static final String PROJECT_CODE = "PROJECT-P";

    private static String encode(String url)
    {
        try
        {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private IEncapsulatedOpenBISService dataSetService;

    private HttpSession httpSession;

    @BeforeMethod
    public void setUp()
    {
        System.setProperty("java.awt.headless", "true");
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        dataSetService = context.mock(IEncapsulatedOpenBISService.class);
        httpSession = context.mock(HttpSession.class);
        TEST_FOLDER.mkdirs();
        EXAMPLE_DATA_SET_FOLDER.mkdir();
        FileUtilities.writeToFile(EXAMPLE_FILE, EXAMPLE_FILE_CONTENT);
        EXAMPLE_DATA_SET_SUB_FOLDER.mkdir();
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        FileUtilities.deleteRecursively(TEST_FOLDER);
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetMimetype()
    {
        assertEquals("image/tiff", DatasetDownloadServlet.getMimeType(new File("/some/image.tiff"), false));
        assertEquals("binary", DatasetDownloadServlet.getMimeType(new File("/some/image.tiff"), true));
        assertEquals("image/tiff", DatasetDownloadServlet.getMimeType(new File("/some/image.TIF"), false));
        assertEquals("binary", DatasetDownloadServlet.getMimeType(new File("/some/image.TIF"), true));
        assertEquals("application/pdf", DatasetDownloadServlet.getMimeType(new File("doc.pdf"), false));
        assertEquals("text/plain", DatasetDownloadServlet.getMimeType(new File("/dir/filewithoutext"), false));
    }

    @Test
    public void testInitialDoGet() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareParseRequestURL();
        prepareForObtainingDataSetFromServer(externalData);
        prepareForGettingDataSetFromSession(externalData, "");
        prepareForCreatingHTML(writer);

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(
                "<html><head><title>Data Set Download Service: GROUP-G/PROJECT-P/EPERIMENT-E/SAMPLE-S/1234-1</title><style type=\'text/css\'> * { margin: 3px; }html { height: 100%;  }body { height: 100%; font-family: verdana, tahoma, helvetica; font-size: 11px; text-align:left; }h1 { text-align: center; padding: 1em; color: #1E4E8F;}.td_hd { border: 1px solid #FFFFFF; padding 3px; background-color: #DDDDDD; height: 1.5em; }.div_hd { background-color: #1E4E8F; color: white; font-weight: bold; padding: 3px; }table { border-collapse: collapse; padding: 1em; }tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }.td_file { font-family: verdana, tahoma, helvetica; font-size: 11px; height: 1.5em }.wrapper { min-height: 100%; height: auto !important; height: 100%; margin: 0em auto -4em; }.footer { height: 4em; text-align: center; }</style></head><body><div class=\'wrapper\'><h1>Data Set Download Service</h1><div class=\'div_hd\'>Information about data set</div><table><tr><td class=\'td_hd\'>Group:</td><td>GROUP-G</td></tr><tr><td class=\'td_hd\'>Project:</td><td>PROJECT-P</td></tr><tr><td class=\'td_hd\'>Experiment:</td><td>EPERIMENT-E</td></tr><tr><td class=\'td_hd\'>Sample:</td><td>SAMPLE-S</td></tr><tr><td class=\'td_hd\'>Data Set Code:</td><td>1234-1</td></tr></table> <div class=\'div_hd\'>Files</div><table> "
                        + OSUtilities.LINE_SEPARATOR
                        + "<tr><td class=\'td_file\'><a href=\'/datastore_server/1234-1/%2B+s+%25+%21+%23+%40\'>+ s % ! # @</td><td></td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "<tr><td class=\'td_file\'><a href=\'/datastore_server/1234-1/read+me+%40home.txt\'>read me @home.txt</td><td>12 bytes</td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "</table> </div> <div class=\'footer\'>Copyright &copy; 2008 ETHZ - <a href=\'http://www.cisd.systemsx.ethz.ch/\'>CISD</a> </div> </body></html>"
                        + OSUtilities.LINE_SEPARATOR + "", writer.toString());
        assertEquals(LOG_INFO + "Data set '1234-1' obtained from openBIS server."
                + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "For data set '1234-1' show directory <wd>/data set #123",
                getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testInitialDoGetNoSample() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData(false);
        prepareParseRequestURL();
        prepareForObtainingDataSetFromServer(externalData);
        prepareForGettingDataSetFromSession(externalData, "");
        prepareForCreatingHTML(writer);
        
        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(
                "<html><head><title>Data Set Download Service: GROUP-G/PROJECT-P/EPERIMENT-E/1234-1</title>"
                        + "<style type=\'text/css\'> * { margin: 3px; }html { height: 100%;  }body { height: 100%; font-family: verdana, tahoma, helvetica; font-size: 11px; text-align:left; }h1 { text-align: center; padding: 1em; color: #1E4E8F;}.td_hd { border: 1px solid #FFFFFF; padding 3px; background-color: #DDDDDD; height: 1.5em; }.div_hd { background-color: #1E4E8F; color: white; font-weight: bold; padding: 3px; }table { border-collapse: collapse; padding: 1em; }tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }.td_file { font-family: verdana, tahoma, helvetica; font-size: 11px; height: 1.5em }.wrapper { min-height: 100%; height: auto !important; height: 100%; margin: 0em auto -4em; }.footer { height: 4em; text-align: center; }</style></head>"
                        + "<body><div class=\'wrapper\'><h1>Data Set Download Service</h1><div class=\'div_hd\'>Information about data set</div><table><tr><td class=\'td_hd\'>Group:</td>"
                        + "<td>GROUP-G</td></tr><tr><td class=\'td_hd\'>Project:</td><td>PROJECT-P</td></tr>"
                        + "<tr><td class=\'td_hd\'>Experiment:</td><td>EPERIMENT-E</td></tr>"
                        + "<tr><td class=\'td_hd\'>Data Set Code:</td><td>1234-1</td></tr>"
                        + "</table> <div class=\'div_hd\'>Files</div><table> "
                + OSUtilities.LINE_SEPARATOR
                + "<tr><td class=\'td_file\'><a href=\'/datastore_server/1234-1/%2B+s+%25+%21+%23+%40\'>+ s % ! # @</td><td></td></tr>"
                + OSUtilities.LINE_SEPARATOR
                + "<tr><td class=\'td_file\'><a href=\'/datastore_server/1234-1/read+me+%40home.txt\'>read me @home.txt</td><td>12 bytes</td></tr>"
                + OSUtilities.LINE_SEPARATOR
                + "</table> </div> <div class=\'footer\'>Copyright &copy; 2008 ETHZ - <a href=\'http://www.cisd.systemsx.ethz.ch/\'>CISD</a> </div> </body></html>"
                + OSUtilities.LINE_SEPARATOR + "", writer.toString());
        assertEquals(LOG_INFO + "Data set '1234-1' obtained from openBIS server."
                + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "For data set '1234-1' show directory <wd>/data set #123",
                getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }
    
    private void prepareParseRequestURL()
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                    will(returnValue(EXAMPLE_SESSION_ID));

                    one(request).getParameter(DatasetDownloadServlet.DISPLAY_MODE_KEY);
                    will(returnValue(null));
                }
            });
    }

    @Test
    public void testInitialDoGetButDataSetNotFoundInStore() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        LocatorType locatorType = new LocatorType();
        locatorType.setCode("unknown");
        externalData.setLocatorType(locatorType);
        prepareParseRequestURL();
        prepareForObtainingDataSetFromServer(externalData);
        prepareForGettingDataSetFromSession(externalData, "blabla");
        context.checking(new Expectations()
            {
                {
                    one(response).setContentType("text/html");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        String pageContent = writer.toString();
        String snippet = "Data set '1234-1' not found in store";
        assertEquals("Text snippet >" + snippet + "< not found in following page content: "
                + pageContent, true, pageContent.indexOf(snippet) > 0);
        String logContent = logRecorder.getLogContent();
        assertEquals("Text snippet >" + snippet + "< not found in following page content: "
                + logContent, true, logContent.indexOf(snippet) > 0);

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetButUnknownDataSetCode() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareParseRequestURL();
        prepareForObtainingDataSetFromServer(externalData);
        context.checking(new Expectations()
            {
                {
                    one(request).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    Map<String, NewExternalData> map = new HashMap<String, NewExternalData>();
                    will(returnValue(map));

                    one(request).getRequestURI();
                    String codeAndPath = externalData.getCode();
                    will(returnValue(REQUEST_URI_PREFIX + codeAndPath));

                    one(response).setContentType("text/html");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body><h1>Error</h1>" + OSUtilities.LINE_SEPARATOR
                + "Unknown data set '1234-1'." + OSUtilities.LINE_SEPARATOR + "</body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        String logContent = logRecorder.getLogContent();
        assertEquals(LOG_INFO + "Data set '1234-1' obtained from openBIS server."
                + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "User failure: Unknown data set '1234-1'.", logContent);

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetSubFolder() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareParseRequestURLNoSession();
        prepareForGettingDataSetFromSession(externalData, ESCAPED_EXAMPLE_DATA_SET_SUB_FOLDER_NAME);
        prepareForCreatingHTML(writer);

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(
                "<html><head><title>Data Set Download Service: GROUP-G/PROJECT-P/EPERIMENT-E/SAMPLE-S/1234-1</title>"
                        + "<style type=\'text/css\'> * { margin: 3px; }html { height: 100%;  }body { height: 100%; font-family: verdana, tahoma, helvetica; font-size: 11px; text-align:left; }h1 { text-align: center; padding: 1em; color: #1E4E8F;}.td_hd { border: 1px solid #FFFFFF; padding 3px; background-color: #DDDDDD; height: 1.5em; }.div_hd { background-color: #1E4E8F; color: white; font-weight: bold; padding: 3px; }table { border-collapse: collapse; padding: 1em; }tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }.td_file { font-family: verdana, tahoma, helvetica; font-size: 11px; height: 1.5em }.wrapper { min-height: 100%; height: auto !important; height: 100%; margin: 0em auto -4em; }.footer { height: 4em; text-align: center; }</style></head>"
                        + "<body><div class=\'wrapper\'><h1>Data Set Download Service</h1><div class=\'div_hd\'>Information about data set</div>"
                        + "<table><tr><td class=\'td_hd\'>Group:</td><td>GROUP-G</td></tr>"
                        + "<tr><td class=\'td_hd\'>Project:</td><td>PROJECT-P</td></tr>"
                        + "<tr><td class=\'td_hd\'>Experiment:</td><td>EPERIMENT-E</td></tr>"
                        + "<tr><td class=\'td_hd\'>Sample:</td><td>SAMPLE-S</td></tr>"
                        + "<tr><td class=\'td_hd\'>Data Set Code:</td><td>1234-1</td></tr></table> <div class=\'div_hd\'>Files</div><table> <tr><td class=\'td_hd\'>Folder:</td><td>+ s % ! # @</td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "<tr><td class=\'td_file\'><a href=\'/datastore_server/1234-1/\'>..</td><td></td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "</table> </div> <div class=\'footer\'>Copyright &copy; 2008 ETHZ - <a href=\'http://www.cisd.systemsx.ethz.ch/\'>CISD</a> </div> </body></html>"
                        + OSUtilities.LINE_SEPARATOR, writer.toString());
        assertEquals(LOG_INFO + "For data set '1234-1' show directory <wd>/data set #123/"
                + EXAMPLE_DATA_SET_SUB_FOLDER_NAME, getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetFile() throws Exception
    {
        final ExternalData externalData = createExternalData();
        prepareParseRequestURLNoSession();
        prepareForGettingDataSetFromSession(externalData, EXAMPLE_FILE_NAME);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.checking(new Expectations()
            {
                {
                    one(response).setContentType("text/plain");
                    one(response).setContentLength(EXAMPLE_FILE_CONTENT.length());
                    one(response).setHeader("Content-Disposition",
                            "inline; filename=" + EXAMPLE_FILE_NAME);
                    one(response).getOutputStream();
                    will(returnValue(new ServletOutputStream()
                        {
                            @Override
                            public void write(int b) throws IOException
                            {
                                outputStream.write(b);
                            }
                        }));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("Hello world!", outputStream.toString());
        assertEquals(LOG_INFO + "For data set '1234-1' deliver file "
                + "<wd>/data set #123/read me @home.txt (12 bytes).", getNormalizedLogContent());

        context.assertIsSatisfied();
    }
    
    @Test
    public void testDoGetThumbnail() throws Exception
    {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", EXAMPLE_FILE);
        final ExternalData externalData = createExternalData();
        prepareParseRequestURLForThumbnail(100, 50);
        prepareForObtainingDataSetFromServer(externalData);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue(REQUEST_URI_PREFIX + EXAMPLE_DATA_SET_CODE + "/" + EXAMPLE_FILE_NAME));

                    one(request).getSession(false);
                    will(returnValue(httpSession));
                    
                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    Map<String, ExternalData> map = new HashMap<String, ExternalData>();
                    map.put(externalData.getCode(), externalData);
                    will(returnValue(map));
                  
                    one(response).setContentType("image/png");
                    one(response).setContentLength(84);
                    one(response).setHeader("Content-Disposition",
                            "inline; filename=" + EXAMPLE_FILE_NAME);
                    one(response).getOutputStream();
                    will(returnValue(new ServletOutputStream()
                        {
                            @Override
                            public void write(int b) throws IOException
                            {
                                outputStream.write(b);
                            }
                        }));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(25, thumbnail.getWidth());
        assertEquals(50, thumbnail.getHeight());
        assertEquals(LOG_INFO + "Data set '1234-1' obtained from openBIS server."
                + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "For data set '1234-1' deliver file <wd>/data set #123/read me @home.txt "
                + "as a thumbnail.", getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetNonExistingFile() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareParseRequestURLNoSession();
        prepareForGettingDataSetFromSession(externalData, "blabla");
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURL();
                    will(returnValue(new StringBuffer("requestURL")));

                    one(request).getQueryString();
                    will(returnValue("queryString"));

                    one(response).setContentType("text/html");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body><h1>Error</h1>" + OSUtilities.LINE_SEPARATOR
                + "File 'blabla' does not exist." + OSUtilities.LINE_SEPARATOR + "</body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        String logContent = getNormalizedLogContent();
        assertEquals("The following string does not start as expected: " + logContent, true,
                logContent.startsWith(LOG_ERROR
                        + "Request requestURL?queryString caused an exception:"));

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetForExpiredSession() throws Exception
    {
        final StringWriter writer = new StringWriter();
        prepareParseRequestURLNoSession();
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue(REQUEST_URI_PREFIX + EXAMPLE_DATA_SET_CODE));

                    one(request).getSession(false);
                    will(returnValue(null));

                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(EXPIRATION_MESSAGE, writer.toString());
        assertEquals("", getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetRequestURINotStartingWithApplicationName() throws Exception
    {
        final StringWriter writer = new StringWriter();
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue("blabla"));

                    one(request).getRequestURL();
                    will(returnValue(new StringBuffer("requestURL")));

                    one(request).getQueryString();
                    will(returnValue("query"));

                    one(response).setContentType("text");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("Error:" + OSUtilities.LINE_SEPARATOR
                + "Request URI 'blabla' expected to start with '/datastore_server/'."
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        String logContent = getNormalizedLogContent();
        assertEquals("The following string does not start as expected: " + logContent, true,
                logContent.startsWith(LOG_ERROR + "Request requestURL?query caused an exception:"));

        context.assertIsSatisfied();
    }

    @Test
    public void testDoGetForPathInfoStartingWithSeparator() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareParseRequestURL();
        prepareForObtainingDataSetFromServer(externalData);
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue(REQUEST_URI_PREFIX + EXAMPLE_DATA_SET_CODE));

                    one(request).getSession(false);
                    will(returnValue(null));

                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(EXPIRATION_MESSAGE, writer.toString());
        assertEquals(LOG_INFO + "Data set '1234-1' obtained from openBIS server.",
                getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    private void prepareForGettingDataSetFromSession(final ExternalData externalData,
            final String path)
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    Map<String, ExternalData> map = new HashMap<String, ExternalData>();
                    map.put(externalData.getCode(), externalData);
                    will(returnValue(map));

                    one(request).getRequestURI();
                    String codeAndPath = REQUEST_URI_PREFIX + externalData.getCode() + "/" + path;
                    will(returnValue(codeAndPath));

                    allowing(request).getRequestURI();
                    will(returnValue(GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME
                            + "/" + codeAndPath));

                }
            });
    }

    private void prepareParseRequestURLNoSession()
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.DISPLAY_MODE_KEY);
                    will(returnValue("html"));
                }
            });
    }

    private void prepareParseRequestURLForThumbnail(final int width, final int height)
    {
        context.checking(new Expectations()
        {
            {
                one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                will(returnValue(EXAMPLE_SESSION_ID));
                
                one(request).getParameter(DatasetDownloadServlet.DISPLAY_MODE_KEY);
                will(returnValue("thumbnail" + width + "x" + height));
            }
        });
    }
    
    private void prepareForObtainingDataSetFromServer(final ExternalData externalData)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetService).tryGetDataSet(EXAMPLE_SESSION_ID, EXAMPLE_DATA_SET_CODE);
                    will(returnValue(externalData));

                    one(request).getSession(true);
                    will(returnValue(httpSession));

                    one(httpSession).setMaxInactiveInterval(120);
                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    will(returnValue(null));

                    one(httpSession).setAttribute(DatasetDownloadServlet.DATA_SET_KEY,
                            new HashMap<String, ExternalDataPE>());
                }
            });
    }

    private void prepareForCreatingHTML(final StringWriter writer) throws IOException
    {
        context.checking(new Expectations()
            {
                {
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));

                    one(response).setContentType("text/html");
                }
            });
    }

    private ExternalData createExternalData()
    {
        return createExternalData(true);
    }
    
    private ExternalData createExternalData(boolean withSample)
    {
        Group group = new Group();
        group.setCode(GROUP_CODE);
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        project.setGroup(group);
        Experiment experiment = new Experiment();
        experiment.setCode(EXPERIMENT_CODE);
        experiment.setProject(project);
        final ExternalData externalData = new ExternalData();
        externalData.setExperiment(experiment);
        externalData.setCode(EXAMPLE_DATA_SET_CODE);
        if (withSample)
        {
            Sample sample = new Sample();
            sample.setCode(SAMPLE_CODE);
            externalData.setSample(sample);
        }
        LocatorType locatorType = new LocatorType();
        locatorType.setCode(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
        externalData.setLocatorType(locatorType);
        externalData.setLocation(EXAMPLE_DATA_SET_FOLDER_NAME);
        return externalData;
    }

    private DatasetDownloadServlet createServlet()
    {
        Properties properties = new Properties();
        properties.setProperty(ConfigParameters.STOREROOT_DIR_KEY, TEST_FOLDER.toString());
        properties.setProperty(ConfigParameters.PORT_KEY, "8080");
        properties.setProperty(ConfigParameters.SERVER_URL_KEY, "http://localhost");
        properties.setProperty(ConfigParameters.SESSION_TIMEOUT_KEY, "2");
        properties.setProperty(ConfigParameters.KEYSTORE_PATH_KEY, "/");
        properties.setProperty(ConfigParameters.KEYSTORE_PASSWORD_KEY, "x");
        properties.setProperty(ConfigParameters.KEYSTORE_KEY_PASSWORD_KEY, "y");
        ConfigParameters configParameters = new ConfigParameters(properties);
        return new DatasetDownloadServlet(new ApplicationContext(dataSetService, configParameters));
    }

    private String getNormalizedLogContent()
    {
        String logContent = logRecorder.getLogContent();
        logContent = logContent.replace(TEST_FOLDER.getAbsolutePath(), "<wd>");
        logContent = logContent.replace('\\', '/');
        return logContent;
    }

}
