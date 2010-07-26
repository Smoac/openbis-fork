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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserverWithCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.EntityPropertyUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Invalidation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermValueEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * {@link ContentPanel} containing experiment properties.
 * 
 * @author Izabela Adamczyk
 */
public class ExperimentPropertiesPanel extends ContentPanel
{
    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "experiment-properties-section_";

    private final TechId experimentId;

    private final PropertyGrid grid;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final GenericExperimentViewer viewer;

    public ExperimentPropertiesPanel(final Experiment experiment,
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final GenericExperimentViewer viewer)
    {
        setHeading("Experiment Properties");
        this.experimentId = new TechId(experiment);
        this.viewContext = viewContext;
        this.viewer = viewer;
        this.grid = createPropertyGrid(experiment);
        add(grid);
        setScrollMode(Scroll.AUTOY);
    }

    private PropertyGrid createPropertyGrid(Experiment experiment)
    {
        return createPropertyGrid(createProperties(experiment, viewContext));
    }

    private final PropertyGrid createPropertyGrid(Map<String, Object> properties)
    {
        IMessageProvider messageProvider = viewContext;
        final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
        propertyGrid.getElement().setId(PROPERTIES_ID_PREFIX + experimentId);
        propertyGrid.registerPropertyValueRenderer(Person.class, PropertyValueRenderers
                .createPersonPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(ExperimentType.class, PropertyValueRenderers
                .createExperimentTypePropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Invalidation.class, PropertyValueRenderers
                .createInvalidationPropertyValueRenderer(messageProvider));
        propertyGrid.registerPropertyValueRenderer(Project.class, PropertyValueRenderers
                .createProjectPropertyValueRenderer(viewContext));
        final IPropertyValueRenderer<IEntityProperty> renderer =
                PropertyValueRenderers.createEntityPropertyPropertyValueRenderer(viewContext);
        propertyGrid.registerPropertyValueRenderer(EntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(GenericValueEntityProperty.class, renderer);
        propertyGrid.registerPropertyValueRenderer(VocabularyTermValueEntityProperty.class,
                renderer);
        propertyGrid.registerPropertyValueRenderer(MaterialValueEntityProperty.class, renderer);
        propertyGrid.setProperties(properties);
        return propertyGrid;
    }

    private static Map<String, Object> createProperties(Experiment experiment,
            IMessageProvider messageProvider)
    {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        final ExperimentType experimentType = experiment.getExperimentType();
        final Invalidation invalidation = experiment.getInvalidation();
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT), experiment.getIdentifier());
        properties.put(messageProvider.getMessage(Dict.PERM_ID), new ExternalHyperlink(experiment
                .getPermId(), experiment.getPermlink()));
        properties.put(messageProvider.getMessage(Dict.EXPERIMENT_TYPE), experimentType);
        properties.put(messageProvider.getMessage(Dict.REGISTRATOR), experiment.getRegistrator());
        properties.put(messageProvider.getMessage(Dict.REGISTRATION_DATE), experiment
                .getRegistrationDate());
        if (invalidation != null)
        {
            properties.put(messageProvider.getMessage(Dict.INVALIDATION), invalidation);
        }
        properties.put(messageProvider.getMessage(Dict.PROJECT), experiment.getProject());
        final List<IEntityProperty> experimentProperties = experiment.getProperties();
        Collections.sort(experimentProperties);
        List<PropertyType> types = EntityPropertyUtils.extractTypes(experimentProperties);
        for (final IEntityProperty property : experimentProperties)
        {
            properties.put(PropertyTypeRenderer.getDisplayName(property.getPropertyType(), types),
                    property);
        }
        return properties;
    }

    //
    // auto-refresh
    // 

    private final void updateProperties(Experiment experiment)
    {
        final Map<String, Object> properties = createProperties(experiment, viewContext);
        grid.resizeRows(properties.size());
        grid.setProperties(properties);
    }

    private void reloadData(AbstractAsyncCallback<Experiment> callback)
    {
        viewContext.getCommonService().getExperimentInfo(experimentId, callback);
    }

    public IDatabaseModificationObserverWithCallback getDatabaseModificationObserver()
    {
        return new PropertyGridDatabaseModificationObserver();
    }

    private class PropertyGridDatabaseModificationObserver extends
            AbstractDatabaseModificationObserverWithCallback
    {
        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[]
                {
                        DatabaseModificationKind.edit(ObjectKind.EXPERIMENT),
                        DatabaseModificationKind
                                .createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                        DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                        DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM) };
        }

        public void update(Set<DatabaseModificationKind> observedModifications)
        {
            reloadData(new ExperimentInfoCallback(viewContext, ExperimentPropertiesPanel.this));
        }

        private final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
        {
            private final ExperimentPropertiesPanel section;

            private ExperimentInfoCallback(final IViewContext<?> viewContext,
                    final ExperimentPropertiesPanel section)
            {
                super(viewContext);
                this.section = section;
            }

            //
            // AbstractAsyncCallback
            //

            /** This method triggers reloading of the {@link ExperimentPropertiesPanel} data. */
            @Override
            protected final void process(final Experiment result)
            {
                viewer.updateOriginalData(result);
                section.updateProperties(result);
                executeSuccessfulUpdateCallback();
            }

            @Override
            public void finishOnFailure(Throwable caught)
            {
                viewer.setupRemovedEntityView();
            }
        }
    }

}
