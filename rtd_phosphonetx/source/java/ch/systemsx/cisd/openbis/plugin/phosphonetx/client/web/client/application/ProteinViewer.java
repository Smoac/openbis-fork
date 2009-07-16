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
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinDetails;

/**
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

    private final TechId proteinReferenceID;

    private ProteinViewer(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experimentOrNull, TechId proteinReferenceID)
    {
        super(viewContext, "", createWidgetID(proteinReferenceID), false);
        this.viewContext = viewContext;
        this.experimentOrNull = experimentOrNull;
        this.proteinReferenceID = proteinReferenceID;
        reloadAllData();
    }

    private void reloadAllData()
    {
        if (experimentOrNull != null)
        {
            viewContext.getService().getProteinByExperiment(new TechId(experimentOrNull.getId()),
                    proteinReferenceID, new ProteinByExperimentCallback(viewContext, this));
        }
    }

    private void recreateUI(ProteinByExperiment protein)
    {
        fillDebugData(protein);

        setLayout(new BorderLayout());
        removeAll();
        setScrollMode(Scroll.AUTO);
        ContentPanel propertyPanel = createPropertyPanel(protein);

        if (protein.getDetails() == null)
        {
            recreateUIWithDatasetTable(protein, propertyPanel);
        } else
        {
            // TODO 2009-07-16, Tomasz Pylak: write sample viewer
            BorderLayoutData layoutData = createBorderLayoutData(LayoutRegion.CENTER);
            add(propertyPanel, layoutData);
        }
    }

    // TODO 2009-07-17, Tomasz Pylak: remove me!!!!!!!!!!!!!!!!!!!!!!!!!
    private static void fillDebugData(ProteinByExperiment protein)
    {
        ProteinDetails details = new ProteinDetails();
        details.setDatabaseNameAndVersion("database");
        details
                .setSequence("ISDFHSJDGJHFGHHJKDGHJGBGBKJBGSHDFYUERGFUYEWBGCHJEBVHJERBCVUYERBFYUEWBCYUWERBCUYEBCUYEBR");
        details.setDataSetPermID("12312312873812973");
        details.setFalseDiscoveryRate(123);
        protein.setDetails(details);
    }

    private void recreateUIWithDatasetTable(ProteinByExperiment protein, ContentPanel propertyPanel)
    {
        BorderLayoutData layoutData = createBorderLayoutData(LayoutRegion.WEST);
        layoutData.setSize(400);
        add(propertyPanel, layoutData);
        add(new BrowserSectionPanel(viewContext.getMessage(Dict.SEQUENCES), ProteinSequenceGrid
                .create(viewContext, proteinReferenceID)), createRightBorderLayoutData());
        add(new BrowserSectionPanel(viewContext.getMessage(Dict.DATA_SET_PROTEINS),
                DataSetProteinGrid.create(viewContext, experimentOrNull, proteinReferenceID)),
                createBorderLayoutData(LayoutRegion.SOUTH));
        layout();
    }

    private ContentPanel createPropertyPanel(ProteinByExperiment protein)
    {
        PropertyGrid propertyGrid = createPropertyGrid(protein);
        ContentPanel contentPanel = new ContentPanel();
        contentPanel.add(propertyGrid);
        return contentPanel;
    }

    private PropertyGrid createPropertyGrid(ProteinByExperiment protein)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        PropertyGrid propertyGrid = new PropertyGrid(viewContext, 0);
        if (experimentOrNull != null)
        {
            properties.put(viewContext.getMessage(Dict.EXPERIMENT_LABEL), experimentOrNull);
            propertyGrid.registerPropertyValueRenderer(Experiment.class, PropertyValueRenderers
                    .createExperimentPropertyValueRenderer(viewContext));
        }
        properties.put(viewContext.getMessage(Dict.UNIPROT_ID), protein);
        propertyGrid.registerPropertyValueRenderer(ProteinByExperiment.class,
                PropertyValueRenderers.createProteinIdentLinkRenderer(viewContext));

        properties.put(viewContext.getMessage(Dict.PROTEIN_DESCRIPTION), protein.getDescription());
        if (protein.getDetails() != null)
        {
            addProteinDetails(properties, propertyGrid, protein.getDetails());
        }

        propertyGrid.resizeRows(properties.size());
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private void addProteinDetails(Map<String, Object> properties, PropertyGrid propertyGrid,
            ProteinDetails proteinDetails)
    {
        properties.put(viewContext.getMessage(Dict.DATABASE_NAME_AND_VERSION), proteinDetails
                .getDatabaseNameAndVersion());
        properties.put(viewContext.getMessage(Dict.SEQUENCE_NAME), proteinDetails.getSequence());
        properties.put(viewContext.getMessage(Dict.FDR), proteinDetails.getFalseDiscoveryRate());
        properties.put(viewContext.getMessage(Dict.DATA_SET_PERM_ID), proteinDetails
                .getDataSetPermID());
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
