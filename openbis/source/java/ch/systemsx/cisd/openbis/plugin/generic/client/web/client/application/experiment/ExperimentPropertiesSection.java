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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames.REGISTRATION_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames.REGISTRATOR;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * {@link SectionPanel} containing experiment properties.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentPropertiesSection extends SectionPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "experiment-properties-section_";

    private final Experiment experiment;

    private final IViewContext<?> viewContext;

    public ExperimentPropertiesSection(final Experiment experiment,
            final IViewContext<?> viewContext)
    {
        super("Experiment properties");
        this.experiment = experiment;
        this.viewContext = viewContext;
        add(createPropertyGrid());
    }

    private final PropertyGrid createPropertyGrid()
    {
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        final Map<String, Object> properties = createProperties(messageProvider);
        final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
        propertyGrid.getElement()
                .setId(PROPERTIES_ID_PREFIX + experiment.getExperimentIdentifier());
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(ExperimentType.class, PropertyValueRenderers
                .createExperimentTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(ExperimentProperty.class, PropertyValueRenderers
                .createExperimentPropertyPropertyValueRenderer(messageProvider));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final ExperimentType experimentType = experiment.getExperimentType();
        final Invalidation invalidation = experiment.getInvalidation();
        properties.put(messageProvider.getMessage("experiment"), experiment.getCode());
        properties.put(messageProvider.getMessage("experiment_type"), experimentType);
        properties.put(messageProvider.getMessage(REGISTRATOR), experiment.getRegistrator());
        properties.put(messageProvider.getMessage(REGISTRATION_DATE), experiment
                .getRegistrationDate());
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage("invalidation"), invalidation);
        }
        for (final ExperimentProperty property : experiment.getProperties())
        {
            final String simpleCode =
                    property.getEntityTypePropertyType().getPropertyType().getLabel();
            properties.put(simpleCode, property);
        }
        return properties;
    }
}