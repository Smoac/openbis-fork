/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;

/**
 * {@link ComboBox} which loads data, allows filtering and requires selection.
 * 
 * @author Izabela Adamczyk
 */
public abstract class DropDownList<M extends ModelData, E> extends SimpleDropDownList<M, E>
{

    private final IViewContext<?> viewContext;

    public DropDownList(final IViewContext<?> viewContext, String idSuffix, String labelDictCode,
            String displayField, String chooseSuffix, String nothingFoundSuffix)
    {
        super(idSuffix, displayField, viewContext.getMessage(labelDictCode), viewContext
                .getMessage(Dict.COMBO_BOX_CHOOSE, chooseSuffix), viewContext.getMessage(
                Dict.COMBO_BOX_EMPTY, nothingFoundSuffix), viewContext
                .getMessage(Dict.COMBO_BOX_EXPECTED_VALUE_FROM_THE_LIST), true);
        this.viewContext = viewContext;
    }

    abstract protected void loadData(AbstractAsyncCallback<List<E>> callback);

    abstract protected List<M> convertItems(final List<E> result);

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        loadData(new ListItemsCallback(viewContext));
    }

    public class ListItemsCallback extends AbstractAsyncCallback<List<E>>
    {

        protected ListItemsCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        public void process(final List<E> result)
        {
            List<M> convertedItems = convertItems(result);
            updateStore(convertedItems);
        }
    }

}