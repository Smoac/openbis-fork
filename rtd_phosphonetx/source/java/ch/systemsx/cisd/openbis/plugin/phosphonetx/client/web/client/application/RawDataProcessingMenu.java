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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict.COPY_DATA_SETS_MESSAGE;
import static ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.Dict.COPY_DATA_SETS_TITLE;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.VoidAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedActionWithResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.TextToolItem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RawDataProcessingMenu extends TextToolItem
{
    private static final class CopyConfirmationDialog extends
            AbstractDataConfirmationDialog<List<TableModelRowWithObject<Sample>>>
    {
        private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

        private final List<TableModelRowWithObject<Sample>> samples;

        private final DatastoreServiceDescription datastoreServiceDescription;

        private TextField<String> dataSetTypeField;

        private CopyConfirmationDialog(
                IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext,
                DatastoreServiceDescription datastoreServiceDescription,
                List<TableModelRowWithObject<Sample>> samples, String title)
        {
            super(specificViewContext, samples, title);
            this.specificViewContext = specificViewContext;
            this.datastoreServiceDescription = datastoreServiceDescription;
            this.samples = samples;
            setWidth(400);
        }

        @Override
        protected String createMessage()
        {
            String list = "[";
            String delim = "";
            for (TableModelRow sample : samples)
            {
                list += delim + sample.getValues().get(0);
                delim = ", ";
            }
            list += "]";
            String label = datastoreServiceDescription.getLabel();
            return specificViewContext.getMessage(COPY_DATA_SETS_MESSAGE, label, list);
        }

        @Override
        protected void executeConfirmedAction()
        {
            long[] rawDataSampleIDs = new long[samples.size()];
            for (int i = 0; i < samples.size(); i++)
            {
                rawDataSampleIDs[i] = samples.get(i).getObjectOrNull().getId();
            }
            specificViewContext.getService().processRawData(datastoreServiceDescription.getKey(),
                    rawDataSampleIDs, dataSetTypeField.getValue(),
                    new VoidAsyncCallback<Void>(specificViewContext));
        }

        @Override
        protected void extendForm()
        {
            dataSetTypeField = new TextField<String>();
            dataSetTypeField.setFieldLabel(specificViewContext
                    .getMessage(Dict.COPY_DATA_SETS_DATA_SET_TYPE_FIELD));
            dataSetTypeField.setSelectOnFocus(true);
            dataSetTypeField.setWidth(200);
            formPanel.add(dataSetTypeField);
            formPanel.setLabelWidth(100);
        }
    }

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;
    private final IDelegatedActionWithResult<List<TableModelRowWithObject<Sample>>> selectedDataProvider;
    
    public RawDataProcessingMenu(IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            IDelegatedActionWithResult<List<TableModelRowWithObject<Sample>>> selectedDataProvider)
    {
        super(viewContext.getMessage(Dict.COPY_DATA_SETS_BUTTON_LABEL));
        this.viewContext = viewContext;
        this.selectedDataProvider = selectedDataProvider;
        IViewContext<ICommonClientServiceAsync> commonViewContext =
                viewContext.getCommonViewContext();
        viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.PROCESSING,
                new AbstractAsyncCallback<List<DatastoreServiceDescription>>(commonViewContext)
                    {
                        @Override
                        public final void process(List<DatastoreServiceDescription> plugins)
                        {
                            createMenu(plugins);
                        }
                    });
    }
    
    private void createMenu(List<DatastoreServiceDescription> plugins)
    {
        Menu subMenu = new Menu();

        for (final DatastoreServiceDescription datastoreServiceDescription : plugins)
        {
            IActionMenuItem actionMenuItem = new IActionMenuItem()
                {

                    public String getMenuText(IMessageProvider messageProvider)
                    {
                        return datastoreServiceDescription.getLabel();
                    }

                    public String getMenuId()
                    {
                        return datastoreServiceDescription.getKey();
                    }
                };
            IDelegatedAction action = new IDelegatedAction()
                {

                    public void execute()
                    {
                        String title = viewContext.getMessage(COPY_DATA_SETS_TITLE);
                        List<TableModelRowWithObject<Sample>> selectedSamples = selectedDataProvider.execute();
                        new CopyConfirmationDialog(viewContext, datastoreServiceDescription,
                                selectedSamples, title).show();
                    }
                };
            subMenu.add(new ActionMenu(actionMenuItem, viewContext, action));
        }
        setMenu(subMenu);
    }

}
