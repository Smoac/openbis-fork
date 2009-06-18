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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.EntityChooserDialog.ChosenEntitySetter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/**
 * A field for selecting a sample from a list or by specifying sample identifier.
 * 
 * @author Piotr Buczek
 */
public final class SampleChooserField extends ChosenEntitySetter<Sample>
{
    public interface SampleChooserFieldAdaptor
    {
        Field<?> getField();

        /** @return the sample identifier (as a string) which is set as a field value */
        String getValue();

        void updateOriginalValue();
    }

    /**
     * Creates a text field with the additional browse button which allow to choose a sample from
     * the list.
     */
    public static SampleChooserFieldAdaptor create(final String labelField,
            final boolean mandatory, final String initialValueOrNull, final boolean addShared,
            final boolean excludeWithoutExperiment,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final SampleChooserField chooserField =
                new SampleChooserField(mandatory, initialValueOrNull, viewContext);

        Button chooseButton = new Button(viewContext.getMessage(Dict.BUTTON_BROWSE));
        chooseButton.addSelectionListener(new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    browse(viewContext, chooserField, addShared, excludeWithoutExperiment);
                }
            });
        final Field<?> field =
                new MultiField<Field<?>>(labelField, chooserField, new AdapterField(chooseButton));
        FieldUtil.setMandatoryFlag(field, mandatory);
        return asSampleChooserFieldAdaptor(chooserField, field);
    }

    private static SampleChooserFieldAdaptor asSampleChooserFieldAdaptor(
            final SampleChooserField chooserField, final Field<?> field)
    {
        return new SampleChooserFieldAdaptor()
            {
                public Field<?> getField()
                {
                    return field;
                }

                public String getValue()
                {
                    return chooserField.getValue();
                }

                public void updateOriginalValue()
                {
                    String valueOrNull = getValue();
                    String textValue = (valueOrNull == null ? "" : valueOrNull);
                    chooserField.setOriginalValue(textValue);
                }
            };
    }

    private static void browse(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<Sample> chosenSampleField, final boolean addShared,
            final boolean excludeWithoutExperiment)
    {
        DisposableEntityChooser<Sample> browser =
                SampleBrowserGrid.createChooser(viewContext, addShared, excludeWithoutExperiment);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_EXPERIMENT);
        new EntityChooserDialog<Sample>(browser, chosenSampleField, title, viewContext).show();
    }

    // ------------------

    private final boolean mandatory;

    @Override
    public void setChosenEntity(Sample entityOrNull)
    {
        if (entityOrNull != null)
        {
            setValue(entityOrNull.getIdentifier());
        }
    }

    public SampleChooserField(boolean mandatory, String initialValueOrNull,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.mandatory = mandatory;

        setValidateOnBlur(true);
        setAutoValidate(true);

        // no regexp validation is done
        // we use plain string identifiers which currently can be parsed only on the server side

        if (initialValueOrNull != null)
        {
            setValue(initialValueOrNull);
        }
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