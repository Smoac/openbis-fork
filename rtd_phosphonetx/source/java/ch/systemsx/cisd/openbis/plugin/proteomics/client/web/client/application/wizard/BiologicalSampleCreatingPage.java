/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeFieldWithGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ClickableFormPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard.WizardPage;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedInputWidgetDescription;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.AbstractGenericSampleRegisterEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.SamplePropertyEditor;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * Wizard for guiding the user to annotate an MS_INJECTION sample.
 *
 * @author Franz-Josef Elmer
 */
public class BiologicalSampleCreatingPage extends WizardPage<MsInjectionSampleAnnotationModel>
{

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    private ClickableFormPanel formPanel;

    private SampleTypeSelectionWidget sampleTypeSelectionWidget;

    private SampleType sampleType;

    private CodeFieldWithGenerator codeField;

    private SpaceSelectionWidget spaceSelectionWidget;

    private SamplePropertyEditor samplePropertyEditor;

    private ExperimentChooserFieldAdaptor experimentField;

    private Map<String, List<IManagedInputWidgetDescription>> inputWidgetDescriptions = Collections
            .<String, List<IManagedInputWidgetDescription>> emptyMap();

    public BiologicalSampleCreatingPage(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            MsInjectionSampleAnnotationModel model)
    {
        super(viewContext, MsInjectionAnnotationWizardState.BIOLOGICAL_SAMPLE_CREATING, model);
        this.viewContext = viewContext;
        setLeftContentByDictionary();
    }

    @Override
    public void init()
    {
        formPanel = new ClickableFormPanel();
        formPanel.setHeaderVisible(false);
        sampleTypeSelectionWidget = new SampleTypeSelectionWidget(viewContext, "bio-samp", true,
                SampleTypeDisplayID.SAMPLE_REGISTRATION, null)
            {

                @Override
                protected void filterTypes(List<SampleType> types)
                {
                    for (Iterator<SampleType> iterator = types.iterator(); iterator.hasNext();)
                    {
                        SampleType type = iterator.next();
                        if (type.getCode().startsWith("BIO") == false)
                        {
                            iterator.remove();
                        }
                    }
                }
            };
        sampleTypeSelectionWidget.addSelectionChangedListener(new SelectionChangedListener<SampleTypeModel>()
            {

                @Override
                public void selectionChanged(SelectionChangedEvent<SampleTypeModel> se)
                {
                    SampleType sampleTypeOrNull = sampleTypeSelectionWidget.tryGetSelectedSampleType();
                    if (sampleTypeOrNull != null)
                    {
                        onSampleTypeChanged(sampleTypeOrNull);
                    }
                }
            });
        formPanel.add(sampleTypeSelectionWidget);
        addToRightContent(formPanel, new RowData(1, 600, new Margins(10)));
    }

    protected void onSampleTypeChanged(SampleType type)
    {
        this.sampleType = type;
        formPanel.removeAll();
        formPanel.add(sampleTypeSelectionWidget);
        codeField =
                new CodeFieldWithGenerator(viewContext, viewContext.getMessage(Dict.CODE),
                        type.getGeneratedCodePrefix(), EntityKind.SAMPLE,
                        type.isAutoGeneratedCode());
        boolean codeReadonly = type.isAutoGeneratedCode();
        codeField.setReadOnly(codeReadonly);
        codeField.setHideTrigger(codeReadonly);
        formPanel.add(codeField);
        List<SampleTypePropertyType> types = type.getAssignedPropertyTypes();
        spaceSelectionWidget =
                new SpaceSelectionWidget(viewContext, getId(), true, false, viewContext.getModel()
                        .getSessionContext().getUser().getHomeGroupCode());
        FieldUtil.markAsMandatory(spaceSelectionWidget);
        formPanel.add(spaceSelectionWidget);
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        experimentField = ExperimentChooserField.create(label, false, null,
                viewContext.getCommonViewContext());
        formPanel.add(experimentField.getChooserField());

        samplePropertyEditor =
                new SamplePropertyEditor("bio-s", inputWidgetDescriptions,
                        viewContext.getCommonViewContext());
        samplePropertyEditor.initWithoutProperties(types);
        samplePropertyEditor.addPropertyFieldsWithFieldsetToPanel(formPanel);
        formPanel.layout();
        formPanel.addClickListener(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    enableNextButton(formPanel.isValid());
                }
            });
    }

    @Override
    public void deactivate()
    {
        String identifier =
                AbstractGenericSampleRegisterEditForm.createSampleIdentifier(spaceSelectionWidget,
                        codeField);
        String experimentIdentifierOrNull = experimentField.getChooserField().getValue();
        List<IEntityProperty> properties = samplePropertyEditor.extractProperties();
        model.defineBiologicalSample(sampleType, identifier, experimentIdentifierOrNull, properties);
    }

    @Override
    public void destroy()
    {
        spaceSelectionWidget.dispose();
    }

}
