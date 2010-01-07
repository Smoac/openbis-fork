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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.PlateSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.GeneMaterialViewer;

/**
 * {@link IClientPluginFactory} implementation for <i>screening</i> plugin.
 * <p>
 * Currently, this implementation only runs for a sample of type SampleTypeCode#CELL_PLATE.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IScreeningClientServiceAsync>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IScreeningClientServiceAsync> createViewContext(
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
            types.add(ScreeningConstants.WELL_PLUGIN_TYPE_CODE);
        } else if (entityKind == EntityKind.MATERIAL)
        {
            types.add(ScreeningConstants.GENE_PLUGIN_TYPE_CODE);
        }
        return types;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType, I extends IIdentifiable> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind)
    {
        IViewContext<IScreeningClientServiceAsync> viewContext = getViewContext();
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin(viewContext);
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin(viewContext);
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class MaterialClientPlugin extends DelegatedClientPlugin<SampleType>
    {
        private MaterialClientPlugin(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext, EntityKind.MATERIAL);
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            final TechId materialId = TechId.create(identifiable);
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent viewer =
                                GeneMaterialViewer.create(getViewContext(), materialId);
                        return DefaultTabItem.create(getViewerTitle(), viewer, getViewContext(),
                                false);
                    }

                    private String getViewerTitle()
                    {
                        return AbstractViewer.getTitle(getViewContext(), Dict.MATERIAL,
                                identifiable);
                    }

                    public String getId()
                    {
                        return PlateSampleViewer.createId(materialId);
                    }
                };
        }
    }

    private final class SampleClientPlugin extends DelegatedClientPlugin<SampleType>
    {
        private SampleClientPlugin(IViewContext<IScreeningClientServiceAsync> viewContext)
        {
            super(viewContext, EntityKind.SAMPLE);
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            final TechId sampleId = TechId.create(identifiable);
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent sampleViewer =
                                PlateSampleViewer.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getViewerTitle(), sampleViewer,
                                getViewContext(), false);
                    }

                    private String getViewerTitle()
                    {
                        return AbstractViewer.getTitle(getViewContext(), Dict.SAMPLE, identifiable);
                    }

                    public String getId()
                    {
                        return PlateSampleViewer.createId(sampleId);
                    }
                };
        }
    }

    /**
     * delegates all operations to generic plugin, should be subclasssed and the needed
     * functionality can override the default behaviour
     */
    private static class DelegatedClientPlugin<T extends EntityType> implements
            IClientPlugin<T, IIdentifiable>
    {
        private final IClientPlugin<T, IIdentifiable> delegator;

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

        public ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return delegator.createEntityViewer(identifiable);
        }

        public Widget createBatchRegistrationForEntityType(final T entityType)
        {
            return delegator.createBatchRegistrationForEntityType(entityType);
        }

        public Widget createBatchUpdateForEntityType(final T entityType)
        {
            return delegator.createBatchUpdateForEntityType(entityType);
        }

        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return delegator.createEntityEditor(identifiable);
        }

        public DatabaseModificationAwareWidget createRegistrationForEntityType(T entityType)
        {
            return delegator.createRegistrationForEntityType(entityType);
        }
    }
}
