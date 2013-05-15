/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.jstest.suite.common;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.jstest.layout.OpenbisJsWebappLocation;
import ch.systemsx.cisd.openbis.jstest.layout.OpenbisScreeningJsWebappLocation;
import ch.systemsx.cisd.openbis.jstest.page.OpenbisJsCommonWebapp;
import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.Location;

/**
 * @author pkupczyk
 */
public class JsTestCommonSelenium extends SeleniumTest
{

    @Override
    protected String startApplicationServer() throws Exception
    {
        JsTestCommonApplicationServer as = new JsTestCommonApplicationServer();
        as.setDeamon(true);
        return as.start();
    }

    @Override
    protected String startDataStoreServer() throws Exception
    {
        JsTestCommonDataStoreServer1 dss = new JsTestCommonDataStoreServer1();
        dss.setDeamon(true);
        return dss.start();
    }

    @Override
    protected String startDataStoreServer2() throws Exception
    {
        JsTestCommonDataStoreServer2 dss = new JsTestCommonDataStoreServer2();
        dss.setDeamon(true);
        return dss.start();
    }

    @BeforeTest
    public void before()
    {
        useGui();

        login(ADMIN_USER, ADMIN_PASSWORD);
    }

    @AfterTest
    public void after()
    {
        logout();
    }

    @Test
    public void runOpenbisJsTests()
    {
        runTests("runOpenbisJsTests", new OpenbisJsWebappLocation());
    }

    @Test
    public void runOpenbisScreeningJsTests()
    {
        runTests("runOpenbisScreeningJsTests", new OpenbisScreeningJsWebappLocation());
    }

    private void runTests(String method, Location<OpenbisJsCommonWebapp> location)
    {
        try
        {
            OpenbisJsCommonWebapp webapp = browser().goTo(location);
            int failedCount = webapp.getFailedCount();

            File report =
                    new File("targets/dist/" + this.getClass().getSimpleName() + "/" + method
                            + "/TEST-" + method + ".xml");
            FileUtilities.writeToFile(report, webapp.getJunitReport());

            Assert.assertEquals(0, failedCount);
        } finally
        {
            SeleniumTest.driver.switchTo().defaultContent();
        }
    }
}
