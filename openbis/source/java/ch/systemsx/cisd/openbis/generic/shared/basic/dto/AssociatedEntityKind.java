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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;

/**
 * The <i>GWT</i> counterpart to
 * ch.systemsx.cisd.openbis.generic.shared.api.v1.SearchableEntityKind.
 * 
 * @author Piotr Buczek
 */
public enum AssociatedEntityKind implements ISerializable
{
    SAMPLE("Sample", EntityKind.SAMPLE),

    EXPERIMENT("Experiment", EntityKind.EXPERIMENT),

    SAMPLE_CONTAINER("Container", EntityKind.SAMPLE),

    SAMPLE_PARENT("Parent", EntityKind.SAMPLE);

    private final String description;

    private final EntityKind entityKind;

    private AssociatedEntityKind(final String description, final EntityKind entityKind)
    {
        this.description = description;
        this.entityKind = entityKind;
    }

    public final String getDescription()
    {
        return description;
    }

    public final EntityKind getEntityKind()
    {
        return entityKind;
    }

}
