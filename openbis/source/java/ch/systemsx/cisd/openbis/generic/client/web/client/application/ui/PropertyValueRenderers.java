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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IInvalidationProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Material;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;

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
        return new EntityInformationHolderPropertyValueRenderer<Experiment>(viewContext);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link ExternalData}.
     */
    public final static IPropertyValueRenderer<ExternalData> createExternalDataPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new EntityInformationHolderPropertyValueRenderer<ExternalData>(viewContext);
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
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link DataSetProperty}
     * .
     */
    public final static IPropertyValueRenderer<DataSetProperty> createDataSetPropertyPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new EntityPropertyPropertyValueRenderer<DataSetProperty>(viewContext);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link DataSetType}.
     */
    public final static IPropertyValueRenderer<DataSetType> createDataSetTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityTypePropertyValueRenderer<DataSetType>(messageProvider);
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
            switch (getDataTypeCode(object))
            {
                case MATERIAL:
                    return createLinkToMaterial(object);
                case CONTROLLEDVOCABULARY:
                    return createVocabularyTermLink(object);
                case HYPERLINK:
                    return createHyperlink(object);
                case MULTILINE_VARCHAR:
                    return createMultilineHtmlWidget(object);
                default:
                    return createHtmlWidget(object);
            }
        }

        private DataTypeCode getDataTypeCode(T property)
        {
            return getPropertyType(property).getDataType().getCode();
        }

        private Widget createLinkToMaterial(T object)
        {
            Material material = object.getMaterial();
            if (material != null)
            {
                final ClickListener listener =
                        new OpenEntityDetailsTabClickListener(material, viewContext);

                final Hyperlink link = LinkRenderer.getLinkWidget(material.getCode(), listener);

                FlowPanel panel = new FlowPanel();
                panel.add(link);
                // if all material types are allowed material type will be displayed too
                if (isAllowedMaterialTypeUnspecified(object))
                {
                    panel.add(new InlineHTML(" [" + material.getMaterialType().getCode() + "]"));
                }
                return panel;
            } else
            {
                return createHtmlWidget(object);
            }
        }

        private Widget createMultilineHtmlWidget(T object)
        {
            return new MultilineHTML(object.getValue());
        }

        private Widget createHtmlWidget(T object)
        {
            return new InlineHTML(object.getValue());
        }

        private Widget createHyperlink(T object)
        {
            String value = object.getValue();
            return new ExternalHyperlink(value, value);
        }

        private Widget createVocabularyTermLink(T object)
        {
            VocabularyTerm term = object.getVocabularyTerm();
            String code = null;
            String url = null;
            if (term != null)
            {
                code = term.getCode();
                url = term.getUrl();
            }
            if (code != null && url != null)
            {
                return new ExternalHyperlink(code, url);
            }
            return createHtmlWidget(object);
        }

        private boolean isAllowedMaterialTypeUnspecified(T property)
        {
            return getPropertyType(property).getMaterialType() == null;
        }

        private PropertyType getPropertyType(T property)
        {
            return property.getEntityTypePropertyType().getPropertyType();
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
     * Renderer for {@link IEntityInformationHolder}.
     * 
     * @author Piotr Buczek
     */
    private static class EntityInformationHolderPropertyValueRenderer<T extends IEntityInformationHolder>
            extends AbstractPropertyValueRenderer<T>
    {

        private final IViewContext<?> viewContext;

        EntityInformationHolderPropertyValueRenderer(final IViewContext<?> viewContext)
        {
            super(viewContext);
            this.viewContext = viewContext;
        }

        //
        // AbstractPropertyValueRenderer
        //

        public FlowPanel getAsWidget(final T entity)
        {
            final String code = entity.getCode();
            final boolean invalidate = getInvalidate(entity);
            final ClickListener listener =
                    new OpenEntityDetailsTabClickListener(entity, viewContext);
            final Hyperlink link = LinkRenderer.getLinkWidget(code, listener, invalidate);

            // putting link into a panel makes it a block/row
            // which is important if they are rendered as an array
            final FlowPanel panel = new FlowPanel();
            panel.add(link);
            return panel;
        }

        private boolean getInvalidate(final T entity)
        {
            if (entity instanceof IInvalidationProvider)
            {
                return ((IInvalidationProvider) entity).getInvalidation() != null;
            } else
            {
                return false;
            }
        }

    }

    /**
     * Renderer for {@link Sample}.
     * 
     * @author Piotr Buczek
     */
    private final static class SamplePropertyValueRenderer extends
            EntityInformationHolderPropertyValueRenderer<Sample>
    {
        private final boolean withType;

        SamplePropertyValueRenderer(final IViewContext<?> viewContext, final boolean withType)
        {
            super(viewContext);
            this.withType = withType;
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        public FlowPanel getAsWidget(final Sample sample)
        {
            final FlowPanel panel = super.getAsWidget(sample);
            if (withType)
            {
                panel.add(new InlineHTML(" [" + sample.getSampleType().getCode() + "]"));
            }
            return panel;
        }

    }

}
