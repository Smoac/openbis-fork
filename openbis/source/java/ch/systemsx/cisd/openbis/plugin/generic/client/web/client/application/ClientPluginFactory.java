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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IGenericClientServiceAsync>
{
    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IGenericClientServiceAsync> createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new GenericViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    @SuppressWarnings("unchecked")
    public final <T extends EntityType, I extends IIdentifierHolder> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin();
        }
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin();
        }
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new DataSetClientPlugin();
        }

        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    public final Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        throw new UnsupportedOperationException(
                "Generic plugin factory supports every sample type.");
    }

    private String getDetailsTitle(final String entityKindDictKey, final String identifier)
    {
        return getViewContext().getMessage(Dict.DETAILS_TITLE,
                getViewContext().getMessage(entityKindDictKey), identifier);
    }

    private String getEditTitle(final String entityKindDictKey, final String identifier)
    {
        return getViewContext().getMessage(Dict.EDIT_TITLE,
                getViewContext().getMessage(entityKindDictKey), identifier);
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin implements IClientPlugin<SampleType, IIdentifierHolder>
    {

        //
        // IViewClientPlugin
        //

        public ITabItemFactory createEntityViewer(final IIdentifierHolder sample)
        {
            final String identifier = sample.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent sampleViewer =
                                GenericSampleViewer.create(getViewContext(), identifier);
                        return DefaultTabItem.create(getDetailsTitle(Dict.SAMPLE, identifier),
                                sampleViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericSampleViewer.createId(identifier);
                    }
                };
        }

        public final DatabaseModificationAwareWidget createRegistrationForEntityType(
                final SampleType sampleType)
        {
            GenericSampleRegistrationForm form =
                    new GenericSampleRegistrationForm(getViewContext(), sampleType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleBatchRegistrationForm(getViewContext(), sampleType);
        }

        public ITabItemFactory createEntityEditor(final IIdentifierHolder identifierHolder)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericSampleEditForm.create(getViewContext(), identifierHolder);
                        String title = getEditTitle(Dict.SAMPLE, identifierHolder.getIdentifier());
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericSampleEditForm.createId(identifierHolder, EntityKind.SAMPLE);
                    }
                };
        }

    }

    private final class MaterialClientPlugin extends
            ClientPluginAdapter<MaterialType, IIdentifierHolder>
    {

        @Override
        public final Widget createBatchRegistrationForEntityType(final MaterialType materialType)
        {
            return new GenericMaterialBatchRegistrationForm(getViewContext(), materialType);
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifierHolder identifiable)
        {
            final String identifier = identifiable.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent materialViewer =
                                GenericMaterialViewer.create(getViewContext(), identifier);
                        return DefaultTabItem.create(getDetailsTitle(Dict.MATERIAL, identifier),
                                materialViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericExperimentViewer.createId(identifier);
                    }
                };
        }

        @Override
        public ITabItemFactory createEntityEditor(final IIdentifierHolder identifierHolder)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericMaterialEditForm.create(getViewContext(), identifierHolder,
                                        true);
                        String title =
                                getEditTitle(Dict.MATERIAL, identifierHolder.getIdentifier());
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericMaterialEditForm.createId(identifierHolder,
                                EntityKind.MATERIAL);
                    }
                };
        }
    }

    private final class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdentifierHolder>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifierHolder identifiable)
        {
            final String identifier = identifiable.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent experimentViewer =
                                GenericExperimentViewer.create(getViewContext(), identifier);
                        return DefaultTabItem.create(getDetailsTitle(Dict.EXPERIMENT, identifier),
                                experimentViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericExperimentViewer.createId(identifier);
                    }
                };
        }

        @Override
        public DatabaseModificationAwareWidget createRegistrationForEntityType(
                ExperimentType entityType)
        {
            GenericExperimentRegistrationForm form =
                    new GenericExperimentRegistrationForm(getViewContext(), entityType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        @Override
        public ITabItemFactory createEntityEditor(final IIdentifierHolder identifierHolder)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericExperimentEditForm
                                        .create(getViewContext(), identifierHolder);
                        String title =
                                getEditTitle(Dict.EXPERIMENT, identifierHolder.getIdentifier());
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericDataSetEditForm.createId(identifierHolder,
                                EntityKind.EXPERIMENT);
                    }
                };
        }
    }

    private final class DataSetClientPlugin extends
            ClientPluginAdapter<DataSetType, IIdentifierHolder>
    {

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifierHolder identifiable)
        {
            final String identifier = identifiable.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent dataSetViewer =
                                GenericDataSetViewer.create(getViewContext(), identifier);
                        return DefaultTabItem.create(getDetailsTitle(Dict.DATA_SET, identifier),
                                dataSetViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericDataSetViewer.createId(identifier);
                    }
                };
        }

        @Override
        public ITabItemFactory createEntityEditor(final IIdentifierHolder identifierHolder)
        // TODO IIdentifierHolderWithTechId - but Identifier is not permanent
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericDataSetEditForm.create(getViewContext(), identifierHolder);
                        String title =
                                getEditTitle(Dict.DATA_SET, identifierHolder.getIdentifier());
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericDataSetEditForm.createId(identifierHolder,
                                EntityKind.DATA_SET);
                    }
                };
        }
    }
}
