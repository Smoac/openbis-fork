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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.entity;

import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Text;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * @author pkupczyk
 */
public class MetaprojectMaterialsSection extends DisposableTabContent
{
    public static final String ID_PREFIX = GenericConstants.ID_PREFIX
            + "metaproject-materials-section_";

    private final TechId metaprojectId;

    public MetaprojectMaterialsSection(IViewContext<?> viewContext, TechId metaprojectId)
    {
        super(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_MATERIALS), viewContext,
                metaprojectId);
        this.metaprojectId = metaprojectId;
        setIds(DisplayTypeIDGenerator.MATERIALS_SECTION);
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        return new IDisposableComponent()
            {

                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {

                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return new DatabaseModificationKind[] {};
                }

                @Override
                public Component getComponent()
                {
                    return new Text("Materials for metaproject: " + metaprojectId);
                }

                @Override
                public void dispose()
                {
                }
            };
    }

}
