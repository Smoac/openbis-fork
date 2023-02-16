/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.Method;

import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;

/**
 * A dummy <code>IReturnValueFilter</code> implementation which does not apply any filter to given return value.
 * 
 * @author Christian Ribeaud
 */
public final class NoReturnValueFilter implements IReturnValueFilter
{

    //
    // IReturnValueFilter
    //

    @Override
    public final Object applyFilter(final IAuthSession session, final Method method,
            final Object returnValueOrNull)
    {
        return returnValueOrNull;
    }

}
