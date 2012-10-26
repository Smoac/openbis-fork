/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import ch.systemsx.cisd.openbis.uitest.dsl.Application;
import ch.systemsx.cisd.openbis.uitest.dsl.Ui;
import ch.systemsx.cisd.openbis.uitest.gui.CreatePropertyTypeAssignmentGui;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;

/**
 * @author anttil
 */
@SuppressWarnings("hiding")
public class PropertyTypeAssignmentBuilder implements Builder<PropertyTypeAssignment>
{

    private PropertyType propertyType;

    private SampleType entityType;

    private boolean mandatory;

    private String initialValue;

    private UidGenerator uid;

    public PropertyTypeAssignmentBuilder(UidGenerator uid)
    {
        this.uid = uid;
        this.propertyType = null;
        this.entityType = null;
        this.mandatory = false;
        this.initialValue = "";
    }

    public PropertyTypeAssignmentBuilder with(SampleType sampleType)
    {
        this.entityType = sampleType;
        return this;
    }

    public PropertyTypeAssignmentBuilder with(PropertyType propertyType)
    {
        this.propertyType = propertyType;
        return this;
    }

    public PropertyTypeAssignmentBuilder thatIsMandatory()
    {
        this.mandatory = true;
        return this;
    }

    public PropertyTypeAssignmentBuilder havingInitialValueOf(String value)
    {
        this.initialValue = value;
        return this;
    }

    @Override
    public PropertyTypeAssignment build(Application openbis, Ui ui)
    {
        if (propertyType == null)
        {
            propertyType =
                    new PropertyTypeBuilder(uid, PropertyTypeDataType.BOOLEAN).build(openbis, ui);
        }

        if (entityType == null)
        {
            entityType = new SampleTypeBuilder(uid).build(openbis, ui);
        }

        PropertyTypeAssignment assignment =
                new PropertyTypeAssignmentDsl(propertyType, entityType, mandatory, initialValue);
        if (Ui.WEB.equals(ui))
        {
            assignment = openbis.execute(new CreatePropertyTypeAssignmentGui(assignment));
        } else
        {
            throw new UnsupportedOperationException();
        }
        entityType.getPropertyTypeAssignments().add(assignment);
        return assignment;
    }
}
