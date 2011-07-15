/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;

/**
 * An UI panel for selecting analysis procedures.
 * 
 * @author Kaloyan Enimanev
 */
class AnalysisProcedureChooser extends LayoutContainer
{

    /**
     * Can be used from external classes wishing to be notified when the analysis procedure
     * selection changes.
     */
    public static interface IAnalysisProcedureSelectionListener
    {
        void analysisProcedureSelected(String analysisProcedureOrNull);
    }

    private final static String UNSPECIFIED_ANALYSIS_PROCEDURE = "unspecified";

    private final IViewContext<IScreeningClientServiceAsync> viewContext;
    private final IAnalysisProcedureSelectionListener selectionListener;

    private SimpleComboBox<String> analysisProceduresComboBox;

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {

            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    public AnalysisProcedureChooser(IViewContext<IScreeningClientServiceAsync> viewContext,
            List<String> analysisProcedures,
            String selectedAnalysisProcedureOrNull,
            IAnalysisProcedureSelectionListener selectionListener)
    {
        this.viewContext = viewContext;
        this.selectionListener = selectionListener;
        analysisProceduresComboBox = createProceduresComboBox();

        setAutoHeight(true);
        setAutoWidth(true);

        add(createComboLabel());
        add(analysisProceduresComboBox);
        add(new FillToolItem());
        addAnalysisProcedures(analysisProcedures);


        setInitialSelection(selectedAnalysisProcedureOrNull);
    }

    private Component createComboLabel()
    {
        final LabelToolItem label =
                new LabelToolItem(viewContext.getMessage(Dict.ANALYSIS_PROCEDURE)
                        + GenericConstants.LABEL_SEPARATOR);
        label.addStyleName("default-text");
        return label;
    }

    private SimpleComboBox<String> createProceduresComboBox()
    {
        SimpleComboBox<String> comboBox = new SimpleComboBox<String>();

        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setAllowBlank(false);
        comboBox.setEditable(false);
        comboBox.setEmptyText("No data sets found...");
        comboBox.addListener(Events.SelectionChange, selectionChangeListener);

        return comboBox;
    }

    private void addAnalysisProcedures(List<String> codes)
    {
        for (String code : transformAndSortCodes(codes))
        {
            addCodeToComboBox(code);
        }
    }

    private List<String> transformAndSortCodes(List<String> codes)
    {
        List<String> sortedCodes = new ArrayList<String>();
        for (String code : codes)
        {
            sortedCodes.add(analysisCodeToComboBoxValue(code));
        }
        Collections.sort(sortedCodes);

        // unspecified is always an option and is always displayed at the end
        sortedCodes.remove(UNSPECIFIED_ANALYSIS_PROCEDURE);
        sortedCodes.add(UNSPECIFIED_ANALYSIS_PROCEDURE);

        return sortedCodes;
    }

    private void addCodeToComboBox(String code)
    {
        if (analysisProceduresComboBox.findModel(code) == null)
        {
            analysisProceduresComboBox.add(code);
        }
    }

    private void setInitialSelection(String analysisProcedureOrNull)
    {
        String comboBoxValue = analysisCodeToComboBoxValue(analysisProcedureOrNull);
        if (UNSPECIFIED_ANALYSIS_PROCEDURE.equals(comboBoxValue))
        {
            comboBoxValue = getFirstValueFromCombo();
        }
        analysisProceduresComboBox.setSimpleValue(comboBoxValue);
    }

    private String getFirstValueFromCombo()
    {
        return analysisProceduresComboBox.getStore().getAt(0).getValue();
    }

    private void selectionChanged()
    {
        String selection = analysisProceduresComboBox.getSimpleValue();
        notifySelectionListener(selection);
    }

    private void notifySelectionListener(String selection)
    {
        String analysisProcedureOrNull = comboBoxValueToAnalysisProcedure(selection);
        selectionListener.analysisProcedureSelected(analysisProcedureOrNull);
    }

    private String analysisCodeToComboBoxValue(String analysisProcedureOrNull)
    {
        return StringUtils.isBlank(analysisProcedureOrNull) ? UNSPECIFIED_ANALYSIS_PROCEDURE
                : analysisProcedureOrNull;
    }

    private String comboBoxValueToAnalysisProcedure(String comboBoxValue)
    {
        return UNSPECIFIED_ANALYSIS_PROCEDURE.equals(comboBoxValue) ? null : comboBoxValue;
    }
}