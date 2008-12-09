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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Criteria describing which samples should be listed.
 * 
 * @author Tomasz Pylak
 */
// TODO 2008-12-09, Christian Ribeaud: Delete this class. Only use the GWT DTO 'ListSampleCriteria'.
public class ListSampleCriteriaDTO extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    // list samples belonging to a given group or database instance
    private List<SampleOwnerIdentifier> ownerIdentifiers;

    // list samples of particular type
    private SampleTypePE sampleType;

    private SampleIdentifier containerIdentifier;

    public final SampleIdentifier getContainerIdentifier()
    {
        return containerIdentifier;
    }

    public final void setContainerIdentifier(final SampleIdentifier container)
    {
        this.containerIdentifier = container;
    }

    public List<SampleOwnerIdentifier> getOwnerIdentifiers()
    {
        return ownerIdentifiers;
    }

    public SampleTypePE getSampleType()
    {
        return sampleType;
    }

    public final void setOwnerIdentifiers(final List<SampleOwnerIdentifier> ownerIdentifiers)
    {
        this.ownerIdentifiers = ownerIdentifiers;
    }

    public final void setSampleType(final SampleTypePE sampleType)
    {
        this.sampleType = sampleType;
    }
}
