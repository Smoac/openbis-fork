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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETNewPTAssigments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewETPTAssignment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewPTNewAssigment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author pkupczyk
 */
public class PropertyTypeAssignmentGridAssignmentsHolder
{

    private NewETNewPTAssigments originalAssignments;

    private NewETNewPTAssigments assignments;

    public PropertyTypeAssignmentGridAssignmentsHolder(NewETNewPTAssigments assignments)
    {
        this.assignments = assignments;
        this.originalAssignments = copy(assignments);
    }

    private NewETNewPTAssigments copy(NewETNewPTAssigments original)
    {
        if (original == null)
        {
            return null;
        }

        NewETNewPTAssigments copy = new NewETNewPTAssigments();
        copy.setEntity(original.getEntity());
        if (original.getAssigments() != null)
        {
            List<NewPTNewAssigment> copyAssignments = new ArrayList<NewPTNewAssigment>();
            for (NewPTNewAssigment originalAssignment : original.getAssigments())
            {
                copyAssignments.add(copy(originalAssignment));
            }
            copy.setAssigments(copyAssignments);
        }
        return copy;
    }

    private NewPTNewAssigment copy(NewPTNewAssigment original)
    {
        if (original == null)
        {
            return null;
        }

        NewPTNewAssigment copy = new NewPTNewAssigment();
        copy.setExistingPropertyType(original.isExistingPropertyType());
        copy.setAssignment(copy(original.getAssignment()));
        copy.setPropertyType(copy(original.getPropertyType()));
        return copy;
    }

    private NewETPTAssignment copy(NewETPTAssignment original)
    {
        if (original == null)
        {
            return null;
        }

        NewETPTAssignment copy = new NewETPTAssignment();
        copy.setDefaultValue(original.getDefaultValue());
        copy.setDynamic(original.isDynamic());
        copy.setManaged(original.isManaged());
        copy.setMandatory(original.isMandatory());
        copy.setModificationDate(original.getModificationDate());
        copy.setOrdinal(original.getOrdinal());
        copy.setPropertyTypeCode(original.getPropertyTypeCode());
        copy.setScriptName(original.getScriptName());
        copy.setSection(original.getSection());
        copy.setShownInEditView(original.isShownInEditView());
        copy.setShowRawValue(original.getShowRawValue());
        return copy;
    }

    private PropertyType copy(PropertyType original)
    {
        if (original == null)
        {
            return null;
        }

        PropertyType copy = new PropertyType();
        copy.setCode(original.getCode());
        copy.setDataType(original.getDataType());
        copy.setDescription(original.getDescription());
        copy.setId(original.getId());
        copy.setInternalNamespace(original.isInternalNamespace());
        copy.setLabel(original.getLabel());
        copy.setManagedInternally(original.isManagedInternally());
        copy.setMaterialType(original.getMaterialType());
        copy.setModificationDate(original.getModificationDate());
        copy.setSchema(original.getSchema());
        copy.setSimpleCode(original.getSimpleCode());
        copy.setTransformation(original.getTransformation());
        copy.setVocabulary(original.getVocabulary());
        return copy;
    }

    private boolean equal(NewETNewPTAssigments o1, NewETNewPTAssigments o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        if (equal(o1.getEntity(), o2.getEntity()))
        {
            List<NewPTNewAssigment> a1Assignments = o1.getAssigments();
            List<NewPTNewAssigment> a2Assignments = o2.getAssigments();

            if (a1Assignments == null || a1Assignments.isEmpty() || a2Assignments == null || a2Assignments.isEmpty())
            {
                return (a1Assignments == null || a1Assignments.isEmpty()) && (a2Assignments == null || a2Assignments.isEmpty());
            }

            Iterator<NewPTNewAssigment> a1Iter = a1Assignments.iterator();
            Iterator<NewPTNewAssigment> a2Iter = a2Assignments.iterator();

            while (a1Iter.hasNext() && a2Iter.hasNext())
            {
                if (equal(a1Iter.next(), a2Iter.next()) == false)
                {
                    return false;
                }
            }

            return a1Iter.hasNext() == false && a2Iter.hasNext() == false;

        } else
        {
            return false;
        }
    }

    private boolean equal(NewPTNewAssigment o1, NewPTNewAssigment o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getAssignment(), o2.getAssignment()) && equal(o1.getPropertyType(), o2.getPropertyType());
    }

    private boolean equal(NewETPTAssignment o1, NewETPTAssignment o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getDefaultValue(), o2.getDefaultValue()) && equal(o1.isDynamic(), o2.isDynamic())
                && equal(o1.isManaged(), o2.isManaged()) && equal(o1.isMandatory(), o2.isMandatory())
                && equal(o1.getModificationDate(), o2.getModificationDate()) && equal(o1.getOrdinal(), o2.getOrdinal())
                && equal(o1.getPropertyTypeCode(), o2.getPropertyTypeCode()) && equal(o1.getScriptName(), o2.getScriptName())
                && equal(o1.getSection(), o2.getSection()) && equal(o1.isShownInEditView(), o2.isShownInEditView())
                && equal(o1.getShowRawValue(), o2.getShowRawValue());
    }

    private boolean equal(PropertyType o1, PropertyType o2)
    {
        if (o1 == null || o2 == null)
        {
            return o1 == null && o2 == null;
        }

        return equal(o1.getCode(), o2.getCode()) && equal(o1.getDataType(), o2.getDataType()) && equal(o1.getDescription(), o2.getDescription())
                && equal(o1.getId(), o2.getId()) && equal(o1.isInternalNamespace(), o2.isInternalNamespace()) && equal(o1.getLabel(), o2.getLabel())
                && equal(o1.isManagedInternally(), o2.isManagedInternally()) && equal(o1.getMaterialType(), o2.getMaterialType())
                && equal(o1.getModificationDate(), o2.getModificationDate()) && equal(o1.getSchema(), o2.getSchema())
                && equal(o1.getSimpleCode(), o2.getSimpleCode()) && equal(o1.getTransformation(), o2.getTransformation())
                && equal(o1.getVocabulary(), o2.getVocabulary());
    }

    private boolean equal(Object o1, Object o2)
    {
        if (o1 == null)
        {
            return o2 == null;
        } else
        {
            return o1.equals(o2);
        }
    }

    public NewETNewPTAssigments getAssignments()
    {
        return assignments;
    }

    public boolean isDirty()
    {
        return equal(originalAssignments, assignments) == false;
    }

}
