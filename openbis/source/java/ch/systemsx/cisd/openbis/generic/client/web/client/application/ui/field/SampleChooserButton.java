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

import java.util.LinkedHashSet;
import java.util.Set;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.SampleTypeDisplayID;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid2;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * ` A button for selecting a sample from a list.
 * 
 * @author Piotr Buczek
 */
public class SampleChooserButton extends Button implements IChosenEntitySetter<TableModelRowWithObject<Sample>>
{
    public interface SampleChooserButtonAdaptor
    {
        Field<?> getField();

        SampleChooserButton getChooserButton();

        /** @return the sample identifier (as a string) which is set as a field value */
        String getValue();
    }

    private final Set<IChosenEntityListener<TableModelRowWithObject<Sample>>> listeners =
            new LinkedHashSet<IChosenEntityListener<TableModelRowWithObject<Sample>>>();

    /**
     * Creates a text field with the additional browse button which allow to choose a sample from
     * the list.
     */
    public static SampleChooserButtonAdaptor create(final String labelField,
            final String buttonText, final boolean addShared, boolean addAll,
            final boolean excludeWithoutExperiment,
            final IViewContext<ICommonClientServiceAsync> viewContext,
            SampleTypeDisplayID sampleTypeDisplayID)
    {
        return create(labelField, buttonText, addShared, addAll, excludeWithoutExperiment,
                viewContext, null, sampleTypeDisplayID);

    }

    public static SampleChooserButtonAdaptor create(final String labelFieldOrNull,
            final String buttonText, final boolean addShared, final boolean addAll,
            final boolean excludeWithoutExperiment,
            final IViewContext<ICommonClientServiceAsync> viewContext, String idOrNull,
            final SampleTypeDisplayID sampleTypeDisplayID)
    {
        final SampleChooserButton chooserButton = new SampleChooserButton(viewContext, buttonText);
        chooserButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    browse(viewContext, chooserButton, addShared, addAll, excludeWithoutExperiment,
                            sampleTypeDisplayID);
                }

            });
        if (idOrNull != null)
        {
            chooserButton.setId(idOrNull);
        }
        final SampleChooserButtonAdaptor adaptor = asSampleChooserFieldAdaptor(chooserButton);
        Field<?> field = adaptor.getField();
        field.setLabelSeparator("");
        return adaptor;
    }

    private static SampleChooserButtonAdaptor asSampleChooserFieldAdaptor(
            final SampleChooserButton chooserButton)
    {
        final Field<?> chooserField = new AdapterField(chooserButton);
        return new SampleChooserButtonAdaptor()
            {
                public Field<?> getField()
                {
                    return chooserField;
                }

                public SampleChooserButton getChooserButton()
                {
                    return chooserButton;
                }

                public String getValue()
                {
                    return chooserButton.getValue();
                }

            };
    }

    private static void browse(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IChosenEntitySetter<TableModelRowWithObject<Sample>> chooserSampleSetter, final boolean addShared,
            boolean addAll, final boolean excludeWithoutExperiment,
            SampleTypeDisplayID sampleTypeDisplayID)
    {
        DisposableEntityChooser<TableModelRowWithObject<Sample>> browser =
                SampleBrowserGrid2.createChooser(viewContext, addShared, addAll,
                        excludeWithoutExperiment, sampleTypeDisplayID);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_SAMPLE);
        new EntityChooserDialog<TableModelRowWithObject<Sample>>(browser, chooserSampleSetter, title, viewContext).show();
    }

    // ------------------

    // @Override
    public String renderEntity(TableModelRowWithObject<Sample> entityOrNull)
    {
        return entityOrNull.getObjectOrNull().getIdentifier();
    }

    private SampleChooserButton(final IViewContext<ICommonClientServiceAsync> viewContext,
            final String buttonText)
    {
        super(buttonText);
    }

    public void setChosenEntity(TableModelRowWithObject<Sample> entityOrNull)
    {
        if (entityOrNull != null)
        {
            setValue(renderEntity(entityOrNull));
        }
        for (IChosenEntityListener<TableModelRowWithObject<Sample>> listener : listeners)
        {
            listener.entityChosen(entityOrNull);
        }
    }

    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void addChosenEntityListener(IChosenEntityListener<TableModelRowWithObject<Sample>> listener)
    {
        listeners.add(listener);
    }

}
