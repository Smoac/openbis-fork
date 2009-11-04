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

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractValidator implements IValidator
{
    private final boolean allowEmptyValues;
    
    AbstractValidator(boolean allowEmptyValues)
    {
        this.allowEmptyValues = allowEmptyValues;
    }
    
    public final void assertValid(String value)
    {
        if (allowEmptyValues)
        {
            if (StringUtils.isBlank(value))
            {
                return;
            }
        } else if (StringUtils.isBlank(value))
        {
            throw new UserFailureException("Empty value is not allowed.");
        }
        assertValidNonEmptyValue(value);
    }
    
    protected abstract void assertValidNonEmptyValue(String value);

}
