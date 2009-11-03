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

package ch.systemsx.cisd.etlserver.validation;

import java.util.regex.Pattern;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class RegExBasedValidator implements IValidator, IColumnHeaderValidator
{
    private final Pattern pattern;

    RegExBasedValidator(String regularExpression)
    {
        pattern = Pattern.compile(regularExpression);
    }

    public void assertValid(String value)
    {
        if (isValidHeader(value) == false)
        {
            throw new UserFailureException("'" + value
                    + "' dosn't match the following regular expression: " + pattern);
        }
    }

    public boolean isValidHeader(String header)
    {
        return pattern.matcher(header).matches();
    }

}
