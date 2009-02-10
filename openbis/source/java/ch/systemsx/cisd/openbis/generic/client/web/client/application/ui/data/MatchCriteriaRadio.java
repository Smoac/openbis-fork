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

import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria;

/**
 * @author Izabela Adamczyk
 */
public class MatchCriteriaRadio extends HorizontalPanel
{

    private final Radio orRadio;

    private final Radio andRadio;

    public MatchCriteriaRadio()
    {
        RadioGroup group = new RadioGroup();
        andRadio = new Radio();
        andRadio.setBoxLabel("match all"); // FIXME

        orRadio = new Radio();
        orRadio.setBoxLabel("match any");

        group.add(andRadio);
        group.add(orRadio);

        reset();
        add(group);
    }

    public void reset()
    {
        orRadio.setValue(true);
    }

    SearchCriteria.CriteriaConnection getSelected()
    {
        if (andRadio.getValue() != null && andRadio.getValue().booleanValue() == true)
        {
            return SearchCriteria.CriteriaConnection.AND;
        } else
        {
            return SearchCriteria.CriteriaConnection.OR;
        }
    }
}