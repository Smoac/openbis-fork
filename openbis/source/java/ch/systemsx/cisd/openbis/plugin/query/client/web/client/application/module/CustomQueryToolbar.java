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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineVarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict;

/**
 * The toolbar of query viewer.
 * 
 * @author Piotr Buczek
 */
public class CustomQueryToolbar extends ToolBar implements ICustomQueryProvider
{

    private static final int QUERY_FIELD_WIDTH = 600;

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    private final TextArea queryField;

    private final Button executeButton;

    private IDelegatedAction refreshViewerAction;

    public CustomQueryToolbar(final IViewContext<IQueryClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        this.queryField = createQueryField();
        this.executeButton = createExecuteButton();
        display();
    }

    public void setRefreshViewerAction(IDelegatedAction refreshViewerAction)
    {
        this.refreshViewerAction = refreshViewerAction;
    }

    private Button createExecuteButton()
    {
        return new Button(viewContext.getMessage(Dict.QUERY_EXECUTE),
                new SelectionListener<ButtonEvent>()
                    {
                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            if (queryField.isValid() && refreshViewerAction != null)
                            {
                                refreshViewerAction.execute();
                            }
                        }
                    });
    }

    private TextArea createQueryField()
    {
        TextArea result = new MultilineVarcharField(viewContext.getMessage(Dict.QUERY_TEXT), true);
        result.setWidth(QUERY_FIELD_WIDTH);
        return result;
    }

    private void display()
    {
        setAlignment(HorizontalAlignment.CENTER);
        setBorders(true);
        add(new LabelToolItem(viewContext.getMessage(Dict.QUERY_TEXT)
                + GenericConstants.LABEL_SEPARATOR));
        add(queryField);
        add(executeButton);
    }

	//
	// ICustomQueryProvider
	//

    public String tryGetCustomSQLQuery()
    {
        return queryField.getValue();
    }

}
