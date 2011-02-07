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

package ch.systemsx.cisd.openbis.generic.server.business;

import static ch.systemsx.cisd.openbis.generic.server.business.PropertiesBatchEvaluationErrors.MAX_ERROR_DETAILS_KEPT;
import static ch.systemsx.cisd.openbis.generic.server.business.PropertiesBatchEvaluationErrors.MAX_ERROR_IN_USER_MESSAGE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;

/**
 * @author Kaloyan Enimanev
 */
public class PropertiesBatchEvaluationErrorsTest extends AssertJUnit
{

    private final String ERROR_TEXT = "errtext";

    private final String CODE = "propcode";

    private final int totalRows = 100;

    private PropertiesBatchEvaluationErrors errors;

    private ScriptPE script;


    @BeforeMethod
    public void setUp()
    {
        errors = createErrorsObject();
        script = createScript();
    }

    @Test
    public void testAllRowsFailWithTheSameErrorMessage()
    {
        for (int i = 1; i <= totalRows; i++)
        {
            errors.accumulateError(i, new EvaluatorException(ERROR_TEXT), CODE, script);
        }

        assertTrue(errors.hasErrors());
        assertEquals(
                "Script malfunction in 100 out of 100 rows.\n"
                        + "100 rows including [1, 2, 3] have failed due to the property 'propcode' causing a malfuction in the script "
                        + "(name = 'script.py', registrator = 'admin@vip.net'): errtext\n"
                        + "A detailed error report has been sent to your system administrator.",
                errors.constructUserFailureMessage());

        String email = errors.constructErrorReportEmail();
        // exactly one stack trace shown
        assertOccurences(ERROR_TEXT, 1, email);
        assertOccurences(
                "100 rows including \\[1, 2, 3] have failed due to the property 'propcode' causing a malfuction",
                1, email);
    }

    @Test
    public void testAllRowsFailWithADifferentErrorMessage()
    {
        for (int i = 1; i <= totalRows; i++)
        {
            errors.accumulateError(i, new EvaluatorException(ERROR_TEXT + i), CODE, script);
        }

        assertTrue(errors.hasErrors());

        String errorMessage = errors.constructUserFailureMessage();
        // not more than 3 messages shown to the user
        assertOccurences(ERROR_TEXT, MAX_ERROR_IN_USER_MESSAGE,
                errorMessage);

        String email = errors.constructErrorReportEmail();
        // only the first 10 stack traces are included
        assertOccurences(ERROR_TEXT, MAX_ERROR_DETAILS_KEPT, email);
        assertOccurences("Row 1 has failed due to the property 'propcode' causing a malfuction", 1,
                email);
    }

    private void assertOccurences(String pattern, int times, String string)
    {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(string);
        int actualCount = 0;
        while (matcher.find())
        {
            actualCount++;
        }
        String errFormat =
                String.format("Invalid number of occurences of '%s' in '%s'", pattern, string);
        assertEquals(errFormat, times, actualCount);
    }

    private PropertiesBatchEvaluationErrors createErrorsObject()
    {
        PersonPE registrator = createPerson("user@slum.net");
        return new PropertiesBatchEvaluationErrors(registrator, totalRows);
    }

    private PersonPE createPerson(String email)
    {
        PersonPE result = new PersonPE();
        result.setEmail(email);
        return result;
    }

    private ScriptPE createScript()
    {
        ScriptPE result = new ScriptPE();
        result.setName("script.py");
        result.setRegistrator(createPerson("admin@vip.net"));
        return result;
    }
}
