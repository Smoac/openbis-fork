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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames.CODE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames.LOCATION;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CategoriesBuilder;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.OpenTab;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.ListSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser.ShowSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.IValueAssertion;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericSampleViewer}.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericSampleViewerTest extends AbstractGWTTestCase
{
    private static final String GROUP_IDENTIFIER = "CISD:/CISD";

    private static final String MASTER_PLATE_EXAMPLE = "MP1-MIXED";

    private static final String CELL_PLATE_EXAMPLE = "3VCP1";

    public final void testShowMasterPlateView()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new ListSamples(true, true, "CISD", "MASTER_PLATE"));
        remoteConsole.prepare(new ShowSample(MASTER_PLATE_EXAMPLE));
        CheckSample checkSample = new CheckSample(GROUP_IDENTIFIER, MASTER_PLATE_EXAMPLE);
        checkSample.property("Sample").asString(MASTER_PLATE_EXAMPLE);
        checkSample.property("Sample Type").asCode("MASTER_PLATE");
        checkSample.property("Registrator").asPerson("Doe, John");
        checkSample.property("Generated Samples").asGeneratedSamples("DP1-A [DILUTION_PLATE]",
                "DP1-B [DILUTION_PLATE]");
        checkSample.property("Plate Geometry").asProperty("384_WELLS_16X24");

        CheckTableCommand componentsTable = checkSample.componentsTable().expectedSize(4);
        componentsTable.expectedRow(new Row().withCell(CODE, "A01"));
        componentsTable.expectedRow(new Row().withCell(CODE, "A02"));
        componentsTable.expectedRow(new Row().withCell(CODE, "A03"));
        componentsTable.expectedRow(new Row().withCell(CODE, "B02"));
        checkSample.dataTable().expectedSize(0);
        remoteConsole.prepare(checkSample);

        remoteConsole.finish(60000);
        client.onModuleLoad();

    }

    public final void testShowCellPlateView()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new OpenTab(CategoriesBuilder.CATEGORIES.SAMPLES,
                CategoriesBuilder.MENU_ELEMENTS.LIST));
        remoteConsole.prepare(new ListSamples(true, true, "CISD", "CELL_PLATE"));
        remoteConsole.prepare(new ShowSample(CELL_PLATE_EXAMPLE));
        CheckSample checkSample = new CheckSample(GROUP_IDENTIFIER, CELL_PLATE_EXAMPLE);
        checkSample.property("Sample").asString(CELL_PLATE_EXAMPLE);
        checkSample.property("Sample Type").asCode("CELL_PLATE");
        checkSample.property("Generated Samples").asGeneratedSamples("3VRP1A [REINFECT_PLATE]",
                "3VRP1B [REINFECT_PLATE]");
        checkSample.property("Invalidation").by(new IValueAssertion<Invalidation>()
            {
                public void assertValue(Invalidation invalidation)
                {
                    assertEquals("Doe", invalidation.getRegistrator().getLastName());
                    assertEquals("wrong-code", invalidation.getReason());
                }
            });
        checkSample.property("Dilution Plate").asCode("3V-123");
        checkSample.property("Dilution Plate").asInvalidEntity();
        checkSample.property("Master Plate").asCode("MP001-1");
        checkSample.property("Master Plate").asInvalidEntity();

        checkSample.componentsTable().expectedSize(0);
        CheckTableCommand dataTable = checkSample.dataTable().expectedSize(2);
        dataTable.expectedRow(new Row().withCell(CODE, "20081105092158673-1").withCell(
                FILE_FORMAT_TYPE, "TIFF"));
        dataTable.expectedRow(new Row().withCell(CODE, "20081105092159188-3").withCell(LOCATION,
                "analysis/result"));
        remoteConsole.prepare(checkSample);

        remoteConsole.finish(60000);
        client.onModuleLoad();
    }

}
