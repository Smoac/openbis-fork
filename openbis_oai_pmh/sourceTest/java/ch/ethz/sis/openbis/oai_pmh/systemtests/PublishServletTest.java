/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.oai_pmh.systemtests;

import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.http.HttpTest;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public class PublishServletTest extends OAIPMHSystemTest
{

    private static final String PUBLISH_SERVLET_URL = TestInstanceHostUtils.getDSSUrl() + "/publish";

    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "password";

    @Test
    public void test()
    {
        GetMethod result = HttpTest.sendRequest(USER_ID, USER_PASSWORD, PUBLISH_SERVLET_URL + "?verb=ListIdentifiers&metadataPrefix=oai_dc");
        System.out.println("RESULT: " + result);
    }
}
