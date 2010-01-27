/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.test;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;

/**
 * @author Piotr Buczek
 */
public class DataSetUtilsTest extends AssertJUnit
{

    @DataProvider(name = "patterns")
    protected Object[][] getPatterns()
    {
        return new Object[][]
            {
                { DataSetUtils.REGEXP_PREFIX + "file_.+\\.*", "file_.+\\.*" },
                { "file$^.|(1){2}<3>+", "file\\$\\^\\.\\|\\(1\\)\\{2\\}\\<3\\>\\+" },
                { "dir\\file", "dir\\\\file" },
                { "file.*", "file\\..*" },
                { "file.?", "file\\.." },
                { "*.tsv", ".*\\.tsv" },
                { "file[0-9].txt", "file[0-9]\\.txt" },
                { "*.", ".*\\." },
                { ".*", "\\..*" }

            };
    }

    @Test(dataProvider = "patterns")
    public void testTranslateToRegexp(String wildcardPattern, String expectedRegexpPattern)
    {
        assertEquals(expectedRegexpPattern, DataSetUtils.translateToRegexp(wildcardPattern));
    }

}
