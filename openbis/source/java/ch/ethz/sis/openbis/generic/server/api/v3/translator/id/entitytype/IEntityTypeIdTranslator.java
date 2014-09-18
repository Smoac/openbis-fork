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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.id.entitytype;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.IEntityTypeId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnsupportedObjectIdException;

/**
 * @author pkupczyk
 */
public class IEntityTypeIdTranslator extends
        AbstractTranslator<IEntityTypeId, ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.entitytype.IEntityTypeId>
{

    @Override
    protected ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.entitytype.IEntityTypeId doTranslate(IEntityTypeId id)
    {
        if (id instanceof EntityTypePermId)
        {
            return new EntityTypePermIdTranslator().translate((EntityTypePermId) id);
        } else
        {
            throw new UnsupportedObjectIdException(id);
        }
    }

}
