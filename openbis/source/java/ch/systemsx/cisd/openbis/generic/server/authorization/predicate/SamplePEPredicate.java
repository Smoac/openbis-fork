/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * This predicate by default authenticates for write access, i.e. it will allow not access to shared samples for all users.
 * 
 * @author anttil
 */
public class SamplePEPredicate extends PersistentEntityPredicate<SamplePE>
{

    public SamplePEPredicate()
    {
        super();
    }

    public SamplePEPredicate(boolean isReadAccess)
    {
        super(isReadAccess);
    }

    @Override
    public SpacePE getSpace(SamplePE value)
    {
        return value.getSpace();
    }
}
