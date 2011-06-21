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

package eu.basynthec.cisd.dss.growthprofiles;

import java.io.File;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunner;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class OD600ValidatorTest extends AssertJUnit
{
    private static final String[] VALIDATION_SCRIPT_PATH = new String[]
        { "dist/etc/growth-profiles/data-set-validator.py" };

    @Test
    public void testGoodData()
    {
        ValidationScriptRunner scriptRunner =
                ValidationScriptRunner.createValidatorFromScriptPaths(VALIDATION_SCRIPT_PATH);
        List<ValidationError> errors =
                scriptRunner.validate(new File("sourceTest/examples/OD600-Example.xlsx"));
        assertTrue("The example should have no errors", errors.isEmpty());
    }

    @Test
    public void testTemplate()
    {
        ValidationScriptRunner scriptRunner =
                ValidationScriptRunner.createValidatorFromScriptPaths(VALIDATION_SCRIPT_PATH);
        List<ValidationError> errors =
                scriptRunner.validate(new File("sourceTest/examples/OD600-Template.xlsx"));
        if (errors.size() > 0)
        {
            System.out.println(errors);
        }
        assertEquals("The template should have five errors", 5, errors.size());
    }

}
