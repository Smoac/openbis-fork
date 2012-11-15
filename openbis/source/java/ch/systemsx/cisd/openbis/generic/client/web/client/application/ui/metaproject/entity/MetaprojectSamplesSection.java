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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

/**
 * @author pkupczyk
 */
public class MetaprojectSamplesSection extends DisposableTabContent
{
    public static final String ID_PREFIX = GenericConstants.ID_PREFIX
            + "metaproject-samples-section_";

    private final TechId metaprojectId;

    public MetaprojectSamplesSection(IViewContext<?> viewContext, TechId metaprojectId)
    {
        super(viewContext.getMessage(Dict.METAPROJECT_ENTITIES_SAMPLES), viewContext, metaprojectId);
        this.metaprojectId = metaprojectId;
        setIds(DisplayTypeIDGenerator.SAMPLES_SECTION);
    }

    private static String createBrowserId(TechId metaprojectId)
    {
        return ID_PREFIX + metaprojectId + "-browser";
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        IDisposableComponent disposableComponent =
                SampleBrowserGrid.createGridForMetaprojectSamples(
                        viewContext.getCommonViewContext(), metaprojectId,
                        createBrowserId(metaprojectId));
        return disposableComponent;
    }

}
