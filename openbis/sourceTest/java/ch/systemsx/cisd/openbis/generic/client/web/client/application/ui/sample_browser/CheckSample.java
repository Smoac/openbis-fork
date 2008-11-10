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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericSampleViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericSampleViewer.SampleGenerationInfoCallback;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CheckSample extends AbstractDefaultTestCommand
{

    private final String identifier;
    private final String code;

    public CheckSample(String identifierPrefix, String code)
    {
        this.code = code;
        this.identifier = identifierPrefix + "/" + code;
        addCallbackClass(SampleGenerationInfoCallback.class);
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        Widget widget = GWTTestUtil.getWidgetWithID(GenericSampleViewer.PROPERTIES_ID_PREFIX + identifier);
        assertEquals(true, widget instanceof WidgetComponent);
        PropertyGrid grid = (PropertyGrid) ((WidgetComponent) widget).getWidget();
        assertEquals(code, grid.getText(0, 1));
        assertEquals("MASTER_PLATE", grid.getText(1, 1));
        assertEquals("Doe, John", grid.getText(2, 1));
        assertEquals("DP1-A [DILUTION_PLATE]\nDP1-B [DILUTION_PLATE]", grid.getText(4, 1)
                .replaceAll("\r\n", "\n"));
        assertEquals("Plate Geometry", grid.getText(5, 0));
        assertEquals("384_WELLS_16X24", grid.getText(5, 1));
        assertEquals(6, grid.getRowCount());
    }

}
