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

import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * A role code translator.
 * 
 * @author Christian Ribeaud
 */
public final class RoleCodeTranslator
{

    private RoleCodeTranslator()
    {
        // Can not be instantiated.
    }

    public final static RoleCode translate(final String code)
    {
        if ("INSTANCE_ADMIN".equals(code))
        {
            return RoleCode.ADMIN;
        } else if ("GROUP_ADMIN".equals(code))
        {
            return RoleCode.ADMIN;
        } else if ("USER".equals(code))
        {
            return RoleCode.USER;
        } else if ("OBSERVER".equals(code))
        {
            return RoleCode.OBSERVER;
        } else
        {
            throw new IllegalArgumentException("Unknown role set");
        }
    }

}
