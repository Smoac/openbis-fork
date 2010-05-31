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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers.EntityInformationHolderPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.AbstractPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.ProteinViewer.DatasetInformationHolder;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AccessionNumberProvider;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Peptide;

/**
 * Provides {@link IPropertyValueRenderer} implementations to render properties in
 * {@link PropertyGrid}
 * 
 * @author Tomasz Pylak
 */
public final class PropertyValueRenderers
{

    private PropertyValueRenderers()
    {
        // Can not be instantiated
    }

    /**
     * Allows to render a property identifier which is a link to an external page with a
     * description.
     */
    public final static IPropertyValueRenderer<AccessionNumberProvider> createProteinIdentLinkRenderer(
            final IViewContext<?> viewContext)
    {
        return new AccessionNumberRenderer(viewContext);
    }

    public final static IPropertyValueRenderer<Peptide> createPeptideRenderer(
            final IViewContext<?> viewContext)
    {
        return new PeptideRenderer(viewContext);
    }

    public final static IPropertyValueRenderer<Experiment> createExperimentPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers
                .createExperimentPropertyValueRenderer(viewContext);
    }

    public final static IPropertyValueRenderer<DatasetInformationHolder> createEntityInformationPropertyValueRenderer(
            final IViewContext<?> viewContext)
    {
        return new EntityInformationHolderPropertyValueRenderer<DatasetInformationHolder>(
                viewContext);
    }

    private final static class AccessionNumberRenderer extends
            AbstractPropertyValueRenderer<AccessionNumberProvider>
    {

        AccessionNumberRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        public Widget getAsWidget(AccessionNumberProvider object)
        {
            String accessionNumber = object.getAccessionNumber();
            String type = object.getAccessionNumberType();
            String url = AccessionNumberURLCreator.tryToCreateURL(type, accessionNumber);
            if (url == null)
            {
                return new Text(accessionNumber);
            }
            return new ExternalHyperlink(accessionNumber, url);
        }
    }

    private final static class PeptideRenderer extends AbstractPropertyValueRenderer<Peptide>
    {

        PeptideRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        public Widget getAsWidget(Peptide object)
        {
            final FlowPanel panel = new FlowPanel();
            panel.add(new InlineHTML(getFixedWidthHTMLString(object.getSequence())));
            return panel;
        }

        private static String getFixedWidthHTMLString(String text)
        {
            return "<font style=\"font-family:monospace\">" + text + "</font>";
        }

    }
}
