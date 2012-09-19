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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.page.common.TopBar;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Form;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

public class RegisterSample extends TopBar
{

    @Locate("openbis_select_sample-typeopenbis_sample-registration")
    private DropDown sampleTypes;

    @Lazy
    @Locate("openbis_generic-sample-register_formcode")
    private Text code;

    @SuppressWarnings("unused")
    @Lazy
    @Locate("openbis_generic-sample-register_formexperiment")
    private Text experiment;

    @Lazy
    @Locate("register-sample-space-selection")
    private DropDown spaces;

    @Lazy
    @Locate("openbis_generic-sample-register_formsave-button")
    private Button save;

    @Lazy
    @Locate("registration-panel-openbis_generic-sample-register_form")
    private Form form;

    public void fillWith(Sample sample)
    {
        code.write(sample.getCode());
        spaces.select(sample.getSpace().getCode());

        Map<PropertyType, Object> properties = sample.getProperties();

        for (PropertyType propertyType : properties.keySet())
        {
            Widget w = form.getWidget(propertyType.getLabel());
            PropertyTypeDataType type = propertyType.getDataType();
            String value = properties.get(propertyType).toString();

            switch (type)
            {
                case BOOLEAN:
                    w.handleAs(Checkbox.class).fillWith(value);
                    break;
                case VARCHAR:
                    w.handleAs(Text.class).fillWith(value);
                    break;
                case INTEGER:
                    w.handleAs(Text.class).fillWith(value);
                    break;
                case CONTROLLED_VOCABULARY:
                    w.handleAs(DropDown.class).fillWith(value);
                    break;
                default:
                    throw new IllegalArgumentException(type + " not supported");
            }
        }
    }

    public RegisterSample selectSampleType(SampleType sampleType)
    {
        sampleTypes.select(sampleType.getCode());
        return get(RegisterSample.class);
    }

    public RegisterSample save()
    {
        save.click();
        return get(RegisterSample.class);
    }

    public Collection<String> getProperties()
    {
        Collection<String> properties = new HashSet<String>();
        for (String label : form.getLabels())
        {
            properties.add(label.replace(":", "").replace("*", "").trim());
        }
        return properties;
    }

    @Override
    public String toString()
    {
        return "Register Sample tab with properties " + this.getProperties();
    }
}
