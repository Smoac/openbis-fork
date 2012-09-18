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

package ch.systemsx.cisd.openbis.uitest.page.tab;

import ch.systemsx.cisd.openbis.uitest.infra.Locate;
import ch.systemsx.cisd.openbis.uitest.infra.NotAlwaysPresent;
import ch.systemsx.cisd.openbis.uitest.page.NavigationPage;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

public class AssignSamplePropertyType extends NavigationPage
{

    @Locate("openbis_select_property-typeopenbis_property-type-assignment_SAMPLEproperty_type")
    private DropDown propertyType;

    @Locate("openbis_select_sample-typeopenbis_property-type-assignment_sample_type")
    private DropDown sampleType;

    @Locate("openbis_property-type-assignment_SAMPLEmandatory_checkbox")
    private Checkbox mandatory;

    @NotAlwaysPresent
    @Locate("openbis_property-type-assignment_SAMPLEdefault_value")
    private Widget initialValue;

    @Locate("openbis_property-type-assignment_SAMPLEsave-button")
    private Button save;

    public void fillWith(PropertyTypeAssignment assignment)
    {
        propertyType.select(assignment.getPropertyType().getLabel());
        sampleType.select(assignment.getSampleType().getCode());
        mandatory.set(assignment.isMandatory());

        if (assignment.getInitialValue() != null && assignment.getInitialValue().length() > 0)
        {
            PropertyTypeDataType type = assignment.getPropertyType().getDataType();

            if (type.equals(PropertyTypeDataType.BOOLEAN))
            {
                initialValue.handleAs(Checkbox.class).set(
                        assignment.getInitialValue().equals("true"));
            } else if (type.equals(PropertyTypeDataType.VARCHAR))
            {
                initialValue.handleAs(Text.class).write(assignment.getInitialValue());
            } else if (type.equals(PropertyTypeDataType.INTEGER))
            {
                initialValue.handleAs(Text.class).write(assignment.getInitialValue());
            } else if (type.equals(PropertyTypeDataType.CONTROLLED_VOCABULARY))
            {
                initialValue.handleAs(DropDown.class).select(assignment.getInitialValue());
            } else
            {
                throw new IllegalArgumentException("Type " + type + " not supported");
            }
        }
    }

    public AssignSamplePropertyType save()
    {
        this.save.click();
        return get(AssignSamplePropertyType.class);
    }
}
