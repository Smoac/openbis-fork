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

import static ch.systemsx.cisd.openbis.generic.server.business.PropertiesBatchEvaluationErrors.MAX_ERRORS_IN_USER_MESSAGE;
import static ch.systemsx.cisd.openbis.generic.server.business.PropertiesBatchEvaluationErrors.MAX_ERROR_DETAILS_KEPT;

import org.apache.commons.lang.StringUtils;
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
        assertEquals(1, StringUtils.countMatches(email, ERROR_TEXT));

        String pattern =
                "100 rows including [1, 2, 3] have failed due to the property 'propcode' causing a malfuction";
        assertEquals(1, StringUtils.countMatches(email, pattern));
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
        assertEquals(MAX_ERRORS_IN_USER_MESSAGE, StringUtils.countMatches(errorMessage, ERROR_TEXT));

        String email = errors.constructErrorReportEmail();
        // only the first 10 stack traces are included
        assertEquals(MAX_ERROR_DETAILS_KEPT, StringUtils.countMatches(email, ERROR_TEXT));
        assertEquals(1, StringUtils.countMatches(email,
                "Row 1 has failed due to the property 'propcode' causing a malfuction"));
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
