/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.TechIdStringIdentifierRecord;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.AbstractListTechIdById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ListSampleTechIdByIdentifier extends AbstractListTechIdById<SampleIdentifier>
{
    private String homeSpaceCodeOrNull; 
    
    public ListSampleTechIdByIdentifier(String homeSpaceCodeOrNull)
    {
        this.homeSpaceCodeOrNull = homeSpaceCodeOrNull;
    }

    @Override
    public Class<SampleIdentifier> getIdClass()
    {
        return SampleIdentifier.class;
    }
    
    @Override
    protected Map<Long, SampleIdentifier> createIdsByTechIdsMap(List<SampleIdentifier> ids)
    {
        Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupedIdentifiers = new HashMap<>();
        for (SampleIdentifier sampleIdentifier : ids)
        {
            ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier sampleIdentifier2 
                = SampleIdentifierFactory.parse(sampleIdentifier.getIdentifier());
            String spaceCode = null;
            if (sampleIdentifier2.isSpaceLevel())
            {
                if (sampleIdentifier2.isInsideHomeSpace())
                {
                    if (homeSpaceCodeOrNull == null)
                    {
                        continue;
                    }
                    spaceCode = homeSpaceCodeOrNull;
                } else
                {
                    spaceCode = CodeConverter.tryToDatabase(sampleIdentifier2.getSpaceLevel().getSpaceCode());
                }
            }
            String sampleSubCode = CodeConverter.tryToDatabase(sampleIdentifier2.getSampleSubCode());
            String containerCode = CodeConverter.tryToDatabase(sampleIdentifier2.tryGetContainerCode());
            SampleIdentifierParts key = new SampleIdentifierParts(spaceCode, null, containerCode);
            Map<String, SampleIdentifier> identifiersByCode = groupedIdentifiers.get(key);
            if (identifiersByCode == null)
            {
                identifiersByCode = new HashMap<>();
                groupedIdentifiers.put(key, identifiersByCode);
            }
            identifiersByCode.put(sampleSubCode, sampleIdentifier);
        }
        
        Map<Long, SampleIdentifier> result = new HashMap<>();
        SampleQuery query = QueryTool.getManagedQuery(SampleQuery.class);
        for (Entry<SampleIdentifierParts, Map<String, SampleIdentifier>> entry : groupedIdentifiers.entrySet())
        {
            SampleIdentifierParts key = entry.getKey();
            Map<String, SampleIdentifier> identifiersByCode = entry.getValue();
            List<TechIdStringIdentifierRecord> records = list(query, key, identifiersByCode.keySet());
            for (TechIdStringIdentifierRecord record : records)
            {
                String sampleCode = record.identifier;
                result.put(record.id, identifiersByCode.get(sampleCode));
            }
        }
        return result;
    }
    
    private List<TechIdStringIdentifierRecord> list(SampleQuery query, SampleIdentifierParts key, Collection<String> codes)
    {
        String[] codesArray = codes.toArray(new String[codes.size()]);
        String spaceCode = key.getSpaceCodeOrNull();
        String containerCode = key.getContainerCodeOrNull();
        if (spaceCode == null)
        {
            if (containerCode == null)
            {
                return query.listSharedSampleTechIdsByCodes(codesArray);
            }
            return query.listSharedSampleTechIdsByContainerCodeAndCodes(containerCode, codesArray);
        }
        if (containerCode == null)
        {
            return query.listSpaceSampleTechIdsByCodes(spaceCode, codesArray);
        }
        return query.listSpaceSampleTechIdsByContainerCodeAndCodes(spaceCode, containerCode, codesArray);
    }

}
