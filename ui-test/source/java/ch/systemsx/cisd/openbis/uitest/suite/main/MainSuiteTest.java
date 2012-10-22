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

package ch.systemsx.cisd.openbis.uitest.suite.main;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;

/**
 * @author anttil
 */
public abstract class MainSuiteTest extends SeleniumTest
{
    @BeforeTest
    public void before()
    {
        useGui();

        login(ADMIN_USER, ADMIN_PASSWORD);

        // this is because of BIS-184
        if (tabsContain(sampleBrowser()))
        {
            switchTabTo(sampleBrowser()).allSpaces();
        }
    }

    @AfterTest
    public void after()
    {
        logout();
    }
}
