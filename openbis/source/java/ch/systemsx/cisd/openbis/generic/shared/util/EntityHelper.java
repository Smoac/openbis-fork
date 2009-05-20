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

package ch.systemsx.cisd.openbis.generic.shared.util;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author Izabela Adamczyk
 */
public class EntityHelper
{
    /**
     * Creates {@link EntityType} appropriate for given {@link EntityKind}.
     */
    public static EntityType createEntityType(EntityKind kind, String code)
    {
        EntityType type = null;
        switch (kind)
        {
            case DATA_SET:
                type = new DataSetType();
                break;
            case EXPERIMENT:
                type = new ExperimentType();
                break;
            case MATERIAL:
                type = new MaterialType();
                break;
            case SAMPLE:
                type = new SampleType();
                break;
        }
        assert type != null;
        type.setCode(code);
        return type;
    }
}
