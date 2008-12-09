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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Translator of server side {@link ch.systemsx.cisd.common.exceptions.UserFailureException} into
 * GWT compatible {@link UserFailureException}.
 * 
 * @author Franz-Josef Elmer
 */
public class UserFailureExceptionTranslator
{
    private static final String WEB_CLIENT_EXCEPTIONS_PACKAGE =
            getPackageName(UserFailureException.class);

    private static String getPackageName(Class<?> clazz)
    {
        String fullName = clazz.getName();
        return fullName.substring(0, fullName.length() - clazz.getSimpleName().length() - 1);
    }

    private UserFailureExceptionTranslator()
    {
    }

    /**
     * Converts any {@link ch.systemsx.cisd.common.exceptions.UserFailureException} or subclass of
     * it to a <i>GWT</i> {@link UserFailureException} (or subclass of it if this one could be
     * found in the same package).
     */
    public static UserFailureException translate(
            ch.systemsx.cisd.common.exceptions.UserFailureException exception)
    {
        final String className =
                WEB_CLIENT_EXCEPTIONS_PACKAGE + "." + exception.getClass().getSimpleName();
        String message = exception.getMessage();
        try
        {
            return ClassUtils.create(UserFailureException.class, className, message);
        } catch (final CheckedExceptionTunnel e)
        {
            return new UserFailureException(message);
        }

    }
}
