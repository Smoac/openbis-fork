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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Radio;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchFieldKind;

/**
 * A {@link AbstractDefaultTestCommand} extension for filling {@link CriteriaWidget}.
 * 
 * @author Izabela Adamczyk
 */
public final class FillSearchCriteria extends AbstractDefaultTestCommand
{
    private final List<Criterion> criteria;

    private boolean criteriaDefined;

    private Boolean matchAll;

    public FillSearchCriteria()
    {
        criteria = new ArrayList<Criterion>();
        criteriaDefined = false;
        addCallbackClass(DataSetSearchFieldsSelectionWidget.ListPropertyTypesCallback.class);
    }

    public FillSearchCriteria matchAll()
    {
        matchAll = true;
        return this;
    }

    public FillSearchCriteria matchAny()
    {
        matchAll = false;
        return this;
    }

    public FillSearchCriteria addSimpleCriterion(DataSetSearchFieldKind name, String value)
    {
        return addCriterion(name, null, value);
    }

    public FillSearchCriteria addSamplePropertyCriterion(String name, String value)
    {
        return addCriterion(DataSetSearchFieldKind.SAMPLE_PROPERTY, name, value);
    }

    public FillSearchCriteria addExperimentPropertyCriterion(String name, String value)
    {
        return addCriterion(DataSetSearchFieldKind.EXPERIMENT_PROPERTY, name, value);
    }

    private FillSearchCriteria addCriterion(DataSetSearchFieldKind fieldKind,
            String propertyOrNull, String value)
    {
        final String field =
                fieldKind.description()
                        + (propertyOrNull == null ? "" : " '" + propertyOrNull + "'");
        criteria.add(new Criterion(field, value));
        criteriaDefined = true;
        return this;
    }

    public final void execute()
    {
        assert criteriaDefined : "At least one search criterion should be specified";
        if (matchAll != null)
        {
            if (matchAll)

            {
                final Radio andRadio =
                        (Radio) GWTTestUtil.getWidgetWithID(MatchCriteriaRadio.AND_RADIO_ID);
                andRadio.setValue(true);
            } else

            {
                final Radio anyRadio =
                        (Radio) GWTTestUtil.getWidgetWithID(MatchCriteriaRadio.OR_RADIO_ID);
                anyRadio.setValue(true);
            }
        }
        for (int i = 0; i < criteria.size(); i++)
        {
            final DataSetSearchFieldsSelectionWidget selector =
                    (DataSetSearchFieldsSelectionWidget) GWTTestUtil
                            .getWidgetWithID(DataSetSearchFieldsSelectionWidget.ID
                                    + DataSetSearchFieldsSelectionWidget.SUFFIX
                                    + CriteriaWidget.FIRST_ID_SUFFIX + getSuffix(i));
            GWTUtils.setSelectedItem(selector, ModelDataPropertyNames.CODE, criteria.get(i)
                    .getName());
            GWTTestUtil.setTextFieldValue(CriterionWidget.VALUE_FIELD_ID_PREFIX
                    + CriteriaWidget.FIRST_ID_SUFFIX + getSuffix(i), criteria.get(i).getValue());
            GWTTestUtil.clickButtonWithID(CriterionWidget.ADD_BUTTON_ID_PREFIX
                    + CriteriaWidget.FIRST_ID_SUFFIX);
        }
        GWTTestUtil.clickButtonWithID(DataSetSearchWindow.SEARCH_BUTTON_ID);
    }

    private final String getSuffix(int i)
    {
        if (i == 0)
        {
            return "";
        } else
        {
            return "_" + (i - 1);
        }
    }

}
