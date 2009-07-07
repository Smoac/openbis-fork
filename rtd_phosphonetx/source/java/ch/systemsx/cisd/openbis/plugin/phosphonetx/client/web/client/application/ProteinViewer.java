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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinViewer extends ContentPanel implements IDatabaseModificationObserver
{
    private static final String PREFIX = "protein-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;
    
    static ITabItemFactory createTabItemFactory(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            final ProteinInfo proteinInfo)
    {
        return new ITabItemFactory()
            {
                public String getId()
                {
                    return createWidgetID(proteinInfo.getId());
                }

                public ITabItem create()
                {
                    ProteinViewer viewer =
                            new ProteinViewer(viewContext, proteinInfo.getExperimentID(),
                                    proteinInfo.getId());
                    DatabaseModificationAwareComponent c =
                            new DatabaseModificationAwareComponent(viewer, viewer);
                    return DefaultTabItem.create("Protein: " + proteinInfo.getDescription(), c,
                            viewContext, false);
                }
            };
    }
    
    static String createWidgetID(TechId proteinReferenceID)
    {
        return ID_PREFIX + proteinReferenceID.getId();
    }
    
    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
    private final TechId experimentID;
    private final TechId proteinreferenceID;
    private final String widgetID;
    
    private ProteinViewer(IViewContext<IPhosphoNetXClientServiceAsync> viewContext, TechId experimentID,
            TechId proteinReferenceID)
    {
        widgetID = createWidgetID(proteinReferenceID);
        this.viewContext = viewContext;
        this.experimentID = experimentID;
        this.proteinreferenceID = proteinReferenceID;
        reloadAllData();
    }
    
    private void reloadAllData()
    {
        viewContext.getService().getProteinByExperiment(experimentID, proteinreferenceID,
                new ProteinByExperimentCallback(viewContext, this));
    }

    private void recreateUI(ProteinByExperiment protein)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        properties.put("ID", proteinreferenceID);
        add(propertyGrid);
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[0];
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
    }

    public static final class ProteinByExperimentCallback extends
            AbstractAsyncCallback<ProteinByExperiment>
    {
        private final ProteinViewer viewer;

        private ProteinByExperimentCallback(
                final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
                final ProteinViewer viewer)
        {
            super(viewContext);
            this.viewer = viewer;
        }

        @Override
        protected final void process(final ProteinByExperiment result)
        {
            viewer.recreateUI(result);
        }

    }



}
