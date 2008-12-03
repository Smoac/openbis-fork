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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.DateRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PersonRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.AbstractPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DOMUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

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
            final IMessageProvider messageProvider, final boolean withType)
    {
        return new SamplePropertyValueRenderer(messageProvider, withType);
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

        SamplePropertyValueRenderer(final IMessageProvider messageProvider, final boolean withType)
        {
            super(messageProvider);
            this.withType = withType;
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        protected final String renderNotNull(final Sample sample)
        {
            final String code = sample.getCode();
            final StringBuilder builder = new StringBuilder();
            if (sample.getInvalidation() != null)
            {
                builder.append(DOMUtils.createDelElement(code));
            } else
            {
                builder.append(code);
            }
            if (withType)
            {
                builder.append(" [").append(sample.getSampleType().getCode()).append("]");
            }
            return builder.toString();
        }
    }

    /**
     * Renderer for {@link SampleType}.
     * 
     * @author Christian Ribeaud
     */
    private final static class SampleTypePropertyValueRenderer extends
            AbstractPropertyValueRenderer<SampleType>
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
            AbstractPropertyValueRenderer<Person>
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
            AbstractPropertyValueRenderer<Invalidation>
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
            return getMessageProvider().getMessage("invalidation_template",
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
            AbstractPropertyValueRenderer<ExperimentType>
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

}
