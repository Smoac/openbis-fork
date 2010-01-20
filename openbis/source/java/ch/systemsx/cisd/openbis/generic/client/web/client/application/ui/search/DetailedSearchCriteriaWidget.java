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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.VerticalPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * Widget for {@link DetailedSearchCriteria} management.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DetailedSearchCriteriaWidget extends VerticalPanel
{

    public static final String FIRST_ID_SUFFIX = "_first";

    private final List<DetailedSearchCriterionWidget> criteriaWidgets;

    private final MatchCriteriaRadio matchRadios;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public DetailedSearchCriteriaWidget(IViewContext<ICommonClientServiceAsync> viewContext,
            EntityKind entityKind)
    {
        this.viewContext = viewContext;
        setLayoutOnChange(true);
        criteriaWidgets = new ArrayList<DetailedSearchCriterionWidget>();
        add(matchRadios =
                new MatchCriteriaRadio(viewContext.getMessage(Dict.MATCH_ALL), viewContext
                        .getMessage(Dict.MATCH_ANY)));
        addCriterion(new DetailedSearchCriterionWidget(viewContext, this, FIRST_ID_SUFFIX,
                entityKind));
    }

    private void enableRemovalIfOneExists(final boolean enable)
    {
        if (criteriaWidgets.size() == 1)
        {
            criteriaWidgets.get(0).enableRemoveButton(enable);
        }
    }

    /**
     * Adds given {@link DetailedSearchCriterionWidget} to the panel.
     */
    void addCriterion(DetailedSearchCriterionWidget criterion)
    {
        enableRemovalIfOneExists(true);
        criteriaWidgets.add(criterion);
        add(criterion);
        enableRemovalIfOneExists(false);
        layout();
    }

    /**
     * Removes given {@link DetailedSearchCriterionWidget} from the panel, unless it is the only one
     * that left. In this case the state of chosen {@link DetailedSearchCriterionWidget} is set to
     * initial value (reset).
     */
    void removeCriterion(DetailedSearchCriterionWidget w)
    {
        if (criteriaWidgets.size() > 1)
        {
            criteriaWidgets.remove(w);
            remove(w);
            enableRemovalIfOneExists(false);
        } else
        {
            w.reset();
        }
    }

    public List<PropertyType> getAvailablePropertyTypes()
    {
        return criteriaWidgets.get(0).getAvailablePropertyTypes();
    }

    /**
     * @return <b>search criteria</b> extracted from criteria widgets and "match" radio buttons<br>
     *         <b>null</b> if no criteria were selected
     */
    public DetailedSearchCriteria tryGetCriteria()
    {

        List<DetailedSearchCriterion> criteria = new ArrayList<DetailedSearchCriterion>();
        for (DetailedSearchCriterionWidget cw : criteriaWidgets)
        {
            DetailedSearchCriterion value = cw.tryGetValue();
            if (value != null)
            {
                criteria.add(value);
            }
        }
        if (criteria.size() > 0)
        {
            final DetailedSearchCriteria result = new DetailedSearchCriteria();
            result.setUseWildcardSearchMode(viewContext.getDisplaySettingsManager()
                    .isUseWildcardSearchMode());
            result.setConnection(matchRadios.getSelected());
            result.setCriteria(criteria);
            return result;
        }
        return null;

    }

    /** description of the search criteria for the user */
    public String getCriteriaDescription()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(matchRadios.getSelectedLabel());
        sb.append(": ");
        boolean first = true;
        for (DetailedSearchCriterionWidget cw : criteriaWidgets)
        {
            String desc = cw.tryGetDescription();
            if (desc != null)
            {
                if (first == false)
                {
                    sb.append(", ");
                } else
                {
                    first = false;
                }
                sb.append(desc);
            }
        }
        return sb.toString();
    }

    /**
     * Resets "match criteria" radio buttons to initial values, removes unnecessary criteria widgets
     * and resets the remaining ones.
     */
    public void reset()
    {
        matchRadios.reset();
        List<DetailedSearchCriterionWidget> list =
                new ArrayList<DetailedSearchCriterionWidget>(criteriaWidgets);
        for (DetailedSearchCriterionWidget cw : list)
        {
            removeCriterion(cw);
        }
        layout();
    }

    /**
     * Set the initial search string to the argument. This should be called after creation but
     * before the user has had a chance to use the window, otherwise user input may be overwritten.
     */
    public void setInitialSearchCriterion(DetailedSearchField initialField,
            String initialSearchString)
    {
        DetailedSearchCriterionWidget widget = criteriaWidgets.get(0);
        widget.setSearchCriterion(initialField, initialSearchString);
    }

}
