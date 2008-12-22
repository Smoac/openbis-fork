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

package ch.systemsx.cisd.openbis.generic;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.google.gwt.junit.tools.GWTTestSuite;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AuthenticationTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AuthorizationManagementConsolTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.EntityTypePropertyTypeAssignmentTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ExperimentBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PropertyTypeAssignmentBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PropertyTypeBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.PropertyTypeRegistrationTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SampleBrowserTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SearchTest;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VocabularyRegistrationTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewerTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationTest;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewerTest;

/**
 * @author Franz-Josef Elmer
 */
public class SystemTestSuite extends GWTTestSuite
{
    public static Test suite()
    {
        final TestSuite testSuite = new TestSuite("System Tests");
        testSuite.addTestSuite(AuthenticationTest.class);
        testSuite.addTestSuite(AuthorizationManagementConsolTest.class);
        testSuite.addTestSuite(SampleBrowserTest.class);
        testSuite.addTestSuite(GenericSampleViewerTest.class);
        testSuite.addTestSuite(SearchTest.class);
        testSuite.addTestSuite(GenericSampleRegistrationTest.class);
        testSuite.addTestSuite(ExperimentBrowserTest.class);
        testSuite.addTestSuite(GenericExperimentViewerTest.class);
        testSuite.addTestSuite(PropertyTypeBrowserTest.class);
        testSuite.addTestSuite(PropertyTypeAssignmentBrowserTest.class);
        testSuite.addTestSuite(EntityTypePropertyTypeAssignmentTest.class);
        testSuite.addTestSuite(VocabularyRegistrationTest.class);
        testSuite.addTestSuite(PropertyTypeRegistrationTest.class);
        return testSuite;
    }
}
