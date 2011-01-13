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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.Set;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedUiDescription;

/**
 * {@link TabContent} handled by managed property script.
 * 
 * @author Piotr Buczek
 */
public class ManagedPropertySection extends DisposableTabContent
{
    private final ManagedUiDescription uiDescription;

    public ManagedPropertySection(final String header, IViewContext<?> viewContext,
            IIdHolder ownerId, ManagedUiDescription uiDescription)
    {
        super(header, viewContext, ownerId);
        this.uiDescription = uiDescription;
        setIds(new IDisplayTypeIDGenerator()
            {

                public String createID(String suffix)
                {
                    return createID() + suffix;
                }

                public String createID()
                {
                    return "managed_property_section_" + Format.hyphenize(header);
                }
            });
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        Info.display(getHeading() + " show content", uiDescription.toString());
        // TODO use uiDescription to create tab
        return new IDisposableComponent()
            {

                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                }

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return DatabaseModificationKind.EMPTY_ARRAY;
                }

                public Component getComponent()
                {
                    return new ContentPanel();
                }

                public void dispose()
                {
                }

            };
    }
}
