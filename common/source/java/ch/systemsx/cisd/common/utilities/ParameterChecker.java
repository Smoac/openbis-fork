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

package ch.systemsx.cisd.common.utilities;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * 
 *
 * @author Tomasz Pylak
 */
public class ParameterChecker
{

    /**
     * Checks given <var>object</var> against <i>blank</i> value.
     * <p>
     * If <i></i> then a {@link UserFailureException} is thrown with given <var>name</var> as
     * information hint. This method internally uses {@link StringUtils#isBlank(String)} to perform
     * its job.
     * </p>
     */
    public static void checkIfNotBlank(final String object, final String name)
            throws UserFailureException
    {
        if (StringUtils.isBlank(object))
        {
            throw UserFailureException.fromTemplate("No '%s' specified.", name);
        }
    }

    /**
     * Checks given <var>object</var> against <code>null</code>.
     * <p>
     * If <code>null</code> then a {@link UserFailureException} is thrown with given <var>name</var>
     * as information hint.
     * </p>
     */
    public final static void checkIfNotNull(final Object object, final String name)
            throws UserFailureException
    {
        if (object == null)
        {
            throw UserFailureException.fromTemplate("No %s specified.", name);
        }
    }
}
