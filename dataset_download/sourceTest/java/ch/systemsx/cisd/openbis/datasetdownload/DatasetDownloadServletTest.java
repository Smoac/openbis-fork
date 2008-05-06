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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

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
    private static final File TEST_FOLDER = new File("targets/unit-test/store");
    
    private static final String EXAMPLE_DATA_SET_FOLDER_NAME = "data-set-123";

    private static final File EXAMPLE_DATA_SET_FOLDER = new File(TEST_FOLDER, EXAMPLE_DATA_SET_FOLDER_NAME);
    
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
    public void testDoGet() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final ExternalData externalData = new ExternalData();
        externalData.setCode(EXAMPLE_DATA_SET_CODE);
        externalData.setLocatorType(new LocatorType(LocatorType.DEFAULT_LOCATOR_TYPE_CODE));
        externalData.setLocation(EXAMPLE_DATA_SET_FOLDER_NAME);
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(DatasetDownloadServlet.DATASET_CODE_KEY);
                    will(returnValue(EXAMPLE_DATA_SET_CODE));
                    
                    one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                    will(returnValue(EXAMPLE_SESSION_ID));
                    
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                    
                    one(dataSetService).getDataSet(EXAMPLE_SESSION_ID, EXAMPLE_DATA_SET_CODE);
                    will(returnValue(externalData));
                    
                    one(request).getSession(true);
                    will(returnValue(httpSession));
                    
                    one(httpSession).setMaxInactiveInterval(120);
                    one(httpSession).setAttribute(DatasetDownloadServlet.DATA_SET_KEY, externalData);
                    one(httpSession).setAttribute(DatasetDownloadServlet.DATA_SET_ROOT_DIR_KEY, EXAMPLE_DATA_SET_FOLDER);
                    
                    one(request).getSession(false);
                    will(returnValue(httpSession));
                    
                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_KEY);
                    will(returnValue(externalData));
                    
                    one(httpSession).getAttribute(DatasetDownloadServlet.DATA_SET_ROOT_DIR_KEY);
                    will(returnValue(EXAMPLE_DATA_SET_FOLDER));
                    
                    one(request).getPathInfo();
                    will(returnValue(null));
                    
                    one(request).getRequestURI();
                    will(returnValue("uri"));
                    
                    one(response).setContentType("text/html");
                }
            });
        
        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body>" + OSUtilities.LINE_SEPARATOR + "<h1>Data Set 1234-1</h1>"
                + OSUtilities.LINE_SEPARATOR
                + "<table border=\'0\' cellpadding=\'5\' cellspacing=\'0\'>"
                + OSUtilities.LINE_SEPARATOR + "</table></body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        
        context.assertIsSatisfied();
    }

    private DatasetDownloadServlet createServlet()
    {
        Properties properties = new Properties();
        properties.setProperty(ConfigParameters.STOREROOT_DIR_KEY, TEST_FOLDER.toString());
        properties.setProperty(ConfigParameters.PORT_KEY, "8080");
        properties.setProperty(ConfigParameters.SERVER_URL_KEY, "http://localhost");
        properties.setProperty(ConfigParameters.USERNAME_KEY, "demo");
        properties.setProperty(ConfigParameters.PASSWORD_KEY, "pwd");
        properties.setProperty(ConfigParameters.SESSION_TIMEOUT_KEY, "2");
        ConfigParameters configParameters = new ConfigParameters(properties);
        return new DatasetDownloadServlet(new ApplicationContext(dataSetService, configParameters));
    }
    
}
