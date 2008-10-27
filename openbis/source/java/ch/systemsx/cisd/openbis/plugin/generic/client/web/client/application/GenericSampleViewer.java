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

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AbstractDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;

/**
 * The <i>generic</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class GenericSampleViewer extends AbstractDialog
{
    private final SampleGeneration sampleGeneration;

    private final IMessageProvider messageProvider;

    public GenericSampleViewer(final String heading, final IMessageProvider messageProvider,
            final SampleGeneration sampleGeneration)
    {
        super(heading);
        this.sampleGeneration = sampleGeneration;
        this.messageProvider = messageProvider;
        addWidget();
    }

    private final static Map<String, Object> createProperties(
            final IMessageProvider messageProvider, final SampleGeneration sampleGeneration)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final Sample sample = sampleGeneration.getGenerator();
        properties.put(messageProvider.getMessage("sample"), sample);
        properties.put(messageProvider.getMessage("sample_type"), sample.getSampleType());
        properties.put(messageProvider.getMessage("registrator"), sample.getRegistrator());
        properties.put(messageProvider.getMessage("registration_date"), sample
                .getRegistrationDate());
        properties.put(messageProvider.getMessage("generated_samples"), sampleGeneration
                .getGenerated());
        return properties;
    }

    //
    // AbstractDialog
    //

    @Override
    public final Widget getWidget()
    {
        final Map<String, Object> properties = createProperties(messageProvider, sampleGeneration);
        final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
        propertyGrid.registerPropertyValueRenderer(SampleType.class, PropertyValueRenderers
                .getSampleTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                .getSamplePropertyValueRenderer(messageProvider));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }
}