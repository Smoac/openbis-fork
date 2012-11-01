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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.metaproject.MetaprojectGrid;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * ` A button for selecting a metaproject from a list.
 * 
 * @author pkupczyk
 */
public class MetaprojectChooserButton extends Button implements
        IChosenEntitiesSetter<TableModelRowWithObject<Metaproject>>
{

    public static final String ID_SUFFIX = "_metaproject_chooser_button";

    private Field<?> field;

    private final List<IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>> listeners =
            new ArrayList<IChosenEntitiesListener<TableModelRowWithObject<Metaproject>>>();

    public MetaprojectChooserButton(final IViewContext<?> viewContext, final String idPrefix)
    {
        super(viewContext.getMessage(Dict.ADD_METAPROJECT));

        setId(idPrefix + ID_SUFFIX);
        addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    DisposableEntityChooser<TableModelRowWithObject<Metaproject>> chooserGrid =
                            MetaprojectGrid.createChooser(viewContext);

                    new EntityChooserDialog<TableModelRowWithObject<Metaproject>>(chooserGrid,
                            MetaprojectChooserButton.this,
                            viewContext.getMessage(Dict.CHOOSE_METAPROJECT), viewContext).show();
                }

            });
    }

    @Override
    public void setChosenEntities(List<TableModelRowWithObject<Metaproject>> entities)
    {
        if (entities == null)
        {
            return;
        }
        for (IChosenEntitiesListener<TableModelRowWithObject<Metaproject>> listener : listeners)
        {
            listener.entitiesChosen(entities);
        }
    }

    public void addChosenEntityListener(
            IChosenEntitiesListener<TableModelRowWithObject<Metaproject>> listener)
    {
        listeners.add(listener);
    }

    public Field<?> getField()
    {
        if (field == null)
        {
            field = new AdapterField(this);
            field.setLabelSeparator("");
        }
        return field;
    }

}
