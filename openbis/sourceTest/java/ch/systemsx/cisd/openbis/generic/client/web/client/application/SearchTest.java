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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.MatchingEntityModel.MatchingEntityColumnKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.Login;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.Row;

/**
 * A {@link AbstractGWTTestCase} extension to test searching.
 * 
 * @author Christian Ribeaud
 */
public class SearchTest extends AbstractGWTTestCase
{
    private final static CheckTableCommand createCheckTableCommandForAll()
    {
        final CheckTableCommand checkTableCommand =
                new CheckTableCommand(MatchingEntitiesPanel.GRID_ID,
                        MatchingEntitiesPanel.ListEntitiesCallback.class).expectedSize(2);
        checkTableCommand.expectedRow(createRow("CISD:/MP"));
        checkTableCommand.expectedRow(createRow("CISD:/CISD/EMPTY-MP"));
        return checkTableCommand;
    }

    private static Row createRow(String identifier)
    {
        return new Row().withCell(MatchingEntityColumnKind.IDENTIFIER.id(), identifier);
    }

    private final static CheckTableCommand createCheckTableCommandForExperiment()
    {
        final CheckTableCommand checkTableCommand =
                new CheckTableCommand(MatchingEntitiesPanel.GRID_ID,
                        MatchingEntitiesPanel.ListEntitiesCallback.class).expectedSize(8);
        checkTableCommand.expectedRow(createRow("CISD:/CISD/NEMO/EXP10"));
        checkTableCommand.expectedRow(createRow("CISD:/CISD/NEMO/EXP11"));
        checkTableCommand.expectedRow(createRow("CISD:/CISD/NEMO/EXP1"));
        return checkTableCommand;
    }

    public final void testAllSearch()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("MP"));
        remoteConsole.prepare(createCheckTableCommandForAll());

        launchTest(20000);
    }

    public final void testExperimentSearch()
    {
        remoteConsole.prepare(new Login("test", "a"));
        remoteConsole.prepare(new SearchCommand("Experiment", "John"));
        remoteConsole.prepare(createCheckTableCommandForExperiment());

        launchTest(20000);
    }
}
