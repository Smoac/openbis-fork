/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.common;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.knime.workbench.core.KNIMECorePlugin;

/**
 * Preference page for openBIS KNIME nodes.
 *
 * @author Franz-Josef Elmer
 */
public class OpenBisPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
    public static final String OPENBIS_URLS_KEY = "openbis_urls";

    public OpenBisPreferencePage()
    {
        super("openBIS", null, GRID);
    }

    @Override
    public void init(IWorkbench workbench)
    {
        setPreferenceStore(KNIMECorePlugin.getDefault().getPreferenceStore());
    }
    
    @Override
    protected void createFieldEditors()
    {
        final Shell shell = Display.getDefault().getActiveShell();
        addField(new ListEditor(OPENBIS_URLS_KEY, "List of openBIS URLs which can be used to configure nodes:", getFieldEditorParent())
            {
                @Override
                protected String[] parseString(String stringList)
                {
                    return stringList.split("\n");
                }
                
                @Override
                protected String getNewInputObject()
                {
                    InputDialog inputDialog = new InputDialog(shell, "openBIS URL", 
                            "Enter the base URL of an openBIS application server", "", null);
                    inputDialog.setBlockOnOpen(true);
                    return inputDialog.open() == InputDialog.OK ? inputDialog.getValue() : null;
                }
                
                @Override
                protected String createList(String[] items)
                {
                    StringBuilder builder = new StringBuilder();
                    for (String item : items)
                    {
                        if (builder.length() > 0)
                        {
                            builder.append('\n');
                        }
                        builder.append(item);
                    }
                    return builder.toString();
                }
            });
    }

}
