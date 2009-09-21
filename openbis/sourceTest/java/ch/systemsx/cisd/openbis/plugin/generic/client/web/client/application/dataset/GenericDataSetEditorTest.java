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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.ShowDataSetEditor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns.DataSetRow;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search.FillSearchCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractGWTTestCase;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CheckTableCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.FailureExpectation;
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

    private static final String DS_WITH_MANY_PARENTS_CODE = "20081105092259000-9";

    public final void testEditDataSetComment()
    {
        final String modifiedDataSetCode = DS_WITH_ONE_PARENT_CODE;

        prepareShowDataSetEditor(modifiedDataSetCode);

        final String newCommentColumnValue = "new comment";
        remoteConsole.prepare(new FillDataSetEditForm().addProperty(new PropertyField("comment",
                newCommentColumnValue)));
        remoteConsole.prepare(new ShowUpdatedDataSet());

        final CheckDataSet checkDataSet = new CheckDataSet();
        checkDataSet.property("Comment").asProperty(newCommentColumnValue);
        remoteConsole.prepare(checkDataSet);

        launchTest(40 * SECOND);
    }

    public final void testEditDataSetAddParent()
    {
        final String modifiedDataSetCode = DS_WITH_ONE_PARENT_CODE;

        prepareShowDataSetEditor(modifiedDataSetCode);

        remoteConsole.prepare(new FillDataSetEditForm()
                .modifyParents(DS_WITH_ONE_PARENT_PARENTS_CODE + ","
                        + DS_WITH_ONE_PARENT_NEW_PARENTS_CODE));
        remoteConsole.prepare(new ShowUpdatedDataSet());

        final CheckTableCommand checkParents = new CheckDataSet().parentsTable().expectedSize(2);
        checkParents.expectedRow(new DataSetRow(DS_WITH_ONE_PARENT_PARENTS_CODE));
        checkParents.expectedRow(new DataSetRow(DS_WITH_ONE_PARENT_NEW_PARENTS_CODE));
        remoteConsole.prepare(checkParents);

        launchTest(20 * SECOND);
    }

    public final void testEditDataSetRemoveAllParents()
    {
        final String modifiedDataSetCode = DS_WITH_ONE_PARENT_CODE;

        prepareShowDataSetEditor(modifiedDataSetCode);

        remoteConsole.prepare(new FillDataSetEditForm().modifyParents(""));
        remoteConsole.prepare(new ShowUpdatedDataSet());
        final CheckTableCommand checkParents = new CheckDataSet().parentsTable().expectedSize(0);
        remoteConsole.prepare(checkParents);
        launchTest(20 * SECOND);
    }

    public final void testEditDataSetParents()
    {
        final String modifiedDataSetCode = DS_WITH_MANY_PARENTS_CODE;
        // Remove three parents and add one in one go. Old parent codes:
        // 20081105092158673-1, 20081105092159111-1, 20081105092159222-2, 20081105092159333-3
        final String newParentCode = "20081105092159188-3";
        final String oldParentCode = "20081105092159111-1";

        prepareShowDataSetEditor(modifiedDataSetCode);

        remoteConsole.prepare(new FillDataSetEditForm().modifyParents(newParentCode + ", "
                + oldParentCode));
        remoteConsole.prepare(new ShowUpdatedDataSet());
        final CheckTableCommand checkParents = new CheckDataSet().parentsTable().expectedSize(2);
        checkParents.expectedRow(new DataSetRow(newParentCode));
        checkParents.expectedRow(new DataSetRow(oldParentCode));
        remoteConsole.prepare(checkParents);
        launchTest(20 * SECOND);
    }

    public final void testEditDataSetParentsFailWithCycleRelationships()
    {
        final String modifiedDataSetCode = DS_WITH_MANY_PARENTS_CODE;
        final String descendantCode = "20081105092359990-2";

        prepareShowDataSetEditor(modifiedDataSetCode);

        remoteConsole.prepare(new FillDataSetEditForm().modifyParents(descendantCode));
        FailureExpectation failureExpectation =
                new FailureExpectation(GenericDataSetEditForm.UpdateDataSetCallback.class)
                        .with("Data Set '" + modifiedDataSetCode + "' is an ancestor of Data Set '"
                                + descendantCode
                                + "' and cannot be at the same time set as its child.");
        remoteConsole.prepare(failureExpectation);

        launchTest(20 * SECOND);
    }

    // This test is needed among system tests because a deferred trigger is executed
    // just before commit and another test in DAO layer uses manual commit that is unnatural.
    public final void testAddDataSetParentFailWithDeferredTriggerError()
    {
        final String modifiedDataSetCode = DS_WITH_ONE_PARENT_PARENTS_CODE;
        final String addedParentCode = DS_WITH_ONE_PARENT_NEW_PARENTS_CODE;

        prepareShowDataSetEditor(modifiedDataSetCode);

        remoteConsole.prepare(new FillDataSetEditForm().modifyParents(addedParentCode));
        FailureExpectation failureExpectation =
                new FailureExpectation(GenericDataSetEditForm.UpdateDataSetCallback.class)
                        .with("ERROR: Insert/Update of Data Set (Code: '"
                                + modifiedDataSetCode
                                + "') failed because it cannot be connected with a Sample and a parent Data Set at the same time.'");
        remoteConsole.prepare(failureExpectation);

        launchTest(20 * SECOND);
    }

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
