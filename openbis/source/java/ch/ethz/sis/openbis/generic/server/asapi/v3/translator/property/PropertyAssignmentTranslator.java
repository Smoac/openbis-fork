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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectToManyRelationTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;

/**
 * @author Franz-Josef Elmer
 */
public abstract class PropertyAssignmentTranslator extends ObjectToManyRelationTranslator<PropertyAssignment, PropertyAssignmentFetchOptions>
{

    @Autowired
    private IPropertyTypeTranslator propertyTypeTranslator;

    @Autowired
    private IPersonTranslator personTranslator;

    protected Map<Long, PropertyAssignment> getAssignments(TranslationContext context,
            Collection<PropertyAssignmentRecord> assignmentRecords,
            PropertyAssignmentFetchOptions assignmentFetchOptions)
    {
        Map<Long, PropertyAssignment> assignments = new HashMap<>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            PropertyAssignment assignment = new PropertyAssignment();
            assignment.setSection(assignmentRecord.section);
            assignment.setOrdinal(assignmentRecord.ordinal);
            assignment.setMandatory(assignmentRecord.is_mandatory);
            assignment.setShowInEditView(assignmentRecord.is_shown_edit);
            assignment.setShowRawValueInForms(assignmentRecord.show_raw_value);
            assignment.setRegistrationDate(assignmentRecord.registration_timestamp);
            assignment.setFetchOptions(assignmentFetchOptions);
            assignments.put(assignmentRecord.id, assignment);
        }

        if (assignmentFetchOptions.getSortBy() != null && (assignmentFetchOptions.getSortBy().getCode() != null
                || assignmentFetchOptions.getSortBy().getLabel() != null))
        {
            assignmentFetchOptions.withPropertyType();
        }

        if (assignmentFetchOptions.hasPropertyType())
        {
            Map<Long, List<PropertyAssignment>> assignmentsByPropertyTypeId = getAssignmentsByPropertyTypeId(assignments, assignmentRecords);
            Map<Long, PropertyType> propertyTypeMap =
                    propertyTypeTranslator.translate(context, assignmentsByPropertyTypeId.keySet(), assignmentFetchOptions.withPropertyType());

            for (Map.Entry<Long, List<PropertyAssignment>> entry : assignmentsByPropertyTypeId.entrySet())
            {
                PropertyType propertyType = propertyTypeMap.get(entry.getKey());
                for (PropertyAssignment assignment : entry.getValue())
                {
                    assignment.setPropertyType(propertyType);
                }
            }
        }

        if (assignmentFetchOptions.hasRegistrator())
        {
            Map<Long, List<PropertyAssignment>> assignmentsByRegistatorId = getAssignmentsByRegistratorId(assignments, assignmentRecords);
            Map<Long, Person> registratorMap =
                    personTranslator.translate(context, assignmentsByRegistatorId.keySet(), assignmentFetchOptions.withRegistrator());

            for (Map.Entry<Long, List<PropertyAssignment>> entry : assignmentsByRegistatorId.entrySet())
            {
                Person registrator = registratorMap.get(entry.getKey());
                for (PropertyAssignment assignment : entry.getValue())
                {
                    assignment.setRegistrator(registrator);
                }
            }
        }

        return assignments;
    }

    private Map<Long, List<PropertyAssignment>> getAssignmentsByPropertyTypeId(Map<Long, PropertyAssignment> assignments,
            Collection<PropertyAssignmentRecord> assignmentRecords)
    {
        Map<Long, List<PropertyAssignment>> map = new HashMap<Long, List<PropertyAssignment>>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            PropertyAssignment assignment = assignments.get(assignmentRecord.id);
            List<PropertyAssignment> list = map.get(assignmentRecord.prty_id);

            if (list == null)
            {
                list = new ArrayList<PropertyAssignment>();
                map.put(assignmentRecord.prty_id, list);
            }

            list.add(assignment);
        }

        return map;
    }

    private Map<Long, List<PropertyAssignment>> getAssignmentsByRegistratorId(Map<Long, PropertyAssignment> assignments,
            Collection<PropertyAssignmentRecord> assignmentRecords)
    {
        Map<Long, List<PropertyAssignment>> map = new HashMap<Long, List<PropertyAssignment>>();

        for (PropertyAssignmentRecord assignmentRecord : assignmentRecords)
        {
            PropertyAssignment assignment = assignments.get(assignmentRecord.id);
            List<PropertyAssignment> list = map.get(assignmentRecord.pers_id_registerer);

            if (list == null)
            {
                list = new ArrayList<PropertyAssignment>();
                map.put(assignmentRecord.pers_id_registerer, list);
            }

            list.add(assignment);
        }

        return map;
    }

    @Override
    protected Collection<PropertyAssignment> createCollection()
    {
        return new ArrayList<>();
    }

}
