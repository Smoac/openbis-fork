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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
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

    public final static IPropertyValueRenderer<Sample> getSamplePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new SamplePropertyValueRenderer(messageProvider);
    }

    public final static IPropertyValueRenderer<SampleType> getSampleTypePropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new SampleTypePropertyValueRenderer(messageProvider);
    }

    public final static IPropertyValueRenderer<Person> getPersonPropertyValueRenderer(
            final IMessageProvider messageProvider)
    {
        return new PersonPropertyValueRenderer(messageProvider);
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
            return sample.getCode();
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

        SampleTypePropertyValueRenderer(IMessageProvider messageProvider)
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
}
