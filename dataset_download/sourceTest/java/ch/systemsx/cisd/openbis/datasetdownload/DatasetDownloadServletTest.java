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

package ch.systemsx.cisd.openbis.datasetdownload;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.lims.base.ExternalData;
import ch.systemsx.cisd.lims.base.IDataSetService;
import ch.systemsx.cisd.lims.base.LocatorType;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServletTest 
{
    private static final String EXPIRATION_MESSAGE =
            "<html><body>Download session expired.</body></html>";

    private static final String LOGGER_NAME =
            "OPERATION.ch.systemsx.cisd.openbis.datasetdownload.DatasetDownloadServlet";

    private static final String LOG_INFO = "INFO  " + LOGGER_NAME + " - ";
    
    private static final String LOG_ERROR = "ERROR " + LOGGER_NAME + " - ";

    private static final File TEST_FOLDER = new File("targets/unit-test/store");
    
    private static final String EXAMPLE_DATA_SET_FOLDER_NAME = "data-set-123";

    private static final File EXAMPLE_DATA_SET_FOLDER =
            new File(TEST_FOLDER, EXAMPLE_DATA_SET_FOLDER_NAME);
    
    private static final String EXAMPLE_FILE_NAME = "readme.txt";
    
    private static final File EXAMPLE_FILE = new File(EXAMPLE_DATA_SET_FOLDER, EXAMPLE_FILE_NAME);
    
    private static final String EXAMPLE_FILE_CONTENT = "Hello world!";
    
    private static final String EXAMPLE_DATA_SET_SUB_FOLDER_NAME = "sub";
    
    private static final File EXAMPLE_DATA_SET_SUB_FOLDER =
            new File(EXAMPLE_DATA_SET_FOLDER, EXAMPLE_DATA_SET_SUB_FOLDER_NAME);
    
    private static final String EXAMPLE_SESSION_ID = "AV76CF";

    private static final String EXAMPLE_DATA_SET_CODE = "1234-1";

    private BufferedAppender logRecorder;
    
    private Mockery context;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private IDataSetService dataSetService;

    private HttpSession httpSession;
    
    @BeforeMethod
    public void setUp()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        dataSetService = context.mock(IDataSetService.class);
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
    public void testInitialDoGet() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareForObtainingDataSetFromServer(externalData);
        prepareForGettingDataSetFromSession(externalData, "");
        prepareForCreatingHTML(writer);
        
        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body>" + OSUtilities.LINE_SEPARATOR + "<h1>Data Set 1234-1</h1>"
                + OSUtilities.LINE_SEPARATOR
                + "<table border=\'0\' cellpadding=\'5\' cellspacing=\'0\'>"
                + OSUtilities.LINE_SEPARATOR + "<tr><td><a href='download/1234-1/sub'>sub</td><td></td></tr>"
                + OSUtilities.LINE_SEPARATOR
                + "<tr><td><a href='download/1234-1/readme.txt'>readme.txt</td><td>12 Bytes</td></tr>"
                + OSUtilities.LINE_SEPARATOR + "</table></body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        assertEquals(LOG_INFO + "Data set '1234-1' obtained from openBIS server."
                + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "For data set '1234-1' show directory <wd>/data-set-123",
                getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }

    @Test
    public void testInitialDoGetButDataSetNotFoundInStore() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        externalData.setLocatorType(new LocatorType("unknown"));
        prepareForObtainingDataSetFromServer(externalData);
        prepareForGettingDataSetFromSession(externalData, "blabla");
        context.checking(new Expectations()
            {
                {
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
        prepareForObtainingDataSetFromServer(externalData);
        context.checking(new Expectations()
            {
                {
                    one(request).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    Map<String, ExternalData> map = new HashMap<String, ExternalData>();
                    will(returnValue(map));

                    one(request).getPathInfo();
                    String codeAndPath = externalData.getCode();
                    will(returnValue(codeAndPath));
                    
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
        prepareForNotObtainingDataSetFromServer();
        prepareForGettingDataSetFromSession(externalData, EXAMPLE_DATA_SET_SUB_FOLDER_NAME);
        prepareForCreatingHTML(writer);
        
        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body>" + OSUtilities.LINE_SEPARATOR + "<h1>Data Set 1234-1</h1>"
                + OSUtilities.LINE_SEPARATOR + "Folder: sub"
                + OSUtilities.LINE_SEPARATOR
                + "<table border=\'0\' cellpadding=\'5\' cellspacing=\'0\'>"
                + OSUtilities.LINE_SEPARATOR + "<tr><td><a href='download/1234-1/'>..</td><td></td></tr>"  
                + OSUtilities.LINE_SEPARATOR + "</table></body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        assertEquals(LOG_INFO + "For data set '1234-1' show directory <wd>/data-set-123/sub",
                getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDoGetFile() throws Exception
    {
        final ExternalData externalData = createExternalData();
        prepareForNotObtainingDataSetFromServer();
        prepareForGettingDataSetFromSession(externalData, EXAMPLE_FILE_NAME);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.checking(new Expectations()
            {
                {
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
        assertEquals(LOG_INFO
                + "For data set '1234-1' deliver file <wd>/data-set-123/readme.txt (12 bytes).",
                getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDoGetNonExistingFile() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareForNotObtainingDataSetFromServer();
        prepareForGettingDataSetFromSession(externalData, "blabla");
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURL();
                    will(returnValue(new StringBuffer("requestURL")));
                    
                    one(request).getQueryString();
                    will(returnValue("queryString"));

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
        prepareForNotObtainingDataSetFromServer();
        context.checking(new Expectations()
            {
                {
                    one(request).getPathInfo();
                    will(returnValue(EXAMPLE_DATA_SET_CODE));
                    
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
    public void testDoGetForNullPathInfo() throws Exception
    {
        final StringWriter writer = new StringWriter();
        context.checking(new Expectations()
        {
            {
                one(request).getPathInfo();
                will(returnValue(null));
                
                one(response).getWriter();
                will(returnValue(new PrintWriter(writer)));
            }
        });
        
        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body><h1>Error</h1>" + OSUtilities.LINE_SEPARATOR
                + "Path not specified in URL." + OSUtilities.LINE_SEPARATOR + "</body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        assertEquals(LOG_INFO + "User failure: Path not specified in URL.",
                getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDoGetForPathInfoStartingWithSeparator() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = createExternalData();
        prepareForObtainingDataSetFromServer(externalData);
        context.checking(new Expectations()
        {
            {
                one(request).getPathInfo();
                will(returnValue("/" + EXAMPLE_DATA_SET_CODE));
                
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

                    one(request).getPathInfo();
                    String codeAndPath = externalData.getCode() + "/" + path;
                    will(returnValue(codeAndPath));

                    allowing(request).getRequestURI();
                    will(returnValue("download/" + codeAndPath));

                }
            });
    }
    
    private void prepareForNotObtainingDataSetFromServer()
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                    will(returnValue(null));
                }
            });
    }

    private void prepareForObtainingDataSetFromServer(final ExternalData externalData)
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                    will(returnValue(EXAMPLE_SESSION_ID));

                    one(dataSetService).getDataSet(EXAMPLE_SESSION_ID, EXAMPLE_DATA_SET_CODE);
                    will(returnValue(externalData));

                    one(request).getSession(true);
                    will(returnValue(httpSession));
                    
                    one(httpSession).setMaxInactiveInterval(120);
                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    will(returnValue(null));
                    
                    one(httpSession).setAttribute(DatasetDownloadServlet.DATA_SET_KEY,
                            new HashMap<String, ExternalData>());
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
        final ExternalData externalData = new ExternalData();
        externalData.setCode(EXAMPLE_DATA_SET_CODE);
        externalData.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
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
        properties.setProperty(ConfigParameters.KEYSTORE_PATH_KEY, "");
        properties.setProperty(ConfigParameters.KEYSTORE_PASSWORD_KEY, "");
        properties.setProperty(ConfigParameters.KEYSTORE_KEY_PASSWORD_KEY, "");
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
