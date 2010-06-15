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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.ExperimentPlateLocationsSection;

/**
 * Screening extensions: reviewing panel section for the experiment (batch search for gene
 * locations)
 * 
 * @author Tomasz Pylak
 */
public class ScreeningModule implements IModule
{
    public static final String ID = GenericConstants.ID_PREFIX + "-screening-";

    private final ScreeningViewContext viewContext;

    public ScreeningModule(ScreeningViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    public List<? extends MenuItem> getMenuItems()
    {
        return Collections.emptyList();
    }

    public String getName()
    {
        return viewContext.getMessage(Dict.SCREENING_MODULE_TITLE);
    }

    public void initialize(AsyncCallback<Void> callback)
    {
        callback.onSuccess(null);
    }

    public Collection<? extends SingleSectionPanel> getSections(
            IEntityInformationHolderWithIdentifier entity)
    {
        ArrayList<SingleSectionPanel> sections = new ArrayList<SingleSectionPanel>();
        if (entity.getEntityKind() == EntityKind.EXPERIMENT)
        {
            sections.add(new ExperimentPlateLocationsSection(viewContext, entity));
        }
        return sections;
    }

}
