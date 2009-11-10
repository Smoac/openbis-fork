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

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.sample.CommonSampleColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.BrowseDataSet;
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
    static final String GROUP_IDENTIFIER = "CISD:/CISD";

    private static final String CONTROL_LAYOUT_EXAMPLE = "CL1";

    private static final String CONTROL_LAYOUT_EXAMPLE_ID = "CISD:/CISD/CL1";

    private static final String CONTROL_LAYOUT_EXAMPLE_PERM_ID = "200811050919915-8";

    private static final String CELL_PLATE_EXAMPLE = "3VCP1";

    private static final String CELL_PLATE_EXAMPLE_ID = "CISD:/CISD/3VCP1";

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

        launchTest(60000);
    }

    public final void testShowCellPlateView()
    {
        loginAndInvokeAction(ActionMenuKind.SAMPLE_MENU_BROWSE);
        remoteConsole.prepare(new ListSamples("CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));

        final CheckSample checkSample = new CheckSample();
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE_ID);
        checkSample.property("Experiment").asString(CELL_PLATE_EXAMPLE_EXPERIMENT_ID);
        checkSample.property("PermID").matchingPattern(
                ".*<a href=\".*permId=" + CELL_PLATE_EXAMPLE_PERM_ID + ".*>"
                        + CELL_PLATE_EXAMPLE_PERM_ID + "</a>.*");
        checkSample.property("Sample Type").asCode("CELL_PLATE");
        checkSample.property("Children Samples").asGeneratedSamples("3VRP1A [REINFECT_PLATE]",
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

        final CheckTableCommand dataTable = checkSample.dataTable().expectedSize(1);
        dataTable.expectedRow(new DataSetRow(DIRECTLY_CONNECTED_DATA_SET_CODE).invalid()
                .withFileFormatType("TIFF"));
        dataTable.expectedColumnsNumber(23);
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
                    GWTTestUtil.clickButtonWithID(showDetailsButtonId);
                }
            });
        // browse shown dataset
        remoteConsole.prepare(new BrowseDataSet(DIRECTLY_CONNECTED_DATA_SET_CODE));

        launchTest(60000);
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

        launchTest(60000);
    }
}
