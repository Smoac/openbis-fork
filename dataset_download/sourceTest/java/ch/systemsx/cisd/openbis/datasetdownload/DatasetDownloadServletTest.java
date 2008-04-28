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

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadServletTest 
{
    private BufferedAppender logRecorder;
    
    private Mockery context;

    private HttpServletRequest request;

    private HttpServletResponse response;
    
    @BeforeMethod
    public void setUp()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testDoGet() throws Exception
    {
        final StringWriter writer = new StringWriter();
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(DatasetDownloadServlet.DATASET_CODE_KEY);
                    will(returnValue("1234-1"));
                    
                    one(request).getParameter(DatasetDownloadServlet.SESSION_ID_KEY);
                    will(returnValue("AV76CF"));
                    
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });
        DatasetDownloadServlet servlet = new DatasetDownloadServlet();
        servlet.init();
        
        servlet.doGet(request, response);
        
        assertEquals("<html><body>Download dataset 1234-1 (sessionID:AV76CF)</body></html>", writer.toString());
    }
    
}
