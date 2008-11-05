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

import java.util.Collection;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension to check whether a list of sample has been loaded.
 * 
 * @author Izabela Adamczyk
 */
public final class CheckSamplesAndListWithoutServerCall extends AbstractDefaultTestCommand
{
    private final int expectedNumberOfSamples;

    public CheckSamplesAndListWithoutServerCall(final int expectedNumberOfSamples)
    {
        this.expectedNumberOfSamples = expectedNumberOfSamples;
        addCallbackClass(SampleBrowserGrid.ListSamplesCallback.class);
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        checkSamples(expectedNumberOfSamples);

        new ListSamples(false, true, "3V", "MASTER_PLATE").execute();
        checkSamples(0);

    }

    @SuppressWarnings("unchecked")
    private void checkSamples(final int expectedNumber)
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(SampleBrowserGrid.GRID_ID);
        assertTrue(widget instanceof Grid);
        final Grid<SampleModel> table = (Grid<SampleModel>) widget;
        final ListStore<SampleModel> store = table.getStore();
        assertEquals(expectedNumber, store.getCount());
        for (int i = 0; i < store.getCount(); i++)
        {
            SampleModel row = store.getAt(i);
            String expectedCode = "MP" + toInteger(i + 1, 3) + "-1";
            assertCell(expectedCode, row, ModelDataPropertyNames.CODE);
            assertCell("CISD:/" + expectedCode, row, ModelDataPropertyNames.SAMPLE_IDENTIFIER);
            assertCell("CISD", row, ModelDataPropertyNames.INSTANCE);
            assertCell("", row, ModelDataPropertyNames.GROUP);
            assertCell(false, row, ModelDataPropertyNames.IS_INVALID);
            assertCell(true, row, ModelDataPropertyNames.IS_INSTANCE_SAMPLE);
            PropertyType propertyType = new PropertyType();
            propertyType.setInternalNamespace(true);
            propertyType.setSimpleCode("PLATE_GEOMETRY");
            assertCell("384_WELLS_16X24", row, SampleModel.createID(propertyType));
        }
    }
    
    private void assertCell(Object expectedValue, SampleModel row, String id)
    {
        Collection<String> propertyNames = row.getPropertyNames();
        assertTrue("ID '" + id + "' missing in " + propertyNames, propertyNames.contains(id));
        assertEquals("Value for ID '" + id + "'", expectedValue, row.get(id));
    }

    private String toInteger(final int x, final int positions)
    {

        double mult = Math.pow(10, positions - 1);
        final StringBuilder buffer = new StringBuilder();
        while (x < mult)
        {
            buffer.append("0");
            mult /= 10;
        }
        return buffer.append(x).toString();
    }
}
