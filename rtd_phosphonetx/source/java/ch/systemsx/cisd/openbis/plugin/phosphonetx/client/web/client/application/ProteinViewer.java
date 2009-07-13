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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.BrowserSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProteinViewer extends
        AbstractViewer<IPhosphoNetXClientServiceAsync, IEntityInformationHolder> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "protein-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;
    
    static ITabItemFactory createTabItemFactory(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            final Experiment experimentOrNull, final ProteinInfo proteinInfo)
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
                            new ProteinViewer(viewContext, experimentOrNull, proteinInfo.getId());
                    DatabaseModificationAwareComponent c =
                            new DatabaseModificationAwareComponent(viewer, viewer);
                    String description = StringUtils.abbreviate(proteinInfo.getDescription(), 30);
                    String identifier =
                            experimentOrNull == null ? "?" : experimentOrNull.getIdentifier();
                    return DefaultTabItem.create(viewContext.getMessage(
                            Dict.PROTEIN_IN_EXPERIMENT_TAB_LABEL, description, identifier), c,
                            viewContext, false);
                }
            };
    }
    
    static String createWidgetID(TechId proteinReferenceID)
    {
        return ID_PREFIX + proteinReferenceID.getId();
    }
    
    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
    private final Experiment experimentOrNull;
    private final TechId experimentID;
    private final TechId proteinreferenceID;
    
    private ProteinViewer(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experimentOrNull, TechId proteinReferenceID)
    {
        super(viewContext, "", createWidgetID(proteinReferenceID), false);
        this.viewContext = viewContext;
        this.experimentOrNull = experimentOrNull;
        this.experimentID = TechId.create(experimentOrNull);
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
        setLayout(new BorderLayout());
        removeAll();
        setScrollMode(Scroll.AUTO);
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        if (experimentOrNull != null)
        {
            String identifier = experimentOrNull.getIdentifier();
            properties.put(viewContext.getMessage(Dict.EXPERIMENT_LABEL), identifier);
        }
        properties.put(viewContext.getMessage(Dict.UNIPROT_ID), protein.getUniprotID());
        properties.put(viewContext.getMessage(Dict.PROTEIN_DESCRIPTION), protein.getDescription());
        PropertyGrid propertyGrid = new PropertyGrid(viewContext, properties.size());
        propertyGrid.setProperties(properties);
        ContentPanel contentPanel = new ContentPanel();
        contentPanel.add(propertyGrid);
        BorderLayoutData layoutData = createBorderLayoutData(LayoutRegion.WEST);
        layoutData.setSize(400);
        add(contentPanel, layoutData);
        add(new BrowserSectionPanel(viewContext.getMessage(Dict.SEQUENCES), ProteinSequenceGrid
                .create(viewContext, proteinreferenceID)), createRightBorderLayoutData());
        layout();
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
