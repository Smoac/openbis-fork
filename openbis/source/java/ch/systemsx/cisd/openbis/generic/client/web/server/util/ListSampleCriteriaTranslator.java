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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * A {@link ListSampleCriteria} &lt;--&gt; {@link ListSampleCriteriaDTO} translator.
 * 
 * @author Christian Ribeaud
 */
public final class ListSampleCriteriaTranslator
{
    private ListSampleCriteriaTranslator()
    {
        // Can not be instantiated.
    }

    public final static ListSampleCriteriaDTO translate(final ListSampleCriteria listCriteria)
    {
        final ListSampleCriteriaDTO criteria = new ListSampleCriteriaDTO();
        final String containerIdentifier = listCriteria.getContainerIdentifier();
        if (containerIdentifier != null)
        {
            criteria.setContainerIdentifier(SampleIdentifierFactory.parse(containerIdentifier));
        } else
        {
            criteria.setOwnerIdentifiers(ListSampleCriteriaTranslator
                    .createOwnerIdentifiers(listCriteria));
            criteria.setSampleType(SampleTypeTranslator.translate(listCriteria.getSampleType()));
        }
        return criteria;
    }

    private final static List<SampleOwnerIdentifier> createOwnerIdentifiers(
            final ListSampleCriteria listCriteria)
    {
        final List<SampleOwnerIdentifier> ownerIdentifiers = new ArrayList<SampleOwnerIdentifier>();
        final DatabaseInstanceIdentifier databaseIdentifier = getDatabaseIdentifier(listCriteria);
        if (listCriteria.isIncludeGroup())

        {
            ownerIdentifiers.add(new SampleOwnerIdentifier(new GroupIdentifier(databaseIdentifier,
                    listCriteria.getGroupCode())));
        }
        if (listCriteria.isIncludeInstance())
        {
            ownerIdentifiers.add(new SampleOwnerIdentifier(databaseIdentifier));
        }
        return ownerIdentifiers;
    }

    private final static DatabaseInstanceIdentifier getDatabaseIdentifier(
            final ListSampleCriteria listCriteria)
    {
        final DatabaseInstance databaseInstance =
                listCriteria.getSampleType().getDatabaseInstance();
        return new DatabaseInstanceIdentifier(databaseInstance.getCode());
    }

}
