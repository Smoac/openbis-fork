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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.COMPONENTS_POSTFIX;
import static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.DATA_POSTFIX;
import static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.ID_PREFIX;
import static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.PROPERTIES_ID_PREFIX;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.SampleGenerationInfoCallback;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CheckSample extends AbstractDefaultTestCommand
{

    private final String identifier;

    @SuppressWarnings("unchecked")
    private Map<String, IValueAssertion> expectedProperties =
            new HashMap<String, IValueAssertion>();
    private CheckTableCommand componentsTableCheck;
    private CheckTableCommand dataTableCheck;

    public CheckSample(String identifierPrefix, String code)
    {
        this.identifier = identifierPrefix + "/" + code;
        addCallbackClass(SampleGenerationInfoCallback.class);
        addCallbackClass(GenericSampleViewer.ListSamplesCallback.class);
        addCallbackClass(GenericSampleViewer.ListExternalDataCallback.class);
    }
    
    public Property property(String name)
    {
        return new Property(name, this);
    }
    
    @SuppressWarnings("unchecked")
    public CheckSample property(String name, IValueAssertion valueAssertion)
    {
        expectedProperties.put(name, valueAssertion);
        return this;
    }
    
    public CheckTableCommand componentsTable()
    {
        componentsTableCheck = new CheckTableCommand(ID_PREFIX + identifier + COMPONENTS_POSTFIX);
        return componentsTableCheck;
    }

    public CheckTableCommand dataTable()
    {
        dataTableCheck = new CheckTableCommand(ID_PREFIX + identifier + DATA_POSTFIX);
        return dataTableCheck;
    }

    public void execute()
    {
        checkProperties();
        if (componentsTableCheck != null)
        {
            componentsTableCheck.execute();
        }
        if (dataTableCheck != null)
        {
            dataTableCheck.execute();
        }
    }

    @SuppressWarnings("unchecked")
    private void checkProperties()
    {
        Widget widget = GWTTestUtil.getWidgetWithID(PROPERTIES_ID_PREFIX + identifier);
        assertEquals(true, widget instanceof WidgetComponent);
        PropertyGrid grid = (PropertyGrid) ((WidgetComponent) widget).getWidget();
        Map<String, ?> actualProperties = grid.getProperties();
        for (Map.Entry<String, IValueAssertion> expectedProperty : expectedProperties.entrySet())
        {
            String key = expectedProperty.getKey();
            assertTrue("Expected property not found: " + key, actualProperties.containsKey(key));
            Object value = actualProperties.get(key);
            expectedProperty.getValue().assertValue(value);
        }
    }

}
