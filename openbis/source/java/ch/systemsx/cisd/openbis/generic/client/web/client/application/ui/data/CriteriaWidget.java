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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.VerticalPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria;

/**
 * Widget for {@link SearchCriteria} management.
 * 
 * @author Izabela Adamczyk
 */
public class CriteriaWidget extends VerticalPanel
{

    private final List<CriterionWidget> criteriaWidgets;

    private final MatchCriteriaRadio matchRadios;

    public CriteriaWidget(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        setLayoutOnChange(true);
        criteriaWidgets = new ArrayList<CriterionWidget>();
        add(matchRadios =
                new MatchCriteriaRadio(viewContext.getMessage(Dict.MATCH_ALL), viewContext
                        .getMessage(Dict.MATCH_ANY)));
        addCriterion(new CriterionWidget(viewContext, this, "first"));
    }

    private void enableRemovalIfOneExists(final boolean enable)
    {
        if (criteriaWidgets.size() == 1)
        {
            criteriaWidgets.get(0).enableRemoveButton(enable);
        }
    }

    /**
     * Adds given {@link CriterionWidget} to the panel.
     */
    void addCriterion(CriterionWidget criterion)
    {
        enableRemovalIfOneExists(true);
        criteriaWidgets.add(criterion);
        add(criterion);
        enableRemovalIfOneExists(false);
        layout();
    }

    /**
     * Removes given {@link CriterionWidget} from the panel, unless it is the only one that left. In
     * this case the state of chosen {@link CriterionWidget} is set to initial value (reset).
     */
    void removeCriterion(CriterionWidget w)
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

    /**
     * @return <b>search criteria</b> extracted from criteria widgets and "match" radio buttons<br>
     *         <b>null</b> if no criteria were selected
     */
    public SearchCriteria tryGetCriteria()
    {

        List<DataSetSearchCriterion> criteria = new ArrayList<DataSetSearchCriterion>();
        for (CriterionWidget cw : criteriaWidgets)
        {
            DataSetSearchCriterion value = cw.tryGetValue();
            if (value != null)
            {
                criteria.add(value);
            }
        }
        if (criteria.size() > 0)
        {
            final SearchCriteria result = new SearchCriteria();
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
        for (CriterionWidget cw : criteriaWidgets)
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
        List<CriterionWidget> list = new ArrayList<CriterionWidget>(criteriaWidgets);
        for (CriterionWidget cw : list)
        {
            removeCriterion(cw);
        }
        layout();
    }
}