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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.script;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ChosenEntitySetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.DataSetChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.IChosenEntityListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MaterialChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.SampleChooserField.SampleChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabClickListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.MultilineHTML;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DynamicPropertyEvaluationInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Izabela Adamczyk
 */
public class ScriptExecutionFramework
{

    FormPanel panel;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final SampleChooserFieldAdaptor sampleChooser;

    private final ExperimentChooserFieldAdaptor experimentChooser;

    private final MaterialChooserField materialChooser;

    private final DataSetChooserField datasetChooser;

    private final FieldSet evaluationResultPanel;

    private final EntityKindSelectionWidget entityKindChooser;

    private final State state = new State();

    private final AdapterField entityLink;

    private final MultilineHTML html;

    private final IValidable validable;

    private static class State
    {
        private String script;

        private IEntityInformationHolderWithPermId chosenEntity;

        public String getScript()
        {
            return script;
        }

        public void setScript(String script)
        {
            this.script = script;
        }

        public IEntityInformationHolderWithPermId getChosenEntity()
        {
            return chosenEntity;
        }

        public void setChosenEntity(IEntityInformationHolderWithPermId chosenEntity)
        {
            this.chosenEntity = chosenEntity;
        }

    }

    public ScriptExecutionFramework(IViewContext<ICommonClientServiceAsync> viewContext,
            IValidable validable)
    {
        this.viewContext = viewContext;
        this.validable = validable;
        entityKindChooser = new EntityKindSelectionWidget(viewContext, null, true, false);
        sampleChooser =
                SampleChooserField.create(viewContext.getMessage(Dict.SAMPLE), true, null, true,
                        true, false, viewContext.getCommonViewContext(),
                        SampleTypeDisplayID.SCRIPT_EDITOR_SAMPLE_CHOOSER);
        experimentChooser =
                ExperimentChooserField.create(viewContext.getMessage(Dict.EXPERIMENT), true, null,
                        viewContext);
        materialChooser =
                MaterialChooserField.create(viewContext.getMessage(Dict.MATERIAL), true, null,
                        null, viewContext);
        datasetChooser =
                DataSetChooserField
                        .create(viewContext.getMessage(Dict.DATA_SET), true, viewContext);
        final Map<EntityKind, Field<?>> map =
                createEntitySelectionMap(sampleChooser, experimentChooser, materialChooser,
                        datasetChooser);
        entityLink = createEntityLink();
        html = new MultilineHTML("");
        evaluationResultPanel = createResultField(html);
        updateVisibleEntityChooser(map, entityKindChooser);
        entityKindChooser
                .addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>()
                    {
                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<SimpleComboValue<String>> se)
                        {
                            state.setChosenEntity(null);
                            updateVisibleEntityChooser(map, entityKindChooser);
                            updateVisibleEntityLink(state, entityLink);
                            evaluationResultPanel.setVisible(false);
                        }
                    });
        setChosenEntityListener(sampleChooser.getChooserField(), state, entityLink);
        setChosenEntityListener(experimentChooser.getChooserField(), state, entityLink);
        setChosenEntityListener(materialChooser, state, entityLink);
        setChosenEntityListener(datasetChooser, state, entityLink);

        panel = createPanel();
        panel.add(entityKindChooser);
        panel.add(sampleChooser.getChooserField());
        panel.add(experimentChooser.getChooserField());
        panel.add(materialChooser);
        panel.add(datasetChooser);
        panel.add(entityLink);
        panel.add(createButtonsField());
        panel.add(evaluationResultPanel);
        updateVisibleEntityLink(state, entityLink);
    }

    private static <T extends IEntityInformationHolderWithPermId> void setChosenEntityListener(
            ChosenEntitySetter<T> setter, final State state, final Field<?> entityLink)
    {
        setter.addChosenEntityListener(new IChosenEntityListener<T>()
            {
                public void entityChosen(T entity)
                {
                    if (entity != null)
                    {
                        state.setChosenEntity(entity);
                        updateVisibleEntityLink(state, entityLink);
                    }
                }
            });
    }

    private AdapterField createButtonsField()
    {
        AdapterField field =
                new AdapterField(WidgetUtils.inRow(createCalculateButton(), createSeparator(),
                        createResetButton()));
        field.setLabelSeparator("");
        return field;
    }

    private Html createSeparator()
    {
        return new Html("&nbsp;");
    }

    private void reset()
    {
        panel.reset();
        state.setChosenEntity(null);
        updateVisibleEntityLink(state, entityLink);
        html.setHTML("");
        evaluationResultPanel.setVisible(false);
    }

    private Button createResetButton()
    {
        Button button = new Button(viewContext.getMessage(Dict.BUTTON_RESET));
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    reset();
                }
            });
        return button;
    }

    private Button createCalculateButton()
    {
        Button refresh = new Button(viewContext.getMessage(Dict.BUTTON_EVALUATE));
        refresh.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    boolean thisValid = panel.isValid();
                    boolean dependantValid = validable.isValid();
                    if (thisValid && dependantValid)
                    {
                        evaluationResultPanel.setVisible(true);
                        evaluate();
                    }
                }
            });
        return refresh;
    }

    public FieldSet createResultField(MultilineHTML widget)
    {
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(viewContext.getMessage(Dict.EVALUATION_RESULT));
        fieldSet.add(widget);
        fieldSet.setVisible(false);
        return fieldSet;
    }

    private AdapterField createEntityLink()
    {
        AdapterField field =
                new AdapterField(LinkRenderer.getLinkWidget(
                        viewContext.getMessage(Dict.SHOW_DETAILS),
                        new OpenEntityDetailsTabClickListener(null, viewContext)
                            {
                                @Override
                                protected IEntityInformationHolderWithPermId getEntity()
                                {
                                    return state.getChosenEntity();
                                }
                            }));
        field.setFieldLabel(viewContext.getMessage(Dict.ENTITY_DETAILS));
        return field;

    }

    public Widget getWidget()
    {
        FieldSet set = new FieldSet();
        set.setHeading(viewContext.getMessage(Dict.SCRIPT_TESTER));
        set.add(panel);
        return set;
    }

    public void update(String script)
    {
        state.setScript(script);
    }

    private void evaluate()
    {
        if (StringUtils.isBlank(sampleChooser.getValue()) == false)
        {
            evaluate(EntityKind.SAMPLE, sampleChooser.getValue(), state.getScript());
        }
        if (experimentChooser.tryToGetValue() != null
                && StringUtils.isBlank(experimentChooser.tryToGetValue().getIdentifier()) == false)
        {
            evaluate(EntityKind.EXPERIMENT, experimentChooser.tryToGetValue().getIdentifier(),
                    state.getScript());
        }
        if (StringUtils.isBlank(materialChooser.getValue()) == false)
        {
            evaluate(EntityKind.MATERIAL, materialChooser.getValue(), state.getScript());
        }
        if (StringUtils.isBlank(datasetChooser.getValue()) == false)
        {
            evaluate(EntityKind.DATA_SET, datasetChooser.getValue(), state.getScript());
        }
    }

    private void evaluate(EntityKind kind, String entity, String script)
    {
        if (entity == null)
        {
            return;
        }
        updateEvaluationResultField(viewContext.getMessage(Dict.EVALUATION_IN_PROGRESS));
        viewContext.getCommonService().evaluate(
                new DynamicPropertyEvaluationInfo(kind, entity, script),
                new AbstractAsyncCallback<String>(viewContext)
                    {

                        @Override
                        protected void process(String result)
                        {
                            updateEvaluationResultField(result);
                        }

                        @Override
                        public void finishOnFailure(Throwable caught)
                        {
                            updateEvaluationResultField("");
                            evaluationResultPanel.setVisible(false);
                        }
                    });
    }

    private void updateEvaluationResultField(String result)
    {
        html.setHTML(result == null ? "(null)" : result);
    }

    private static void updateVisibleEntityLink(State state, Field<?> entityLink)
    {
        FieldUtil.setVisibility(state.getChosenEntity() != null, entityLink);
    }

    private static void updateVisibleEntityChooser(Map<EntityKind, Field<?>> map,
            EntityKindSelectionWidget entityKindChooser)
    {
        for (Field<?> w : map.values())
        {
            w.reset();
            EntityKind kind = entityKindChooser.tryGetEntityKind();
            boolean visible = kind != null && w == map.get(kind);
            FieldUtil.setVisibility(visible, w);
        }
    }

    private static FormPanel createPanel()
    {
        FormPanel p = new FormPanel();
        p.setHeaderVisible(false);
        p.setBodyBorder(false);
        p.setBorders(false);
        p.setScrollMode(Scroll.AUTO);
        p.setWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH
                + AbstractRegistrationForm.DEFAULT_FIELD_WIDTH / 2
                + AbstractRegistrationForm.PANEL_MARGIN);
        p.setLabelWidth(AbstractRegistrationForm.DEFAULT_LABEL_WIDTH);
        p.setFieldWidth(AbstractRegistrationForm.DEFAULT_FIELD_WIDTH / 2);
        return p;
    }

    private static Map<EntityKind, Field<?>> createEntitySelectionMap(
            SampleChooserFieldAdaptor sampleChooser,
            ExperimentChooserFieldAdaptor experimentChooser, MaterialChooserField materialChooser,
            DataSetChooserField datasetChooser)
    {
        Map<EntityKind, Field<?>> m = new HashMap<EntityKind, Field<?>>();
        m.put(EntityKind.SAMPLE, sampleChooser.getChooserField());
        m.put(EntityKind.EXPERIMENT, experimentChooser.getChooserField());
        m.put(EntityKind.MATERIAL, materialChooser);
        m.put(EntityKind.DATA_SET, datasetChooser);
        return m;
    }
}
