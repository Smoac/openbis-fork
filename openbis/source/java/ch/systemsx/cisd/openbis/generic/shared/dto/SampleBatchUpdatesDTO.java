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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Piotr Buczek
 */
public class SampleBatchUpdatesDTO extends SampleUpdatesDTO
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private SampleIdentifier oldSampleIdentifierOrNull;

    private SampleBatchUpdateDetails details;

    public SampleBatchUpdatesDTO(SampleIdentifier oldSampleIdentifier,
            List<IEntityProperty> properties, ExperimentIdentifier experimentIdentifierOrNull,
            SampleIdentifier sampleIdentifier, String parentIdentifierOrNull,
            String containerIdentifierOrNull, SampleBatchUpdateDetails details)
    {
        super(null, properties, experimentIdentifierOrNull, null, null, sampleIdentifier,
                parentIdentifierOrNull, containerIdentifierOrNull);
        this.oldSampleIdentifierOrNull = oldSampleIdentifier;
        this.details = details;
    }

    public SampleIdentifier getOldSampleIdentifierOrNull()
    {
        return oldSampleIdentifierOrNull;
    }

    public SampleBatchUpdateDetails getDetails()
    {
        return details;
    }

}
