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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteriaHolder;

/**
 * An UI panel for selecting analysis procedures.
 * 
 * @author Kaloyan Enimanev
 */
class AnalysisProcedureChooser extends LayoutContainer
{

    private static final int COMBOX_WIDTH_PX = 320;

    private static final int PANEL_WIDTH_PX = COMBOX_WIDTH_PX + 40;

    private final static String UNSPECIFIED_PROCEDURE = "Unspecified";

    /**
     * Can be used from external classes wishing to be notified when the analysis procedure
     * selection changes.
     */
    public static interface IAnalysisProcedureSelectionListener
    {
        void analysisProcedureSelected(AnalysisProcedureCriteria criteria);
    }

    public static final AnalysisProcedureChooser createHorizontal(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            String selectedAnalysisProcedureOrNull,
            IAnalysisProcedureSelectionListener selectionListener,
            boolean triggerInitialSelectionEvent)
    {
        return new AnalysisProcedureChooser(viewContext, experimentCriteriaHolder,
                selectedAnalysisProcedureOrNull, selectionListener, triggerInitialSelectionEvent,
                new HorizontalPanel());
    }

    public static final AnalysisProcedureChooser createVertical(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            ExperimentSearchCriteriaHolder experimentCriteriaHolder,
            String selectedAnalysisProcedureOrNull,
            IAnalysisProcedureSelectionListener selectionListener,
            boolean triggerInitialSelectionEvent)
    {

        final VerticalPanel layoutPanel = new VerticalPanel();
        layoutPanel.setWidth(PANEL_WIDTH_PX);
        return new AnalysisProcedureChooser(viewContext, experimentCriteriaHolder,
                selectedAnalysisProcedureOrNull, selectionListener, triggerInitialSelectionEvent,
                layoutPanel);
    }

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
            IAnalysisProcedureSelectionListener selectionListener,
            boolean triggerInitialSelectionEvent, LayoutContainer layoutPanel)
    {
        this.viewContext = viewContext;
        this.selectionListener = selectionListener;
        this.experimentCriteriaHolder = experimentCriteriaHolder;

        analysisProceduresComboBox = createProceduresComboBox();

        add(layoutPanel);
        layoutPanel.setAutoHeight(true);
        layoutPanel.add(createComboLabel());
        layoutPanel.add(analysisProceduresComboBox);

        initSelection(selectedAnalysisProcedureOrNull, triggerInitialSelectionEvent);
    }

    private void initSelection(String selectedAnalysisProcedureOrNull, boolean triggerEvents)
    {
        if (false == triggerEvents)
        {
            disableEvents(true);
        }

        refresh(selectedAnalysisProcedureOrNull);

        if (false == triggerEvents)
        {
            enableEvents(true);
        }

    }

    public void updateAnalysisProcedures()
    {
        String selectedProcedureOrNull = getSelectionAsCriteria().tryGetAnalysisProcedureCode();
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

        comboBox.setWidth(COMBOX_WIDTH_PX);
        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setAllowBlank(false);
        comboBox.setEditable(false);
        comboBox.setEmptyText(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
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
        sortedCodes.remove(UNSPECIFIED_PROCEDURE);
        Collections.sort(sortedCodes);
        // unspecified is always an option and is always displayed at the end
        sortedCodes.add(UNSPECIFIED_PROCEDURE);

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
        String analysisProcedureOrNull = value;
        if (StringUtils.isBlank(analysisProcedureOrNull))
        {
            analysisProcedureOrNull = getDefaultAnalysisProcedure();
        }

        String comboBoxValue = analysisCodeToComboBoxValue(analysisProcedureOrNull);

        if (UNSPECIFIED_PROCEDURE.equals(comboBoxValue)
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
        AnalysisProcedureCriteria analysisProcedureCriteria = getSelectionAsCriteria();
        selectionListener.analysisProcedureSelected(analysisProcedureCriteria);
        setDefaultAnalysisProcedure(analysisProcedureCriteria);
    }

    private AnalysisProcedureCriteria getSelectionAsCriteria()
    {
        String selection = analysisProceduresComboBox.getSimpleValue();
        String analysisProcedureOrNull = comboBoxValueToAnalysisProcedure(selection);
        return StringUtils.isBlank(analysisProcedureOrNull) ? AnalysisProcedureCriteria
                .createNoProcedures() : AnalysisProcedureCriteria
                .createFromCode(analysisProcedureOrNull);
    }

    private String getDefaultAnalysisProcedure()
    {
        ScreeningDisplaySettingsManager screeningDisplaySettingsManager =
                new ScreeningDisplaySettingsManager(viewContext);
        return screeningDisplaySettingsManager.getDefaultAnalysisProcedure();
    }

    private void setDefaultAnalysisProcedure(AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        String analysisProcedureCode = analysisProcedureCriteria.tryGetAnalysisProcedureCode();
        if (StringUtils.isNotBlank(analysisProcedureCode))
        {
            ScreeningDisplaySettingsManager screeningDisplaySettingsManager =
                    new ScreeningDisplaySettingsManager(viewContext);
            screeningDisplaySettingsManager.setDefaultAnalysisProcedure(analysisProcedureCode);
        }
    }

    private String analysisCodeToComboBoxValue(String analysisProcedureOrNull)
    {
        return StringUtils.isBlank(analysisProcedureOrNull) ? UNSPECIFIED_PROCEDURE
                : analysisProcedureOrNull;
    }

    private String comboBoxValueToAnalysisProcedure(String comboBoxValue)
    {
        return UNSPECIFIED_PROCEDURE.equals(comboBoxValue) ? null : comboBoxValue;
    }
}