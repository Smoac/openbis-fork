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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
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
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment viewer.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentViewer extends LayoutContainer
{
    private static final String PREFIX = "generic-experiment-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String PROPERTIES_ID_PREFIX =
            GenericConstants.ID_PREFIX + "generic-experiment-properties-viewer_";

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final String experimentIdentifier;

    public GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final String experimentIdentifier)
    {
        setId(ID_PREFIX + experimentIdentifier);
        this.experimentIdentifier = experimentIdentifier;
        this.viewContext = viewContext;
    }

    private final static ContentPanel createSection(final String heading, final Widget container)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(true);
        panel.setBorders(true);
        panel.setHeading(heading);
        panel.setCollapsible(true);
        panel.setAnimCollapse(false);
        panel.add(container, new RowData(-1, -1, new Margins(5)));
        panel.setBodyBorder(false);
        return panel;
    }

    private static ContentPanel createGeneralInfo(final Experiment experiment,
            final IViewContext<?> viewContext)
    {
        final ContentPanel panel = new ContentPanel();
        panel.setBorders(false);
        panel.setBodyBorder(false);
        panel.setHeaderVisible(false);
        panel.setScrollMode(Scroll.AUTOY);
        panel.add(createPropertyGrid(experiment, viewContext));
        return createSection("Experiment properties", panel);
    }

    private final static Map<String, Object> createProperties(
            final IMessageProvider messageProvider, final Experiment experiment)
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

    static private final PropertyGrid createPropertyGrid(final Experiment experiment,
            final IViewContext<?> viewContext)
    {
        final IMessageProvider messageProvider = viewContext.getMessageProvider();
        final Map<String, Object> properties = createProperties(messageProvider, experiment);
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

    /**
     * Load the experiment information.
     */
    public final void loadExperimentInfo()
    {
        viewContext.getService().getExperimentInfo(experimentIdentifier,
                new ExperimentInfoCallback(viewContext, this));
    }

    public static final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {
        private final GenericExperimentViewer genericExperimentViewer;

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericExperimentViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericExperimentViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link Experiment} for this <var>generic</var> experiment viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @SuppressWarnings("unchecked")
        @Override
        protected final void process(final Experiment result)
        {
            genericExperimentViewer.removeAll();
            genericExperimentViewer.setScrollMode(Scroll.AUTO);
            genericExperimentViewer.add(createGeneralInfo(result, viewContext), new RowData(-1, -1,
                    new Margins(5)));
            genericExperimentViewer.layout();
        }
    }
}