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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewerWithVerticalSplit;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.RowLayoutManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.IndistinguishableProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Peptide;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinByExperiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinDetails;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;

/**
 * @author Franz-Josef Elmer
 */
public class ProteinViewer extends AbstractViewerWithVerticalSplit<IEntityInformationHolder>
        implements IDatabaseModificationObserver
{
    private static final int AMINOACIDS_IN_SEQUENCE_PER_LINE = 90;

    private static final int AMINOACIDS_IN_ONE_BLOCK = 10;

    private static final String PREFIX = "protein-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    static AbstractTabItemFactory createTabItemFactory(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            final Experiment experiment, final ProteinInfo proteinInfo)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public String getId()
                {
                    return createWidgetID(experiment, proteinInfo.getId());
                }

                @Override
                public ITabItem create()
                {
                    ProteinViewer viewer =
                            new ProteinViewer(viewContext, experiment, proteinInfo.getId());
                    DatabaseModificationAwareComponent c =
                            new DatabaseModificationAwareComponent(viewer, viewer);
                    return DefaultTabItem.create(getTabTitle(), c, viewContext, false);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return HelpPageIdentifier.createSpecific("Protein Viewer");
                }

                @Override
                public String getTabTitle()
                {
                    String description = getAbbreviatedDescription(proteinInfo);
                    String identifier = experiment == null ? "?" : experiment.getCode();
                    return viewContext.getMessage(Dict.PROTEIN_IN_EXPERIMENT_TAB_LABEL,
                            description, identifier);
                }

                @Override
                public String tryGetLink()
                {
                    return null;
                }
            };
    }

    private static String getAbbreviatedDescription(final ProteinInfo proteinInfo)
    {
        String info = proteinInfo.getDescription();
        if (info == null)
        {
            info = proteinInfo.getAccessionNumber();
        }
        return StringUtils.abbreviate(info, 30);
    }

    static String createWidgetID(IIdAndCodeHolder experimentIdOrNull, TechId proteinReferenceID)
    {
        Long experimentID = experimentIdOrNull == null ? null : experimentIdOrNull.getId();
        return ID_PREFIX + experimentID + "_" + proteinReferenceID.getId();
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    private final Experiment experimentOrNull;

    private final TechId proteinReferenceID;

    private ProteinSamplesSection proteinSamplesSection;

    private ProteinViewer(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            Experiment experiment, TechId proteinReferenceID)
    {
        super(viewContext, "", createWidgetID(experiment, proteinReferenceID), false);
        this.viewContext = viewContext;
        this.experimentOrNull = experiment;
        this.proteinReferenceID = proteinReferenceID;
        reloadAllData();
    }

    @Override
    protected void reloadAllData()
    {
        if (experimentOrNull != null)
        {
            viewContext.getService().getProteinByExperiment(new TechId(experimentOrNull.getId()),
                    proteinReferenceID, new ProteinByExperimentCallback(viewContext, this));
        }
    }

    private void recreateUI(ProteinByExperiment protein)
    {
        setLayout(new BorderLayout());
        removeAll();
        setScrollMode(Scroll.AUTO);
        ContentPanel propertyPanel = createPropertyPanel(protein);

        ProteinDetails details = protein.getDetails();
        if (details == null)
        {
            recreateUIWithDatasetTable(protein, propertyPanel);
        } else
        {
            LayoutContainer centerPanel = new LayoutContainer();
            RowLayoutManager rowDataManager = new RowLayoutManager(centerPanel, new RowLayout());
            add(centerPanel, createBorderLayoutData(LayoutRegion.CENTER));
            rowDataManager.addToContainer(propertyPanel, new RowData(1, 0.5f));
            List<IndistinguishableProteinInfo> indistinguishableProteins =
                    details.getIndistinguishableProteinInfos();
            if (indistinguishableProteins.isEmpty() == false)
            {
                List<Peptide> peptides = details.getPeptides();
                ContentPanel panel =
                        createIndistinguishableProteinsSection(indistinguishableProteins, peptides);
                rowDataManager.addToContainer(panel, new RowData(1, 0.3f));
            }
            proteinSamplesSection =
                    new ProteinSamplesSection(viewContext, proteinReferenceID, experimentOrNull);
            rowDataManager.addToContainer(proteinSamplesSection, new RowData(1, 0.2f));
            layout();
        }
    }

    private ContentPanel createIndistinguishableProteinsSection(
            List<IndistinguishableProteinInfo> indistinguishableProteins, List<Peptide> peptides)
    {
        ContentPanel panel = new ContentPanel(new RowLayout());
        panel.setHeading(viewContext.getMessage(Dict.INDISTINGUISHABLE_PROTEINS));
        panel.setCollapsible(true);
        panel.setHeight("100%");
        TabPanel tabPanel = new TabPanel();
        tabPanel.setTabScroll(true);
        for (IndistinguishableProteinInfo info : indistinguishableProteins)
        {
            String accessionNumber = info.getAccessionNumber();
            TabItem item = new TabItem(accessionNumber);
            item.setLayout(new FitLayout());
            PropertyGrid propertyGrid = new PropertyGrid(viewContext, 4);
            final Map<String, Object> properties = new LinkedHashMap<String, Object>();
            properties.put(viewContext.getMessage(Dict.ACCESSION_NUMBER), info);
            propertyGrid.registerPropertyValueRenderer(IndistinguishableProteinInfo.class,
                    PropertyValueRenderers.createProteinIdentLinkRenderer(viewContext));
            properties.put(viewContext.getMessage(Dict.PROTEIN_DESCRIPTION), info.getDescription());
            String markedSequence = markPeptides(info.getSequence(), peptides);
            properties.put(viewContext.getMessage(Dict.SEQUENCE_NAME), markedSequence);
            properties.put(viewContext.getMessage(Dict.COVERAGE), info.getCoverage());
            propertyGrid.setProperties(properties);
            ContentPanel contentPanel = new ContentPanel();
            contentPanel.setHeight("100%");
            contentPanel.setScrollMode(Scroll.AUTO);
            contentPanel.add(propertyGrid);
            item.add(contentPanel);
            tabPanel.add(item);
        }
        panel.add(tabPanel, new RowData(1, 1));
        return panel;
    }

    private void recreateUIWithDatasetTable(ProteinByExperiment protein, ContentPanel propertyPanel)
    {
        BorderLayoutData layoutData = createBorderLayoutData(LayoutRegion.WEST);
        layoutData.setSize(400);
        add(propertyPanel, layoutData);
        DisposableTabContent sequencesSection =
                new DisposableTabContent(viewContext.getMessage(Dict.SEQUENCES), viewContext,
                        proteinReferenceID)
                    {
                        @Override
                        protected IDisposableComponent createDisposableContent()
                        {
                            return ProteinSequenceGrid.create(ProteinViewer.this.viewContext,
                                    proteinReferenceID);
                        }
                    };
        add(sequencesSection, createRightBorderLayoutData());
        DisposableTabContent proteinsSection =
                new DisposableTabContent(viewContext.getMessage(Dict.DATA_SET_PROTEINS),
                        viewContext, proteinReferenceID)
                    {
                        @Override
                        protected IDisposableComponent createDisposableContent()
                        {
                            return DataSetProteinGrid.create(ProteinViewer.this.viewContext,
                                    experimentOrNull, proteinReferenceID);
                        }
                    };
        add(proteinsSection, createBorderLayoutData(LayoutRegion.SOUTH));
        layout();
    }

    private ContentPanel createPropertyPanel(ProteinByExperiment protein)
    {
        PropertyGrid propertyGrid = createPropertyGrid(protein);
        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeading(viewContext.getMessage(Dict.PRIMARY_PROTEIN));
        contentPanel.setScrollMode(Scroll.AUTO);
        contentPanel.setCollapsible(true);
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
            propertyGrid.registerPropertyValueRenderer(Experiment.class,
                    PropertyValueRenderers.createExperimentPropertyValueRenderer(viewContext));
        }
        properties.put(viewContext.getMessage(Dict.ACCESSION_NUMBER), protein);
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
        properties.put(viewContext.getMessage(Dict.DATABASE_NAME_AND_VERSION),
                proteinDetails.getDatabaseNameAndVersion());
        String markedSequence =
                markPeptides(proteinDetails.getSequence(), proteinDetails.getPeptides());
        properties.put(viewContext.getMessage(Dict.SEQUENCE_NAME), markedSequence);

        properties.put(viewContext.getMessage(Dict.COVERAGE), proteinDetails.getCoverage());

        propertyGrid.registerPropertyValueRenderer(Peptide.class,
                PropertyValueRenderers.createPeptideRenderer(viewContext));
        properties.put(viewContext.getMessage(Dict.PEPTIDES, proteinDetails.getPeptides().size()),
                proteinDetails.getPeptides().toArray());

        properties.put(viewContext.getMessage(Dict.PROBABILITY), proteinDetails.getProbability());
        properties.put(viewContext.getMessage(Dict.FDR), proteinDetails.getFalseDiscoveryRate());

        DatasetInformationHolder dataset = new DatasetInformationHolder(proteinDetails);
        properties.put(viewContext.getMessage(Dict.DATA_SET_PERM_ID), dataset);
        propertyGrid.registerPropertyValueRenderer(DatasetInformationHolder.class,
                PropertyValueRenderers.createEntityInformationPropertyValueRenderer(viewContext));
    }

    private static String markPeptides(String sequence, List<Peptide> peptides)
    {
        List<String> peptideSequences = extractSequences(peptides);
        String markedSequence =
                OccurrencesMarker.markOccurrencesWithHtml(sequence, peptideSequences,
                        AMINOACIDS_IN_SEQUENCE_PER_LINE, AMINOACIDS_IN_ONE_BLOCK);
        // the letters should have fixed width
        return getFixedWidthHTMLString(markedSequence);
    }

    private static String getFixedWidthHTMLString(String text)
    {
        return "<font style=\"font-family:monospace\">" + text + "</font>";
    }

    private static List<String> extractSequences(List<Peptide> peptides)
    {
        List<String> result = new ArrayList<String>();
        for (Peptide peptide : peptides)
        {
            result.add(peptide.getSequence());
        }
        return result;
    }

    public static class DatasetInformationHolder implements IEntityInformationHolderWithIdentifier
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;
        private final ProteinDetails proteinDetails;

        public DatasetInformationHolder(ProteinDetails proteinDetails)
        {
            this.proteinDetails = proteinDetails;
        }

        public EntityKind getEntityKind()
        {
            return EntityKind.DATA_SET;
        }

        public EntityType getEntityType()
        {
            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(proteinDetails.getDataSetTypeCode());
            return dataSetType;
        }

        public String getIdentifier()
        {
            return proteinDetails.getDataSetPermID();
        }

        public Long getId()
        {
            return proteinDetails.getDataSetTechID();
        }

        public String getCode()
        {
            return proteinDetails.getDataSetPermID();
        }

        public String getPermId()
        {
            return getIdentifier();
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createDatabaseModificationObserver().getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createDatabaseModificationObserver().update(observedModifications);
    }

    private CompositeDatabaseModificationObserver createDatabaseModificationObserver()
    {
        CompositeDatabaseModificationObserver observer =
                new CompositeDatabaseModificationObserver();
        if (proteinSamplesSection != null)
        {
            observer.addObserver(proteinSamplesSection.tryGetDatabaseModificationObserver());
        }
        // TODO 2009-07-31, Piotr Buczek: refresh properties panel?
        return observer;
    }

    private static final class ProteinByExperimentCallback extends
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
