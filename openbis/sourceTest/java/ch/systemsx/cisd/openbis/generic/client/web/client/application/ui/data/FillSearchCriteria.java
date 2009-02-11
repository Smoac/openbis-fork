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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for filling {@link CriteriaWidget}.
 * 
 * @author Izabela Adamczyk
 */
public final class FillSearchCriteria extends AbstractDefaultTestCommand
{
    private final String value;

    private final String field;

    public FillSearchCriteria(final String field, final String value)
    {
        assert value != null : "Unspecified value.";
        assert field != null : "Unspecified field.";

        this.value = value;
        this.field = field;
        addCallbackClass(DataSetSearchFieldsSelectionWidget.ListPropertyTypesCallback.class);
    }

    public final void execute()
    {
        GWTTestUtil.setTextFieldValue(CriterionWidget.ID + CriteriaWidget.FIRST_ID_SUFFIX, value);
        final DataSetSearchFieldsSelectionWidget selector =
                (DataSetSearchFieldsSelectionWidget) GWTTestUtil
                        .getWidgetWithID(DataSetSearchFieldsSelectionWidget.ID
                                + DataSetSearchFieldsSelectionWidget.SUFFIX
                                + CriteriaWidget.FIRST_ID_SUFFIX);
        GWTUtils.setSelectedItem(selector, ModelDataPropertyNames.CODE, field);

        GWTTestUtil.clickButtonWithID(DataSetSearchHitGrid.DataSetSearchWindow.SEARCH_BUTTON_ID);
    }

}
