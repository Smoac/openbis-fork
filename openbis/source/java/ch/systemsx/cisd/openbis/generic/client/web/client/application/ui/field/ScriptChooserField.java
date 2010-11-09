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

import com.extjs.gxt.ui.client.event.ComponentEvent;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ScriptGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;

/**
 * A field for selecting a script.
 * 
 * @author Izabela Adamczyk
 */
public class ScriptChooserField extends ChosenEntitySetter<Script>
{

    public static ScriptChooserField create(final String labelField, final boolean mandatory,
            String initialValueOrNull, final IViewContext<ICommonClientServiceAsync> viewContext,
            final EntityKind entityKindOrNull)
    {
        final ScriptChooserField field =
                new ScriptChooserField(mandatory, initialValueOrNull, viewContext)
                    {
                        @Override
                        protected void onTriggerClick(ComponentEvent ce)
                        {
                            super.onTriggerClick(ce);
                            browseScripts(viewContext, this, entityKindOrNull);
                        }
                    };

        field.setFieldLabel(labelField);
        return field;
    }

    private static void browseScripts(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<Script> chosenScriptField, EntityKind entityKindOrNull)
    {
        DisposableEntityChooser<Script> scriptBrowser =
                ScriptGrid.create(viewContext, entityKindOrNull);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_SCRIPT);
        new EntityChooserDialog<Script>(scriptBrowser, chosenScriptField, title, viewContext)
                .show();
    }

    @Override
    public String renderEntity(Script script)
    {
        return script.getName();
    }

    private ScriptChooserField(boolean mandatory, String initialValueOrNull,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        FieldUtil.setMandatoryFlag(this, mandatory);
        setValidateOnBlur(true);
        setAutoValidate(true);
        if (initialValueOrNull != null)
        {
            setValue(initialValueOrNull);
        }
    }

}
