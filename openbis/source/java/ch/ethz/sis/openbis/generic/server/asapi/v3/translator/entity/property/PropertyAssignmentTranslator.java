/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataTypeCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.common.ObjectToManyRelationTranslator;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.lemnik.eodsql.QueryTool;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class PropertyAssignmentTranslator extends ObjectToManyRelationTranslator<PropertyAssignment, PropertyAssignmentFetchOptions>
{
    protected Map<Long, PropertyAssignment> getAssignments(Collection<PropertyAssignmentRecord> assignmentRecords)
    {
        Map<Long, PropertyAssignment> assignments = new HashMap<>();
        Map<Long, PropertyAssignment> assignmentsByPropertyTypeId = new HashMap<>();
        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            PropertyAssignment assignment = new PropertyAssignment();
            assignment.setMandatory(assignmentRecord.is_mandatory);
            assignments.put(assignmentRecord.id, assignment);
            assignmentsByPropertyTypeId.put(assignmentRecord.prty_id, assignment);
        }
        PropertyTypeQuery query = QueryTool.getManagedQuery(PropertyTypeQuery.class);
        List<PropertyTypeRecord> propertyTypeRecords = query.getPropertyTypes(new LongOpenHashSet(assignmentsByPropertyTypeId.keySet()));
        for (PropertyTypeRecord propertyTypeRecord : propertyTypeRecords)
        {
            Long propertyTypeId = propertyTypeRecord.id;
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(propertyTypeRecord.code);
            propertyType.setLabel(propertyTypeRecord.label);
            propertyType.setDescription(propertyTypeRecord.description);
            propertyType.setDataTypeCode(DataTypeCode.valueOf(propertyTypeRecord.dataSetTypeCode));
            propertyType.setInternalNameSpace(propertyTypeRecord.is_internal_namespace);
            PropertyAssignment assignment = assignmentsByPropertyTypeId.get(propertyTypeId);
            assignment.setPropertyType(propertyType);
        }
        return assignments;
    }


    @Override
    protected Collection<PropertyAssignment> createCollection()
    {
        return new ArrayList<>();
    }
}
