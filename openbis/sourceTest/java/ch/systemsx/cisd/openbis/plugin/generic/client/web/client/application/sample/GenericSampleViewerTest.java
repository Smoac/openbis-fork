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

import static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.DATA_POSTFIX;
import static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer.ID_PREFIX;

import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.MvcEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.DispatcherListener;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExternalDataModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ShowSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleViewer}.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericSampleViewerTest extends AbstractGWTTestCase
{
    static final String GROUP_IDENTIFIER = "CISD:/CISD";

    private static final String CONTROL_LAYOUT_EXAMPLE = "CL1";

    private static final String CELL_PLATE_EXAMPLE = "3VCP1";

    public final void testShowMasterPlateView()
    {
        loginAndGotoTab(CategoriesBuilder.MenuCategoryKind.SAMPLES,
                CategoriesBuilder.MenuElementKind.BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CONTROL_LAYOUT"));
        remoteConsole.prepare(new ShowSample(CONTROL_LAYOUT_EXAMPLE));
        final CheckSample checkSample = new CheckSample(GROUP_IDENTIFIER, CONTROL_LAYOUT_EXAMPLE);
        checkSample.property("Sample").asString(CONTROL_LAYOUT_EXAMPLE);
        checkSample.property("Sample Type").asCode("CONTROL_LAYOUT");
        checkSample.property("Registrator").asPerson("Doe, John");
        checkSample.property("Plate Geometry").asProperty("384_WELLS_16X24");
        checkSample.property("Description").asProperty("test control layout");

        final CheckTableCommand componentsTable = checkSample.componentsTable().expectedSize(2);
        String sampleCodeFieldIdent = CommonSampleColDefKind.CODE.id();
        componentsTable.expectedRow(new Row().withCell(sampleCodeFieldIdent, "A01"));
        componentsTable.expectedRow(new Row().withCell(sampleCodeFieldIdent, "A03"));

        checkSample.dataTable().expectedSize(0);
        remoteConsole.prepare(checkSample);

        launchTest(60000);
    }

    public final void testShowCellPlateView()
    {
        loginAndGotoTab(CategoriesBuilder.MenuCategoryKind.SAMPLES,
                CategoriesBuilder.MenuElementKind.BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));
        final CheckSample checkSample = new CheckSample(GROUP_IDENTIFIER, CELL_PLATE_EXAMPLE);
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE);
        checkSample.property("Sample Type").asCode("CELL_PLATE");
        checkSample.property("Generated Samples").asGeneratedSamples("3VRP1A [REINFECT_PLATE]",
                "3VRP1B [REINFECT_PLATE]");
        checkSample.property("Invalidation").by(new IValueAssertion<Invalidation>()
            {
                public void assertValue(final Invalidation invalidation)
                {
                    assertEquals("Doe", invalidation.getRegistrator().getLastName());
                    assertEquals("wrong-code", invalidation.getReason());
                }
            });
        checkSample.property("Parent 1").asCode("3V-123");
        checkSample.property("Parent 1").asInvalidEntity();
        checkSample.property("Parent 2").asCode("MP001-1");
        checkSample.property("Parent 2").asInvalidEntity();

        checkSample.componentsTable().expectedSize(0);
        final CheckTableCommand dataTable = checkSample.dataTable().expectedSize(2);
        dataTable.expectedRow(new DataSetRow("20081105092158673-1").invalid().withFileFormatType(
                "TIFF"));
        dataTable.expectedRow(new DataSetRow("20081105092159188-3").invalid().withLocation(
                "analysis/result"));
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                @Override
                public boolean validOnSucess(List<AsyncCallback<Object>> callbackObjects,
                        Object result)
                {
                    return checkSample.validOnSucess(callbackObjects, result);
                }

                @SuppressWarnings("unchecked")
                public void execute()
                {
                    checkSample.execute();
                    // click on first data set
                    client.removeControllers();
                    DispatcherListener dispatcherListener = createDispatcherListener();
                    Dispatcher dispatcher = Dispatcher.get();
                    dispatcher.addDispatcherListener(dispatcherListener);
                    final Widget widget =
                            GWTTestUtil.getWidgetWithID(ID_PREFIX + GROUP_IDENTIFIER + "/"
                                    + CELL_PLATE_EXAMPLE + DATA_POSTFIX);
                    assertTrue(widget instanceof Grid);
                    final Grid<ExternalDataModel> table = (Grid<ExternalDataModel>) widget;
                    final GridEvent gridEvent = new GridEvent(table);
                    gridEvent.rowIndex = 0;
                    gridEvent.colIndex = 0;
                    table.fireEvent(Events.CellClick, gridEvent);
                    dispatcher.removeDispatcherListener(dispatcherListener);
                }

                private DispatcherListener createDispatcherListener()
                {
                    return new DispatcherListener()
                        {
                            @Override
                            public void beforeDispatch(MvcEvent mvce)
                            {
                                String url = String.valueOf(mvce.appEvent.data);
                                assertTrue("Invalid URL: " + url, url
                                        .startsWith("https://localhost:8889/dataset-download/"
                                                + "20081105092158673-1?sessionID=test-"));
                            }
                        };
                }
        
            });

        launchTest(60000);
    }

}
