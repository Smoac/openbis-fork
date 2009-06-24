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

import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.VocabularyTermColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyTermGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.RemoteConsole;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * A {@link AbstractGWTTestCase} extension to test <i>Vocabulary Browser</i>.
 * 
 * @author Tomasz Pylak
 */
public class VocabularyBrowserTest extends AbstractGWTTestCase
{

    private static final String VOCABULARY_CODE = "USER.ORGANISM";

    public final void testListVocabularies()
    {
        loginAndInvokeAction(ActionMenuKind.VOCABULARY_MENU_BROWSE);
        CheckTableCommand table = new CheckTableCommand(VocabularyGrid.GRID_ID);
        table.expectedColumn(VocabularyColDefKind.CODE.id(), VOCABULARY_CODE);
        remoteConsole.prepare(table.expectedSize(5));

        launchTest(20000);
    }

    public final void testShowTermDetails()
    {
        loginAndInvokeAction(ActionMenuKind.VOCABULARY_MENU_BROWSE);
        showControlledVocabularyTerms(remoteConsole, VOCABULARY_CODE, 5, "FLY", "GORILLA", "HUMAN");

        launchTest(20000);
    }

    public static void showControlledVocabularyTerms(RemoteConsole remoteConsole,
            String vocabularyCode, Integer expectedSize, String... expectedTerms)
    {
        remoteConsole.prepare(new ShowVocabularyTerms(vocabularyCode));
        CheckTableCommand termsTable =
                new CheckTableCommand(VocabularyTermGrid
                        .createGridId(TechId.createWildcardTechId()));
        for (String expectedTerm : expectedTerms)
        {
            expectTermWithCode(termsTable, expectedTerm);
        }
        remoteConsole.prepare(termsTable.expectedSize(expectedSize));
    }

    private static void expectTermWithCode(CheckTableCommand termsTable, String code)
    {
        termsTable.expectedColumn(VocabularyTermColDefKind.CODE.id(), code);
    }

    public static class ShowVocabularyTerms extends AbstractDefaultTestCommand
    {
        private final String vocabularyCode;

        public ShowVocabularyTerms(final String vocabularyCode)
        {
            addCallbackClass(VocabularyGrid.GRID_ID);
            this.vocabularyCode = vocabularyCode;
        }

        @SuppressWarnings("unchecked")
        public void execute()
        {
            final Widget widget = GWTTestUtil.getWidgetWithID(VocabularyGrid.GRID_ID);
            final Grid<BaseEntityModel<Vocabulary>> table =
                    (Grid<BaseEntityModel<Vocabulary>>) widget;
            GridTestUtils.fireSelectRow(table, VocabularyColDefKind.CODE.id(), vocabularyCode);
            GWTTestUtil.clickButtonWithID(VocabularyGrid.SHOW_DETAILS_BUTTON_ID);
        }
    }
}
