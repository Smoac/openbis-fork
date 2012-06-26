/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DataSetKindModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author Pawel Glyzewski
 */
public class DataSetKindSelectionWidget extends DropDownList<DataSetKindModel, DataSetKind>
{
    public static final String SUFFIX = "data_set_kind";

    public DataSetKindSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.DATA_SET_KIND, ModelDataPropertyNames.CODE,
                "Data Set Kind", "Data Set Kinds");
        setAutoSelectFirst(true);
    }

    @Override
    protected List<DataSetKindModel> convertItems(List<DataSetKind> result)
    {
        return DataSetKindModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<DataSetKind>> callback)
    {
        ((ListItemsCallback) callback).process(Arrays.asList(DataSetKind.values()));
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.EMPTY_ARRAY;
    }
}
