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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateDatasetViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateLocationsMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.sample.LibrarySampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;

/**
 * {@link IClientPluginFactory} implementation for <i>screening</i> plugin.
 * <p>
 * Currently, this implementation only runs for a sample of type SampleTypeCode#CELL_PLATE.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class ClientPluginFactory extends AbstractClientPluginFactory<ScreeningViewContext>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final ScreeningViewContext createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        Set<String> types = new HashSet<String>();
        if (entityKind == EntityKind.SAMPLE)
        {
            types.add(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
            types.add(ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE);
        } else if (entityKind == EntityKind.MATERIAL)
        {
            types.add(ScreeningConstants.GENE_PLUGIN_TYPE_CODE);
            types.add(ScreeningConstants.SIRNA_PLUGIN_TYPE_NAME);
            // NOTE: it would be better to fetch all the material types from the db, but we cannot
            // do this - this code is executed before the user logs in.
            // Another way not to hardcode material types would be to allow the plugin to be used
            // for all material types.
            types.add("CONTROL");
            types.add("COMPOUND");

        } else if (entityKind == EntityKind.DATA_SET)
        {
            types.add(ScreeningConstants.IMAGE_DATASET_PLUGIN_TYPE_CODE);
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    public <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind)
    {
        ScreeningViewContext viewContext = getViewContext();
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin(viewContext);
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin(viewContext);
        }
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new DatasetClientPlugin(viewContext);
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class MaterialClientPlugin extends DelegatedClientPlugin<MaterialType>
    {
        private MaterialClientPlugin(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext, EntityKind.MATERIAL);
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(final BasicEntityType materialType,
                final IIdAndCodeHolder materialId)
        {
            return createPlateLocationsMaterialViewerTabFactory(materialId, null, getViewContext());
        }
    }

    /**
     * opens material viewer showing wells in which the material is contained, with a selected
     * experiment
     */
    public static final void openPlateLocationsMaterialViewer(final IIdAndCodeHolder materialId,
            final ExperimentSearchCriteria experimentCriteriaOrNull,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        AbstractTabItemFactory tab =
                createPlateLocationsMaterialViewerTabFactory(materialId, experimentCriteriaOrNull,
                        viewContext);
        DispatcherHelper.dispatchNaviEvent(tab);
    }

    private static final AbstractTabItemFactory createPlateLocationsMaterialViewerTabFactory(
            final IIdAndCodeHolder materialId,
            final ExperimentSearchCriteria experimentCriteriaOrNull,
            final IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    final DatabaseModificationAwareComponent viewer =
                            PlateLocationsMaterialViewer.create(viewContext, TechId
                                    .create(materialId), experimentCriteriaOrNull);
                    return createMaterialViewerTab(materialId, viewer, viewContext);
                }

                @Override
                public String getId()
                {
                    return PlateLocationsMaterialViewer.createId(TechId.create(materialId));
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return PlateLocationsMaterialViewer.getHelpPageIdentifier();
                }
            };
    }

    private static ITabItem createMaterialViewerTab(final IIdAndCodeHolder materialId,
            final DatabaseModificationAwareComponent viewer, IViewContext<?> viewContext)
    {
        return createViewerTab(viewer, materialId, Dict.MATERIAL, viewContext);
    }

    private final class DatasetClientPlugin extends DelegatedClientPlugin<DataSetType>
    {
        private final ScreeningViewContext screeningViewContext;

        private DatasetClientPlugin(ScreeningViewContext viewContext)
        {
            super(viewContext, EntityKind.DATA_SET);
            this.screeningViewContext = viewContext;
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(final BasicEntityType dataSetType,
                final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                PlateDatasetViewer.create(screeningViewContext, identifiable);
                        return createViewerTab(viewer, identifiable, Dict.DATA_SET,
                                screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId sampleId = TechId.create(identifiable);
                        return PlateDatasetViewer.createId(sampleId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Dataset Viewer");
                    }
                };
        }
    }

    private final class SampleClientPlugin extends DelegatedClientPlugin<SampleType>
    {
        private final ScreeningViewContext screeningViewContext;

        private SampleClientPlugin(ScreeningViewContext viewContext)
        {
            super(viewContext, EntityKind.SAMPLE);
            this.screeningViewContext = viewContext;
        }

        @Override
        public final AbstractTabItemFactory createEntityViewer(final BasicEntityType sampleType,
                final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                PlateSampleViewer.create(screeningViewContext, identifiable);
                        return createViewerTab(viewer, identifiable, Dict.SAMPLE,
                                screeningViewContext);
                    }

                    @Override
                    public String getId()
                    {
                        final TechId sampleId = TechId.create(identifiable);
                        return PlateSampleViewer.createId(sampleId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("Plate Sample Viewer");
                    }
                };
        }

        @Override
        public Widget createBatchRegistrationForEntityType(SampleType type)
        {
            if (type.getCode().equals(ScreeningConstants.LIBRARY_PLUGIN_TYPE_CODE))
            {
                return new LibrarySampleBatchRegistrationForm(getViewContext());
            } else
            {
                return super.createBatchRegistrationForEntityType(type);
            }
        }
    }

    private static ITabItem createViewerTab(DatabaseModificationAwareComponent viewer,
            ICodeHolder codeProvider, String dictTitleKey, IViewContext<?> viewContext)
    {
        String title = getViewerTitle(dictTitleKey, codeProvider, viewContext);
        return DefaultTabItem.create(title, viewer, viewContext, false);
    }

    private static String getViewerTitle(String dictTitleKey, ICodeHolder codeProvider,
            IMessageProvider messageProvider)
    {
        return AbstractViewer.getTitle(messageProvider, dictTitleKey, codeProvider);
    }

    /**
     * delegates all operations to generic plugin, should be subclasssed and the needed
     * functionality can override the default behaviour
     */
    private static class DelegatedClientPlugin<T extends BasicEntityType> implements
            IClientPlugin<T, IIdAndCodeHolder>
    {
        private final IClientPlugin<T, IIdAndCodeHolder> delegator;

        private DelegatedClientPlugin(IViewContext<?> viewContext, EntityKind entityKind)
        {
            this.delegator = createGenericClientFactory(viewContext).createClientPlugin(entityKind);
        }

        private static ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory createGenericClientFactory(
                IViewContext<?> viewContext)
        {
            ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory clientPluginFactory =
                    new ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory(
                            viewContext.getCommonViewContext());
            return clientPluginFactory;
        }

        public AbstractTabItemFactory createEntityViewer(final BasicEntityType entityType,
                final IIdAndCodeHolder identifiable)
        {
            return delegator.createEntityViewer(entityType, identifiable);
        }

        public Widget createBatchRegistrationForEntityType(final T entityType)
        {
            return delegator.createBatchRegistrationForEntityType(entityType);
        }

        public Widget createBatchUpdateForEntityType(final T entityType)
        {
            return delegator.createBatchUpdateForEntityType(entityType);
        }

        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return delegator.createEntityEditor(identifiable);
        }

        public DatabaseModificationAwareWidget createRegistrationForEntityType(T entityType,
                ActionContext context)
        {
            return delegator.createRegistrationForEntityType(entityType, context);
        }
    }

    @Override
    protected IModule maybeCreateModule()
    {
        return new ScreeningModule(getViewContext());
    }
}
