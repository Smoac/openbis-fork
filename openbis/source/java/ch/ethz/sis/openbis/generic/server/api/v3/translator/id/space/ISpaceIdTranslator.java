/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.id.space;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnsupportedObjectIdException;

/**
 * @author pkupczyk
 */
public class ISpaceIdTranslator extends
        AbstractTranslator<ISpaceId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.space.ISpaceId>
{

    @Override
    protected ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.space.ISpaceId doTranslate(
            ISpaceId id)
    {
        if (id instanceof SpacePermId)
        {
            return new SpacePermIdTranslator().translate((SpacePermId) id);
        } else
        {
            throw new UnsupportedObjectIdException(id);
        }
    }

}
