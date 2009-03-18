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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.EntityChooserDialog.ChosenEntitySetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;

/**
 * A field for selecting an experiment in a fixed group from a list or by specifying code and
 * project.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentChooserField extends TextField<String> implements
        ChosenEntitySetter<Experiment>
{
    public interface ExperimentChooserFieldAdaptor
    {
        Field<?> getField();

        /** @return the experiment identifier which is set as a field value */
        ExperimentIdentifier getValue();

        void updateOriginalValue();
    }

    /**
     * Creates a text field with the additional browse button which allow to choose an experiment
     * from the list.
     */
    public static ExperimentChooserFieldAdaptor create(final String labelField,
            final boolean mandatory, final Group group,
            final ExperimentIdentifier initialValueOrNull,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final ExperimentChooserField chooserField =
                new ExperimentChooserField(mandatory, group, initialValueOrNull, viewContext);

        Button chooseButton = new Button(viewContext.getMessage(Dict.BUTTON_BROWSE));
        chooseButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    browse(viewContext, chooserField, group);
                }
            });
        final Field<?> field =
                new MultiField<Field<?>>(labelField, chooserField, new AdapterField(chooseButton));
        return asExperimentChooserFieldAdaptor(chooserField, field);
    }

    private static ExperimentChooserFieldAdaptor asExperimentChooserFieldAdaptor(
            final ExperimentChooserField chooserField, final Field<?> field)
    {
        return new ExperimentChooserFieldAdaptor()
            {
                public Field<?> getField()
                {
                    return field;
                }

                public ExperimentIdentifier getValue()
                {
                    return chooserField.tryGetIdentifier();
                }

                public void updateOriginalValue()
                {
                    ExperimentIdentifier valueOrNull = getValue();
                    String textValue = (valueOrNull == null ? "" : valueOrNull.print());
                    chooserField.setOriginalValue(textValue);
                }
            };
    }

    private static void browse(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<Experiment> chosenMaterialField, Group group)
    {
        DisposableEntityChooser<Experiment> browser =
                ExperimentBrowserGrid.createChooser(viewContext, group);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_EXPERIMENT);
        new EntityChooserDialog<Experiment>(browser, chosenMaterialField, title, viewContext)
                .show();
    }

    // ------------------

    // the pattern used to validate experiemnt pointer expression
    private final static String CODE_AND_PROJECT_PATTERN =
            CodeField.CODE_CHARS + " " + "\\(" + CodeField.CODE_CHARS + "\\)";

    private final boolean mandatory;

    private final Group group;

    public void setChosenEntity(Experiment entityOrNull)
    {
        if (entityOrNull != null)
        {
            ExperimentIdentifier chosenEntity = ExperimentIdentifier.createIdentifier(entityOrNull);
            setValue(chosenEntity);
        }
    }

    private ExperimentIdentifier tryGetIdentifier()
    {
        return ExperimentIdentifier.tryParseIdentifier(getValue(), group);
    }

    private void setValue(ExperimentIdentifier chosenEntity)
    {
        super.setValue(print(chosenEntity));
    }

    private String print(ExperimentIdentifier chosenEntity)
    {
        return chosenEntity.getExperimentCode() + " (" + chosenEntity.getProjectCode() + ")";
    }

    private ExperimentChooserField(boolean mandatory, Group group,
            ExperimentIdentifier initialValueOrNull,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.mandatory = mandatory;
        this.group = group;

        setValidateOnBlur(true);
        setAutoValidate(true);

        setRegex(CODE_AND_PROJECT_PATTERN);
        getMessages().setRegexText(viewContext.getMessage(Dict.INCORRECT_MATERIAL_SYNTAX));
        if (initialValueOrNull != null)
        {
            setValue(initialValueOrNull);
        }
        FieldUtil.setMandatoryFlag(this, mandatory);
    }

    @Override
    protected boolean validateValue(String val)
    {
        boolean valid = super.validateValue(val);
        if (valid == false)
        {
            return false;
        }
        if (mandatory && getValue() == null)
        {
            forceInvalid(GXT.MESSAGES.textField_blankText());
            return false;
        }
        clearInvalid();
        return true;
    }

}