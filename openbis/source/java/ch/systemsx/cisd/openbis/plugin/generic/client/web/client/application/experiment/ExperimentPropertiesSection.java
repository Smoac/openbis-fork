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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * {@link SectionPanel} containing experiment properties.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentPropertiesSection extends SectionPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "experiment-properties-section_";

    private final TechId experimentId;

    private Experiment experiment;

    private PropertyGrid grid;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public ExperimentPropertiesSection(final Experiment experiment,
            final IViewContext<IGenericClientServiceAsync> viewContext)
    {
        super("Experiment Properties");
        this.experimentId = new TechId(experiment);
        this.experiment = experiment;
        this.viewContext = viewContext;
        this.grid = createPropertyGrid();
        add(grid);
    }

    private final PropertyGrid createPropertyGrid()
    {
        IMessageProvider messageProvider = viewContext;
        final Map<String, Object> properties = createProperties(messageProvider);
        final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + experimentId);
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(ExperimentType.class, PropertyValueRenderers
                .createExperimentTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(ExperimentProperty.class, PropertyValueRenderers
                .createExperimentPropertyPropertyValueRenderer(viewContext));
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private final Map<String, Object> createProperties(final IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final ExperimentType experimentType = experiment.getExperimentType();
        final Invalidation invalidation = experiment.getInvalidation();
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT), experiment.getCode());
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT_TYPE), experimentType);
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), experiment.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), experiment
                .getRegistrationDate());
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage(Dict.INVALIDATION), invalidation);
        }

        final List<ExperimentProperty> experimentProperties = experiment.getProperties();
        Collections.sort(experimentProperties);
        for (final ExperimentProperty property : experimentProperties)
        {
            final String simpleCode =
                    property.getEntityTypePropertyType().getPropertyType().getLabel();
            properties.put(simpleCode, property);
        }
        return properties;
    }

    //
    // auto-refresh
    // 

    private final void updateProperties()
    {
        final Map<String, Object> properties = createProperties(viewContext);
        grid.resizeRows(properties.size());
        grid.setProperties(properties);
    }

    private void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    private void updateData(Experiment newExperiment)
    {
        setExperiment(newExperiment);
        updateProperties();
    }

    private void reloadData()
    {
        viewContext.getService().getExperimentInfo(experimentId,
                GWTUtils.getBaseIndexURL(), new ExperimentInfoCallback(viewContext, this));
    }

    public IDatabaseModificationObserver getDatabaseModificationObserver()
    {
        return new IDatabaseModificationObserver()
            {

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return new DatabaseModificationKind[]
                        {
                                DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                                DatabaseModificationKind
                                        .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                                DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM) };
                }

                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    reloadData();
                }
            };
    }

    private static final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {
        private final ExperimentPropertiesSection section;

        private ExperimentInfoCallback(final IViewContext<?> viewContext,
                final ExperimentPropertiesSection section)
        {
            super(viewContext);
            this.section = section;
        }

        //
        // AbstractAsyncCallback
        //

        /** This method triggers reloading of the {@link ExperimentPropertiesSection} data. */
        @Override
        protected final void process(final Experiment result)
        {
            section.updateData(result);
        }
    }
}