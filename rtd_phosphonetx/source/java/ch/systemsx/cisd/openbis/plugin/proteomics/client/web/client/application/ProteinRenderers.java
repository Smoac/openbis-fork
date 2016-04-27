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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers.EntityInformationHolderPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.AbstractPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.IPropertyValueRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ExternalHyperlink;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.ProteinViewer.DatasetInformationHolder;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AccessionNumberProvider;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.Occurrence;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.OccurrenceUtil;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.Peptide;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.PeptideModification;

/**
 * Utility method for creating {@link IPropertyValueRenderer} instances or directly rendering stuff in {@link ProteinViewer}.
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public final class ProteinRenderers
{
    private static final class Symbol
    {
        private final char character;

        private boolean inPeptide;

        private Double mass;

        Symbol(char character)
        {
            this.character = character;
        }

        public char getCharacter()
        {
            return character;
        }

        public boolean isInPeptide()
        {
            return inPeptide;
        }

        public void inPeptide()
        {
            this.inPeptide = true;
        }

        public Double getMass()
        {
            return mass;
        }

        public void setMass(Double mass)
        {
            this.mass = mass;
        }
    }

    private ProteinRenderers()
    {
        // Can not be instantiated
    }

    /**
     * Allows to render a property identifier which is a link to an external page with a description.
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

    public static String getFixedWidthHTMLString(String text)
    {
        return "<font style='font-family:monospace'>" + text + "</font>";
    }

    /** Produces an HTML code with all occurrences properly marked */
    public static String markOccurrencesWithHtml(String proteinSequence, List<Peptide> peptides,
            int blockLength)
    {
        List<Symbol> symbols = createSymbols(proteinSequence, peptides);
        StringBuilder builder = new StringBuilder();
        boolean inPeptide = false;
        for (int i = 0, n = symbols.size(); i < n; i++)
        {
            if (i > 0)
            {
                if (i % blockLength == 0)
                {
                    builder.append(" ");
                }
            }
            Symbol symbol = symbols.get(i);
            boolean nextInPeptide = symbol.isInPeptide();
            if (inPeptide != nextInPeptide)
            {
                builder.append(nextInPeptide ? "<font color='red'>" : "</font>");
            }
            inPeptide = nextInPeptide;
            Double mass = symbol.getMass();
            char character = symbol.getCharacter();
            if (mass == null)
            {
                builder.append(character);
            } else
            {
                builder.append(renderAminoAcidSymbol(character, i + 1, mass));
            }

        }
        return builder.toString();
    }

    public static String renderAminoAcidSymbol(char character, Integer positionOrNull, double mass)
    {
        String tooltip = positionOrNull == null ? "" : "position=" + positionOrNull + ", ";
        tooltip += "mass=" + mass;

        return "<font style='text-decoration:underline; cursor:pointer' color='blue' title='"
                + tooltip + "'>" + character + "</font>";
    }

    private static List<Symbol> createSymbols(String proteinSequence, List<Peptide> peptides)
    {
        List<Symbol> symbols = new ArrayList<Symbol>();
        for (int i = 0, n = proteinSequence.length(); i < n; i++)
        {
            symbols.add(new Symbol(proteinSequence.charAt(i)));
        }

        for (Peptide peptide : peptides)
        {
            List<Occurrence> occurances =
                    OccurrenceUtil.findAllOccurrences(proteinSequence, peptide.getSequence());
            List<PeptideModification> modifications = peptide.getModifications();
            for (Occurrence occurrence : occurances)
            {
                int startIndex = occurrence.getStartIndex();
                for (int i = startIndex, n = occurrence.getEndIndex(); i <= n; i++)
                {
                    symbols.get(i).inPeptide();
                }
                for (PeptideModification peptideModification : modifications)
                {
                    Symbol symbol = symbols.get(startIndex + peptideModification.getPosition() - 1);
                    symbol.setMass(peptideModification.getMass());
                }
            }
        }
        return symbols;
    }

    private final static class AccessionNumberRenderer extends
            AbstractPropertyValueRenderer<AccessionNumberProvider>
    {

        AccessionNumberRenderer(final IMessageProvider messageProvider)
        {
            super(messageProvider);
        }

        @Override
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

        @Override
        public Widget getAsWidget(Peptide peptide)
        {
            StringBuilder builder = new StringBuilder();
            String sequence = peptide.getSequence();
            List<PeptideModification> modifications = peptide.getModifications();
            Map<Integer, PeptideModification> modificationsMap = new HashMap<Integer, PeptideModification>();
            for (PeptideModification modification : modifications)
            {
                modificationsMap.put(modification.getPosition(), modification);
            }
            for (int i = 0, n = sequence.length(); i < n; i++)
            {
                char c = sequence.charAt(i);
                PeptideModification peptideModification = modificationsMap.get(i + 1);
                if (peptideModification != null)
                {
                    builder.append(renderAminoAcidSymbol(c, null, peptideModification.getMass()));
                } else
                {
                    builder.append(c);
                }
            }
            final FlowPanel panel = new FlowPanel();
            panel.add(new InlineHTML(getFixedWidthHTMLString(builder.toString())));
            return panel;
        }

    }
}
