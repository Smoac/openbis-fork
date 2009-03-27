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

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ViewerTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableMaterial;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableSample;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
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
    public final <T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>, I extends IIdentifierHolder, V extends IEditableEntity<T, S, P>> IClientPlugin<T, S, P, I, V> createClientPlugin(
            EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, S, P, I, V>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, S, P, I, V>) new SampleClientPlugin();
        }
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, S, P, I, V>) new MaterialClientPlugin();
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

    private final class SampleClientPlugin
            implements
            IClientPlugin<SampleType, SampleTypePropertyType, SampleProperty, IIdentifierHolder, EditableSample>
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
                        final GenericSampleViewer sampleViewer =
                                new GenericSampleViewer(getViewContext(), identifier);
                        return new ViewerTabItem(getDetailsTitle(Dict.SAMPLE, identifier),
                                sampleViewer, false);
                    }

                    public String getId()
                    {
                        return GenericSampleViewer.createId(identifier);
                    }
                };
        }

        public final Widget createRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleRegistrationForm(getViewContext(), sampleType);
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleBatchRegistrationForm(getViewContext(), sampleType);
        }

        public ITabItemFactory createEntityEditor(final EditableSample entity)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        Component component =
                                new GenericSampleEditForm(getViewContext(), entity, true);
                        return new DefaultTabItem(
                                getEditTitle(Dict.SAMPLE, entity.getIdentifier()), component, true);
                    }

                    public String getId()
                    {
                        return GenericSampleEditForm.ID_PREFIX + entity.getId();
                    }
                };
        }

    }

    private final class MaterialClientPlugin
            extends
            ClientPluginAdapter<MaterialType, MaterialTypePropertyType, MaterialProperty, IIdentifierHolder, EditableMaterial>
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
                        final GenericMaterialViewer materialViewer =
                                new GenericMaterialViewer(getViewContext(), identifier);
                        return new ViewerTabItem(getDetailsTitle(Dict.MATERIAL, identifier),
                                materialViewer, false);
                    }

                    public String getId()
                    {
                        return GenericExperimentViewer.createId(identifier);
                    }
                };
        }

        @Override
        public ITabItemFactory createEntityEditor(final EditableMaterial entity)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        Component component =
                                new GenericMaterialEditForm(getViewContext(), entity, true);
                        return new DefaultTabItem(getEditTitle(Dict.MATERIAL, entity
                                .getIdentifier()), component, true);
                    }

                    public String getId()
                    {
                        return GenericMaterialEditForm.ID_PREFIX + entity.getId();
                    }
                };
        }
    }

    private final class ExperimentClientPlugin
            extends
            ClientPluginAdapter<ExperimentType, ExperimentTypePropertyType, ExperimentProperty, IIdentifierHolder, EditableExperiment>
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
                        final GenericExperimentViewer experimentViewer =
                                new GenericExperimentViewer(getViewContext(), identifier);
                        return new ViewerTabItem(getDetailsTitle(Dict.EXPERIMENT, identifier),
                                experimentViewer, false);
                    }

                    public String getId()
                    {
                        return GenericExperimentViewer.createId(identifier);
                    }
                };
        }

        @Override
        public Widget createRegistrationForEntityType(ExperimentType entityType)
        {
            return new GenericExperimentRegistrationForm(getViewContext(), entityType);
        }

        @Override
        public ITabItemFactory createEntityEditor(final EditableExperiment entity)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        Component component =
                                new GenericExperimentEditForm(getViewContext(), entity, true);
                        return new DefaultTabItem(getEditTitle(Dict.EXPERIMENT, entity
                                .getIdentifier()), component, true);
                    }

                    public String getId()
                    {
                        return GenericExperimentEditForm.ID_PREFIX + entity.getId();
                    }
                };
        }
    }
}
