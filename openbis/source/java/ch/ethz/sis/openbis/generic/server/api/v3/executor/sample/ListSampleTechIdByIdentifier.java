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
        Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupedIdentifiers = groupIdentifiers(ids);
        
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

    private Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupIdentifiers(List<SampleIdentifier> ids)
    {
        Map<SampleIdentifierParts, Map<String, SampleIdentifier>> groupedIdentifiers = new HashMap<>();
        for (SampleIdentifier sampleIdentifier : ids)
        {
            FullSampleIdentifier fullSampleIdentifier = new FullSampleIdentifier(sampleIdentifier.getIdentifier(), 
                    homeSpaceCodeOrNull);
            
            SampleIdentifierParts key = fullSampleIdentifier.getParts();
            Map<String, SampleIdentifier> identifiersByCode = groupedIdentifiers.get(key);
            if (identifiersByCode == null)
            {
                identifiersByCode = new HashMap<>();
                groupedIdentifiers.put(key, identifiersByCode);
            }
            identifiersByCode.put(fullSampleIdentifier.getSampleCode(), sampleIdentifier);
        }
        return groupedIdentifiers;
    }

    private List<TechIdStringIdentifierRecord> list(SampleQuery query, SampleIdentifierParts key, Collection<String> codes)
    {
        String[] codesArray = codes.toArray(new String[codes.size()]);
        String spaceCode = key.getSpaceCodeOrNull();
        String projectCode = key.getProjectCodeOrNull();
        String containerCode = key.getContainerCodeOrNull();
        if (spaceCode == null)
        {
            if (containerCode == null)
            {
                return query.listSharedSampleTechIdsByCodes(codesArray);
            }
            return query.listSharedSampleTechIdsByContainerCodeAndCodes(containerCode, codesArray);
        }
        if (projectCode == null)
        {
            if (containerCode == null)
            {
                return query.listSpaceSampleTechIdsByCodes(spaceCode, codesArray);
            }
            return query.listSpaceSampleTechIdsByContainerCodeAndCodes(spaceCode, containerCode, codesArray);
        }
        if (containerCode == null)
        {
            return query.listProjectSampleTechIdsByCodes(spaceCode, projectCode, codesArray);
        }
        return query.listProjectSampleTechIdsByContainerCodeAndCodes(spaceCode, projectCode, containerCode, codesArray);
    }

}
