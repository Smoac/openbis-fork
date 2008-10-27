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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.AbstractPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.DatePropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.renderer.PersonUtils;

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

    public final static IPropertyValueRenderer<Sample> createSamplePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new SamplePropertyValueRenderer(messageProvider);
    }

    public final static IPropertyValueRenderer<SampleType> createSampleTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new SampleTypePropertyValueRenderer(messageProvider);
    }

    public final static IPropertyValueRenderer<Person> createPersonPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new PersonPropertyValueRenderer(messageProvider);
    }

    public final static IPropertyValueRenderer<Invalidation> createInvalidationPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new InvalidationPropertyValueRenderer(messageProvider);
    }

    public final static IPropertyValueRenderer<Invalidation> getInvalidationPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new InvalidationPropertyValueRenderer(messageProvider);
    }

    /**
     * Renderer for {@link Sample}.
     * 
     * @author Christian Ribeaud
     */
    private final static class SamplePropertyValueRenderer extends
            AbstractPropertyValueRenderer<Sample>
    {

        SamplePropertyValueRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        protected final String renderNotNull(final Sample sample)
        {
            final String code = sample.getCode();
            if (sample.getInvalidation() != null)
            {
                return DOMUtils.createDelElement(code);
            }
            return code;
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
            return PersonUtils.toString(person);
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
                return PersonUtils.toString(person);
            }
            return "";
        }

        //
        // AbstractPropertyValueRenderer
        //

        @Override
        public final String renderNotNull(final Invalidation invalidation)
        {
            return getMessageProvider().getMessage(
                    "invalidation_template",
                    DatePropertyValueRenderer.defaultDateTimeFormat.format(invalidation
                            .getRegistrationDate()), invalidation.getReason(),
                    rendererPerson(invalidation.getRegistrator()));
        }
    }
}
