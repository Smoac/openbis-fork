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
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
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
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link SampleType}.
     */
    public final static IPropertyValueRenderer<SampleType> createSampleTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new SampleTypePropertyValueRenderer(messageProvider);
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
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link SampleProperty}.
     */
    public final static IPropertyValueRenderer<SampleProperty> createSamplePropertyPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityPropertyPropertyValueRenderer<SampleProperty>(messageProvider);
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
     * Renderer for {@link SampleType}.
     * 
     * @author Christian Ribeaud
     */
    private final static class SampleTypePropertyValueRenderer extends
            AbstractSimplePropertyValueRenderer<SampleType>
    {

        SampleTypePropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        public final String renderNotNull(final SampleType value)
        {
            return value.getCode();
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
            extends AbstractSimplePropertyValueRenderer<T>
    {

        EntityPropertyPropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        protected final String renderNotNull(final T value)
        {
            return value.getValue();
        }
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering {@link ExperimentType}.
     */
    public final static IPropertyValueRenderer<ExperimentType> createExperimentTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new ExperimentTypePropertyValueRenderer(messageProvider);
    }

    /**
     * Renderer for {@link ExperimentType}.
     * 
     * @author Izabela Adamczyk
     */
    private final static class ExperimentTypePropertyValueRenderer extends
            AbstractSimplePropertyValueRenderer<ExperimentType>
    {

        ExperimentTypePropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        public final String renderNotNull(final ExperimentType value)
        {
            return value.getCode();
        }
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering
     * {@link ExperimentProperty}.
     */
    public final static IPropertyValueRenderer<ExperimentProperty> createExperimentPropertyPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityPropertyPropertyValueRenderer<ExperimentProperty>(messageProvider);
    }

    /**
     * Creates a {@link IPropertyValueRenderer} implementation for rendering
     * {@link MaterialProperty}.
     */
    public final static IPropertyValueRenderer<MaterialProperty> createMaterialPropertyPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new EntityPropertyPropertyValueRenderer<MaterialProperty>(messageProvider);
    }

}
