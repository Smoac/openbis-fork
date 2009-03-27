/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.AbstractPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.AbstractSimplePropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Some {@link IPropertyValueRenderer} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class PropertyValueRenderers
{

    private PropertyValueRenderers()
    {
        // Can not be instantiated
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link Sample}.
     */
    public final static IPropertyValueRenderer<Sample> createSamplePropertyValueRenderer(
            final IViewContext<?> viewContext, final boolean withType)
    {
        return new SamplePropertyValueRenderer(viewContext, withType);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link Experiment}.
     */
    public final static IPropertyValueRenderer<Experiment> createExperimentPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new ExperimentPropertyValueRenderer(viewContext);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link SampleType}.
     */
    public final static IPropertyValueRenderer<SampleType> createSampleTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityTypePropertyValueRenderer<SampleType>(messageProvider);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link Person}.
     */
    public final static IPropertyValueRenderer<Person> createPersonPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new PersonPropertyValueRenderer(messageProvider);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link Invalidation}.
     */
    public final static IPropertyValueRenderer<Invalidation> createInvalidationPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new InvalidationPropertyValueRenderer(messageProvider);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link ExperimentType}.
     */
    public final static IPropertyValueRenderer<ExperimentType> createExperimentTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityTypePropertyValueRenderer<ExperimentType>(messageProvider);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link SampleProperty}.
     */
    public final static IPropertyValueRenderer<SampleProperty> createSamplePropertyPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new EntityPropertyPropertyValueRenderer<SampleProperty>(viewContext);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering
     * {@link ExperimentProperty}.
     */
    public final static IPropertyValueRenderer<ExperimentProperty> createExperimentPropertyPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new EntityPropertyPropertyValueRenderer<ExperimentProperty>(viewContext);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering
     * {@link MaterialProperty}.
     */
    public final static IPropertyValueRenderer<MaterialProperty> createMaterialPropertyPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new EntityPropertyPropertyValueRenderer<MaterialProperty>(viewContext);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link MaterialType}.
     */
    public final static IPropertyValueRenderer<MaterialType> createMaterialTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityTypePropertyValueRenderer<MaterialType>(messageProvider);
    }

    /**
     * Renderer for {@link Sample}.
     * 
     * @author Christian Ribeaud
     */
    private final static class SamplePropertyValueRenderer extends
            AbstractPropertyValueRenderer<Sample>
    {
        private final boolean withType;

        private final IViewContext<?> viewContext;

        SamplePropertyValueRenderer(final IViewContext<?> viewContext, final boolean withType)
        {
            super(viewContext);
            this.viewContext = viewContext;
            this.withType = withType;
        }

        //
        // AbstractPropertyValueRenderer
        //

        public Widget getAsWidget(final Sample sample)
        {
            final String code = sample.getCode();
            final boolean invalidate = sample.getInvalidation() != null;
            final ClickListener listener =
                    new OpenEntityDetailsTabClickListener(sample, viewContext);
            final Hyperlink link = LinkRenderer.getLinkWidget(code, listener, invalidate);

            FlowPanel panel = new FlowPanel();
            panel.add(link);
            if (withType)
            {
                panel.add(new InlineHTML(" [" + sample.getSampleType().getCode() + "]"));
            }
            return panel;
        }

    }

    /**
     * Renderer for {@link Person}.
     * 
     * @author Christian Ribeaud
     */
    private final static class PersonPropertyValueRenderer extends
            AbstractSimplePropertyValueRenderer<Person>
    {

        PersonPropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        public final String renderNotNull(final Person person)
        {
            return PersonRenderer.createPersonAnchor(person);
        }
    }

    /**
     * Renderer for {@link Invalidation}.
     * 
     * @author Christian Ribeaud
     */
    private final static class InvalidationPropertyValueRenderer extends
            AbstractSimplePropertyValueRenderer<Invalidation>
    {

        InvalidationPropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        private final String rendererPerson(final Person person)
        {
            if (person != null)
            {
                return PersonRenderer.createPersonAnchor(person);
            }
            return "";
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        public final String renderNotNull(final Invalidation invalidation)
        {
            return getMessageProvider().getMessage(Dict.INVALIDATION_TEMPLATE,
                    rendererPerson(invalidation.getRegistrator()),
                    DateRenderer.renderDate(invalidation.getRegistrationDate()),
                    invalidation.getReason());
        }
    }

    /**
     * Renderer for {@link EntityProperty}.
     * 
     * @author Christian Ribeaud
     */
    private final static class EntityPropertyPropertyValueRenderer<T extends EntityProperty<?, ?>>
            extends AbstractPropertyValueRenderer<T>
    {

        private final IViewContext<?> viewContext;

        EntityPropertyPropertyValueRenderer(final IViewContext<?> viewContext)
        {
            super(viewContext);
            this.viewContext = viewContext;
        }

        //
        // AbstractPropertyValueRenderer
        //

        public Widget getAsWidget(T object)
        {
            if (isMaterialProperty(object))
            {
                return createLinkToMaterial(object);
            } else
            {
                return new InlineHTML(object.getValue());
            }
        }

        private Widget createLinkToMaterial(T object)
        {
            String value = object.getValue();
            MaterialIdentifier materialIdentifier = MaterialIdentifier.tryParseIdentifier(value);

            final EntityKind entityKind = EntityKind.MATERIAL;
            final EntityType entityType = new MaterialType();
            entityType.setCode(materialIdentifier.getTypeCode());
            final String identifier = value;

            final IEntityInformationHolder entity =
                    createEntityInformationHolder(entityKind, entityType, identifier);
            final String code = materialIdentifier.getCode();
            final ClickListener listener =
                    new OpenEntityDetailsTabClickListener(entity, viewContext);

            final Hyperlink link = LinkRenderer.getLinkWidget(code, listener);

            FlowPanel panel = new FlowPanel();
            panel.add(link);
            // if all material types are allowed material type will be displayed too
            if (isAllowedMaterialTypeUnspecified(object))
            {
                panel.add(new InlineHTML(" [" + materialIdentifier.getTypeCode() + "]"));
            }
            return panel;
        }

        private boolean isMaterialProperty(T property)
        {
            return getPropertyType(property).getDataType().getCode().equals(DataTypeCode.MATERIAL);
        }

        private boolean isAllowedMaterialTypeUnspecified(T property)
        {
            return getPropertyType(property).getMaterialType() == null;
        }

        private PropertyType getPropertyType(T property)
        {
            return property.getEntityTypePropertyType().getPropertyType();
        }

        private IEntityInformationHolder createEntityInformationHolder(final EntityKind entityKind,
                final EntityType entityType, final String identifier)
        {
            return new IEntityInformationHolder()
                {

                    public EntityKind getEntityKind()
                    {
                        return entityKind;
                    }

                    public EntityType getEntityType()
                    {
                        return entityType;
                    }

                    public String getIdentifier()
                    {
                        return identifier;
                    }

                };
        }
    }

    /**
     * Renderer for {@link EntityType}.
     * 
     * @author Christian Ribeaud
     */
    private final static class EntityTypePropertyValueRenderer<T extends EntityType> extends
            AbstractSimplePropertyValueRenderer<T>
    {

        EntityTypePropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        protected final String renderNotNull(final T value)
        {
            return value.getCode();
        }
    }

    /**
     * Renderer for {@link Experiment}.
     * 
     * @author Christian Ribeaud
     */
    private final static class ExperimentPropertyValueRenderer extends
            AbstractPropertyValueRenderer<Experiment>
    {

        private final IViewContext<?> viewContext;

        ExperimentPropertyValueRenderer(final IViewContext<?> viewContext)
        {
            super(viewContext);
            this.viewContext = viewContext;
        }

        //
        // AbstractPropertyValueRenderer
        //

        public Widget getAsWidget(final Experiment experiment)
        {
            final String code = experiment.getCode();
            final boolean invalidate = experiment.getInvalidation() != null;
            final ClickListener listener =
                    new OpenEntityDetailsTabClickListener(experiment, viewContext);
            final Hyperlink link = LinkRenderer.getLinkWidget(code, listener, invalidate);

            return link;
        }

    }

}
