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

import com.extjs.gxt.ui.client.event.MvcEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.DispatcherListener;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.ShowSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.SampleRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleViewer}.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericSampleViewerTest extends AbstractGWTTestCase
{
    private static final String CISD_ID_PREFIX = "CISD:/CISD/";

    private static final String CONTROL_LAYOUT_EXAMPLE = "CL1";

    private static final String CONTROL_LAYOUT_EXAMPLE_ID = CISD_ID_PREFIX + CONTROL_LAYOUT_EXAMPLE;

    private static final String CONTROL_LAYOUT_EXAMPLE_PERM_ID = "200811050919915-8";

    private static final String CELL_PLATE_EXAMPLE = "3VCP1";

    private static final String CELL_PLATE_EXAMPLE_ID = CISD_ID_PREFIX + CELL_PLATE_EXAMPLE;

    private static final String CELL_PLATE_EXAMPLE_PERM_ID = "200811050946559-983";

    private static final String CELL_PLATE_EXAMPLE_EXPERIMENT_ID = "/CISD/NEMO/EXP1";

    private static final String DIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092158673-1";

    private static final String INDIRECTLY_CONNECTED_DATA_SET_CODE = "20081105092159188-3";

    public final void testShowMasterPlateView()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CONTROL_LAYOUT"));
        remoteConsole.prepare(new ShowSample(CONTROL_LAYOUT_EXAMPLE));
        final CheckSample checkSample = new CheckSample();
        checkSample.property("Sample").asString(CONTROL_LAYOUT_EXAMPLE_ID);
        checkSample.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + CONTROL_LAYOUT_EXAMPLE_PERM_ID + ".*>"
                        + CONTROL_LAYOUT_EXAMPLE_PERM_ID + "</a>.*");
        checkSample.property("Sample Type").asCode("CONTROL_LAYOUT");
        checkSample.property("Registrator").asPerson("Doe, John");
        checkSample.property("Plate Geometry").asProperty("384_WELLS_16X24");
        checkSample.property("Description").asProperty("test control layout");

        final CheckTableCommand componentsTable = checkSample.componentsTable().expectedSize(2);
        final String sampleSubcodeFieldIdent = CommonSampleColDefKind.SUBCODE.id();
        componentsTable.expectedRow(new SampleRow(CONTROL_LAYOUT_EXAMPLE + ":A01", "WELL")
                .identifier("CISD", "CISD").partOfContainer(CONTROL_LAYOUT_EXAMPLE_ID).withCell(
                        sampleSubcodeFieldIdent, "A01"));
        componentsTable.expectedRow(new SampleRow(CONTROL_LAYOUT_EXAMPLE + ":A03", "WELL")
                .identifier("CISD", "CISD").partOfContainer(CONTROL_LAYOUT_EXAMPLE_ID).withCell(
                        sampleSubcodeFieldIdent, "A03"));

        checkSample.dataTable().expectedSize(0);
        remoteConsole.prepare(checkSample);

        launchTest();
    }

    public final void testShowCellPlateView()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));

        final String parentCode1 = "3V-123";

        final CheckSample checkSample = new CheckSample();
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE_ID);
        checkSample.property("Experiment").asString(CELL_PLATE_EXAMPLE_EXPERIMENT_ID);
        checkSample.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + CELL_PLATE_EXAMPLE_PERM_ID + ".*>"
                        + CELL_PLATE_EXAMPLE_PERM_ID + "</a>.*");
        checkSample.property("Sample Type").asCode("CELL_PLATE");
        final CheckTableCommand childrenTable = checkSample.childrenTable().expectedSize(2);
        childrenTable.expectedRow(new SampleRow("3VRP1A", "REINFECT_PLATE").identifier("CISD",
                "CISD").derivedFromAncestors(CELL_PLATE_EXAMPLE_ID));
        childrenTable.expectedRow(new SampleRow("3VRP1B", "REINFECT_PLATE").identifier("CISD",
                "CISD").derivedFromAncestors(CELL_PLATE_EXAMPLE_ID));
        checkSample.property("Invalidation").by(new IValueAssertion<Invalidation>()
            {
                public void assertValue(final Invalidation invalidation)
                {
                    assertEquals("Doe", invalidation.getRegistrator().getLastName());
                    assertEquals("wrong-code", invalidation.getReason());
                }
            });
        checkSample.property("Parent").asCode(parentCode1);
        checkSample.property("Parent").asInvalidEntity();

        final CheckTableCommand dataTable = checkSample.dataTable().expectedSize(1);
        dataTable.expectedRow(new DataSetRow(DIRECTLY_CONNECTED_DATA_SET_CODE).invalid()
                .withFileFormatType("TIFF"));
        dataTable.expectedColumnsNumber(25);
        final String commentColIdent = GridTestUtils.getPropertyColumnIdentifier("COMMENT", false);
        dataTable.expectedColumnHidden(commentColIdent, true);

        remoteConsole.prepare(checkSample);
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                public void execute()
                {
                    // show DataSet
                    TechId wildcard = TechId.createWildcardTechId();
                    final Widget widget =
                            GWTTestUtil
                                    .getWidgetWithID(SampleDataSetBrowser.createGridId(wildcard));
                    assertTrue(widget instanceof Grid<?>);
                    final Grid<?> table = (Grid<?>) widget;
                    GridTestUtils.fireSelectRow(table, CommonExternalDataColDefKind.CODE.id(),
                            DIRECTLY_CONNECTED_DATA_SET_CODE);
                    String showDetailsButtonId =
                            SampleDataSetBrowser.createBrowserId(wildcard)
                                    + SampleDataSetBrowser.SHOW_DETAILS_BUTTON_ID_SUFFIX;

                    // prepare check of request made by Data View section browsing the dataset
                    final Dispatcher dispatcher = Dispatcher.get();
                    DispatcherListener dispatcherListener =
                            createDispatcherListenerForDataView(DIRECTLY_CONNECTED_DATA_SET_CODE);
                    dispatcher.addDispatcherListener(dispatcherListener);
                    GWTTestUtil.clickButtonWithID(showDetailsButtonId);
                    dispatcher.removeDispatcherListener(dispatcherListener);
                }

                /**
                 * Performs a check of the URL before dispatch. Dispatch will be canceled.
                 */
                private DispatcherListener createDispatcherListenerForDataView(
                        final String dataSetCode)
                {
                    return new DispatcherListener()
                        {
                            @Override
                            public void beforeDispatch(MvcEvent mvce)
                            {
                                // TODO 2010-07-09, Piotr Buczek: fix URL check
                                // String url = String.valueOf(mvce.getAppEvent().getData());
                                // assertTrue("Invalid URL: " + url, url
                                // .startsWith("https://localhost:8889/"
                                // + DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/"
                                // + dataSetCode + "?sessionID=test-"));
                                // assertTrue("Invalid URL: " + url, url
                                // .endsWith("mode=simpleHtml&autoResolve=true"));
                                mvce.setCancelled(true);
                            }
                        };
                }
            });

        launchTest();
    }

    public final void testShowIndirectlyConnectedDataSets()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));
        final CheckSample checkSample = new CheckSample();
        // simplified sample properties check
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE_ID);
        checkSample.property("Experiment").asString(CELL_PLATE_EXAMPLE_EXPERIMENT_ID);
        // extended data set table check
        final CheckTableCommand dataTable = checkSample.dataTable().expectedSize(1);
        dataTable.expectedRow(new DataSetRow(DIRECTLY_CONNECTED_DATA_SET_CODE).invalid()
                .withFileFormatType("TIFF").withSample(CELL_PLATE_EXAMPLE_ID).withExperiment(
                        CELL_PLATE_EXAMPLE_EXPERIMENT_ID));
        remoteConsole.prepare(checkSample);
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                public void execute()
                {
                    // show indirectly connected datasets
                    TechId wildcard = TechId.createWildcardTechId();

                    String showOnlyDirectlyConnectedCheckBoxId =
                            GenericSampleViewer.createId(wildcard)
                                    + GenericSampleViewer.SHOW_ONLY_DIRECTLY_CONNECTED_CHECKBOX_ID_POSTFIX;
                    GWTTestUtil.clickCheckBoxWithID(showOnlyDirectlyConnectedCheckBoxId);

                    // check indiectly connected datasets after refresh od dataset table
                    final CheckTableCommand dataTableAfterRefresh =
                            new CheckTableCommand(SampleDataSetBrowser.createGridId(wildcard));
                    dataTableAfterRefresh.expectedSize(6);
                    dataTableAfterRefresh.expectedRow(new DataSetRow(
                            DIRECTLY_CONNECTED_DATA_SET_CODE).invalid().withFileFormatType("TIFF")
                            .withSample(CELL_PLATE_EXAMPLE_ID).withExperiment(
                                    CELL_PLATE_EXAMPLE_EXPERIMENT_ID));
                    dataTableAfterRefresh.expectedRow(new DataSetRow(
                            INDIRECTLY_CONNECTED_DATA_SET_CODE).valid().withLocation(
                            "analysis/result").withSample("").withExperiment(
                            CELL_PLATE_EXAMPLE_EXPERIMENT_ID));
                    // datasets connected to a different experiment
                    final String differentExperimentIdentifier = "/CISD/DEFAULT/EXP-REUSE";
                    assert differentExperimentIdentifier.equals(CELL_PLATE_EXAMPLE_EXPERIMENT_ID) == false;
                    final String[] indirectlyConnectedDataSetLocations =
                        { "xml/result-9", "xml/result-10", "xml/result-11", "xml/result-12" };
                    for (String location : indirectlyConnectedDataSetLocations)
                    {
                        dataTableAfterRefresh.expectedRow(new DataSetRow().valid().withSample("")
                                .withExperiment(differentExperimentIdentifier).withLocation(
                                        location));
                    }
                    remoteConsole.prepare(dataTableAfterRefresh);
                }
            });

        launchTest();
    }
}
