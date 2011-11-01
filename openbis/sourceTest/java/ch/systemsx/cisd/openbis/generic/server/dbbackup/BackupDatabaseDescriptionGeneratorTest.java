/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dbbackup;

import junit.framework.TestCase;

import org.testng.annotations.Test;

/**
 * @author Kaloyan Enimanev
 */
public class BackupDatabaseDescriptionGeneratorTest extends TestCase
{
    @Test
    public void testParser()
    {
        BackupDatabaseDescriptionGenerator generator = new BackupDatabaseDescriptionGenerator();
        generator.process(new String[]
            { getResourceFileName("as-service.properties"),
                    getResourceFileName("dss-service.properties") });

        String username = System.getProperty("user.name");
        String resultTemplate =
                "database=openbis_fookind;username=%s;password=\n"
                        + "database=phosphonetx_dev;username=%s;password=\n"
                        + "database=imaging_barkind;username=%s;password=";
        String expectedResult = String.format(resultTemplate, username, username, username);

        assertEquals(expectedResult, generator.getResult());

    }

    private String getResourceFileName(String fileName)
    {
        return "../openbis/resource/test-data/" + getClass().getSimpleName() + "/" + fileName;
    }

}
