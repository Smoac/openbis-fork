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
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ScreeningDisplaySettingsManager;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;

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

    public static final AnalysisProcedureChooser createHorizontal(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            String selectedAnalysisProcedureOrNull,
            IAnalysisProcedureSelectionListener selectionListener)
    {
        return new AnalysisProcedureChooser(viewContext, experimentCriteriaHolder,
                selectedAnalysisProcedureOrNull, selectionListener, new HorizontalPanel());
    }

    public static final AnalysisProcedureChooser createVertical(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            String selectedAnalysisProcedureOrNull,
            IAnalysisProcedureSelectionListener selectionListener)
    {

        final VerticalPanel layoutPanel = new VerticalPanel();
        layoutPanel.setWidth(200);
        return new AnalysisProcedureChooser(viewContext, experimentCriteriaHolder,
                selectedAnalysisProcedureOrNull, selectionListener, layoutPanel);
    }

    private final static String ANY_ANALYSIS_PROCEDURE = "Any";

    private final IViewContext<IScreeningClientServiceAsync> viewContext;
    private final IAnalysisProcedureSelectionListener selectionListener;

    private final ExperimentSearchCriteriaHolder experimentCriteriaHolder;

    private SimpleComboBox<String> analysisProceduresComboBox;

    private final Listener<BaseEvent> selectionChangeListener = new Listener<BaseEvent>()
        {

            public void handleEvent(BaseEvent be)
            {
                selectionChanged();
            }

        };

    private AnalysisProcedureChooser(IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            String selectedAnalysisProcedureOrNull,
            IAnalysisProcedureSelectionListener selectionListener, LayoutContainer layoutPanel)
    {
        this.viewContext = viewContext;
        this.selectionListener = selectionListener;
        this.experimentCriteriaHolder = experimentCriteriaHolder;

        analysisProceduresComboBox = createProceduresComboBox();

        add(layoutPanel);
        layoutPanel.setAutoHeight(true);
        layoutPanel.add(createComboLabel());
        layoutPanel.add(analysisProceduresComboBox);

        refresh(selectedAnalysisProcedureOrNull);
    }

    public void updateAnalysisProcedures()
    {
        String selection = analysisProceduresComboBox.getSimpleValue();
        String selectedProcedureOrNull = comboBoxValueToAnalysisProcedure(selection);
        refresh(selectedProcedureOrNull);
    }

    private void refresh(final String selectedAnalysisProcedureOrNull)
    {

        analysisProceduresComboBox.removeAll();

        viewContext.getService().listAnalysisProcedures(experimentCriteriaHolder.tryGetCriteria(),
                new AbstractAsyncCallback<AnalysisProcedures>(viewContext)
                    {
                        @Override
                        protected void process(AnalysisProcedures analysisProcedures)
                        {
                            addAnalysisProcedures(analysisProcedures.getProcedureCodes());
                            setInitialSelection(selectedAnalysisProcedureOrNull);
                        }
                    });

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

        comboBox.setWidth(160);
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
        sortedCodes.remove(ANY_ANALYSIS_PROCEDURE);
        sortedCodes.add(ANY_ANALYSIS_PROCEDURE);

        return sortedCodes;
    }

    private void addCodeToComboBox(String code)
    {
        if (analysisProceduresComboBox.findModel(code) == null)
        {
            analysisProceduresComboBox.add(code);
        }
    }

    private void setInitialSelection(String value)
    {
        // TODO KE: this is a logic we might want to review
        String analysisProcedureOrNull = value;
        if (StringUtils.isBlank(analysisProcedureOrNull))
        {
            analysisProcedureOrNull = getDefaultAnalysisProcedure();
        }

        String comboBoxValue = analysisCodeToComboBoxValue(analysisProcedureOrNull);

        if (ANY_ANALYSIS_PROCEDURE.equals(comboBoxValue)
                || analysisProceduresComboBox.findModel(comboBoxValue) == null)
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
        String analysisProcedureOrNull = comboBoxValueToAnalysisProcedure(selection);
        notifySelectionListener(analysisProcedureOrNull);
        setDefaultAnalysisProcedure(analysisProcedureOrNull);
    }

    private void notifySelectionListener(String analysisProcedureOrNull)
    {
        selectionListener.analysisProcedureSelected(analysisProcedureOrNull);
    }

    private String getDefaultAnalysisProcedure()
    {
        ScreeningDisplaySettingsManager screeningDisplaySettingsManager =
                new ScreeningDisplaySettingsManager(viewContext);
        return screeningDisplaySettingsManager.getDefaultAnalysisProcedure();
    }

    private void setDefaultAnalysisProcedure(String analysisProcedureOrNull)
    {
        if (StringUtils.isNotBlank(analysisProcedureOrNull))
        {
            ScreeningDisplaySettingsManager screeningDisplaySettingsManager =
                    new ScreeningDisplaySettingsManager(viewContext);
            screeningDisplaySettingsManager.setDefaultAnalysisProcedure(analysisProcedureOrNull);
        }
    }

    private String analysisCodeToComboBoxValue(String analysisProcedureOrNull)
    {
        return StringUtils.isBlank(analysisProcedureOrNull) ? ANY_ANALYSIS_PROCEDURE
                : analysisProcedureOrNull;
    }

    private String comboBoxValueToAnalysisProcedure(String comboBoxValue)
    {
        return ANY_ANALYSIS_PROCEDURE.equals(comboBoxValue) ? null : comboBoxValue;
    }
}