/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesCriteriaProvider.ListPropertyTypesCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelationshipRole;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.DataSetRelationshipBrowser;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer.DataSetInfoCallback;

/**
 * @author Piotr Buczek
 */

/**
 * A {@link AbstractDefaultTestCommand} extension for showing edit view of a data set that assuming
 * that opening its detail view was triggered before.
 * 
 * @author Piotr Buczek
 */
public class ShowDataSetEditor extends AbstractDefaultTestCommand
{
    private final TechId dataSetId;

    public ShowDataSetEditor()
    {
        this(TechId.createWildcardTechId());
    }

    private ShowDataSetEditor(final TechId dataSetId)
    {
        this.dataSetId = dataSetId;
        addCallbackClass(DataSetInfoCallback.class);
        addCallbackClass(ListPropertyTypesCallback.class);
        final String childrenGridId =
                DataSetRelationshipBrowser.createGridId(dataSetId, DataSetRelationshipRole.PARENT);
        final String parentsGridId =
                DataSetRelationshipBrowser.createGridId(dataSetId, DataSetRelationshipRole.CHILD);
        addCallbackClass(childrenGridId);
        addCallbackClass(parentsGridId);
    }

    public void execute()
    {
        GWTTestUtil.clickButtonWithID(GenericDataSetViewer.createId(dataSetId)
                + GenericDataSetViewer.ID_EDIT_SUFFIX);
    }

}
