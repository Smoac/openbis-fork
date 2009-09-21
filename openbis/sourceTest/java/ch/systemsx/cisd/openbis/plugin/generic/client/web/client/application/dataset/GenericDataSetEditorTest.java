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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSetEditor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractGWTTestCase} extension to test {@link GenericDataSetEditForm}.
 * 
 * @author Piotr Buczek
 */
public class GenericDataSetEditorTest extends AbstractGWTTestCase
{

    private static final String DS_WITH_ONE_PARENT_CODE = "20081105092159188-3";

    private static final String DS_WITH_ONE_PARENT_PARENTS_CODE = "20081105092158673-1";

    private static final String DS_WITH_ONE_PARENT_NEW_PARENTS_CODE = "20081105092159111-1";

    public final void testEditDataSetComment()
    {
        prepareShowDataSetEditor(DS_WITH_ONE_PARENT_CODE);

        final String newCommentColumnValue = "new comment";
        remoteConsole.prepare(new FillDataSetEditForm().addProperty(new PropertyField("comment",
                newCommentColumnValue)));
        final AbstractDefaultTestCommand showUpdatedDataSet = new ShowUpdatedDataSet();
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                @Override
                public List<AbstractAsyncCallback<Object>> tryValidOnSucess(
                        List<AbstractAsyncCallback<Object>> callbackObjects, Object result)
                {
                    return showUpdatedDataSet.tryValidOnSucess(callbackObjects, result);
                }

                public void execute()
                {
                    showUpdatedDataSet.execute();

                    final CheckDataSet checkDataSet = new CheckDataSet();
                    checkDataSet.property("Comment").asProperty(newCommentColumnValue);
                    remoteConsole.prepare(checkDataSet);
                }
            });
        launchTest(20 * SECOND);
    }

    public final void testEditDataSetAddParent()
    {
        prepareShowDataSetEditor(DS_WITH_ONE_PARENT_CODE);

        remoteConsole.prepare(new FillDataSetEditForm()
                .modifyParents(DS_WITH_ONE_PARENT_PARENTS_CODE + ","
                        + DS_WITH_ONE_PARENT_NEW_PARENTS_CODE));

        final AbstractDefaultTestCommand showUpdatedDataSet = new ShowUpdatedDataSet();
        // remoteConsole.prepare(new ShowUpdatedDataSet());
        remoteConsole.prepare(new AbstractDefaultTestCommand()
            {
                @Override
                public List<AbstractAsyncCallback<Object>> tryValidOnSucess(
                        List<AbstractAsyncCallback<Object>> callbackObjects, Object result)
                {
                    return showUpdatedDataSet.tryValidOnSucess(callbackObjects, result);
                }

                public void execute()
                {
                    showUpdatedDataSet.execute();
                    final CheckTableCommand checkParents =
                            new CheckDataSet().parentsTable().expectedSize(2);
                    // checkParents.expectedRow(new DataSetRow(DS_WITH_ONE_PARENT_PARENTS_CODE));
                    // checkParents.expectedRow(new
                    // DataSetRow(DS_WITH_ONE_PARENT_NEW_PARENTS_CODE));

                    remoteConsole.prepare(checkParents);
                }
            });

        launchTest(20 * SECOND);
    }

    // public final void testEditDataSetRemoveAllParents()
    // {
    // prepareShowDataSetEditor(DS_WITH_ONE_PARENT_CODE);
    //
    // remoteConsole.prepare(new FillDataSetEditForm().modifyParents(""));
    // remoteConsole.prepare(new ShowUpdatedDataSet());
    // final CheckTableCommand checkParents = new CheckDataSet().parentsTable().expectedSize(0);
    // remoteConsole.prepare(checkParents);
    // launchTest(20 * SECOND);
    // }

    private class ShowUpdatedDataSet extends AbstractDefaultTestCommand
    {
        public ShowUpdatedDataSet()
        {
            addCallbackClass(GenericDataSetEditForm.UpdateDataSetCallback.class);
        }

        public void execute()
        {
            String tabItemId =
                    GenericDataSetViewer.createId(TechId.createWildcardTechId())
                            + MainTabPanel.TAB_SUFFIX;
            GWTTestUtil.selectTabItemWithId(MainTabPanel.ID, tabItemId);
        }
    }

    //
    // public final void testEditExperimentProject()
    // {
    // String oldProject = NEMO;
    // String newProject = DEFAULT;
    // String experiment = EXP11;
    // prepareShowExperimentEditor(CISD, oldProject, SIRNA_HCS, experiment);
    // remoteConsole.prepare(new FillExperimentEditForm().changeProject(identifier(CISD,
    // newProject)));
    // remoteConsole.prepare(new ListExperiments(withGroup(CISD, newProject), SIRNA_HCS,
    // GenericExperimentEditForm.UpdateExperimentCallback.class));
    // CheckExperimentTable table = new CheckExperimentTable();
    // table.expectedRow(new ExperimentRow(experiment));
    // remoteConsole.prepare(table);
    // launchTest(20 * SECOND);
    // }

    private void prepareShowDataSetEditor(String dataSetCode)
    {
        // Open data set editor by simulating 3 steps:
        // - search for data set with given code,
        // - click on a result row and then on 'show details' button,
        // - click on edit button in data set detail view.
        // It could be also done without 3rd step and with clicking on 'edit' button
        // in 2nd step instead.
        loginAndInvokeAction(ActionMenuKind.DATA_SET_MENU_SEARCH);
        remoteConsole.prepare(FillSearchCriteria.searchForDataSetWithCode(dataSetCode));
        remoteConsole.prepare(new ShowDataSet(dataSetCode));
        remoteConsole.prepare(new ShowDataSetEditor());
    }

}
