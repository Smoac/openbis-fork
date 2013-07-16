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

package ch.systemsx.cisd.cifex.client.application.ui.itemswidget;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SuggestOracle;

import ch.systemsx.cisd.cifex.client.application.utils.IValidator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ItemsField extends TextArea
{

    private ItemsWidget itemsWidget;
    private Element hiddenTextField;

    public ItemsField(SuggestOracle oracleOrNull, final IValidator validatorOrNull)
    {
        itemsWidget = new ItemsWidget(oracleOrNull, validatorOrNull);
        itemsWidget.addItemsChangeListener(new IItemsWidgetChangeListener()
            {
                @Override
                public void itemsChanged(List<String> items)
                {
                    StringBuilder builder = new StringBuilder();
                    for (String item : items)
                    {
                        if (builder.length() > 0)
                        {
                            builder.append(' ');
                        }
                        builder.append(item);
                    }
                    String renderedItems = builder.toString();
                    hiddenTextField.setAttribute("value", renderedItems);
                    setValue(renderedItems);
                }
            });
        hiddenTextField = DOM.createInputText();
        hiddenTextField.setAttribute("type", "hidden");
    }

    public final String[] getItems()
    {
        return itemsWidget.getItems().toArray(new String[0]);
    }

    public final void setItems(final String[] items)
    {
        if (items != null && items.length != 0)
        {
            for (int i = 0; i < items.length; i++)
            {
                addItem(items[i]);
            }
        }
    }

    @Override
    public void setName(String name)
    {
        super.setName(name);
        hiddenTextField.setAttribute("name", name);
    }

    /**
     * Adds specified item. Does nothing if the item is invalid.
     */
    public final void addItem(final String item)
    {
        itemsWidget.addItem(item);
    }
    
    @Override
    protected void onRender(Element target, int index)
    {
        if (el() == null)
        {
            setElement(DOM.createDiv(), target, index);
            getElement().appendChild(itemsWidget.getElement());
            getElement().appendChild(hiddenTextField);
            input = el().firstChild();
        }

        getInputEl().dom.setPropertyString("autocomplete", "off");
        super.onRender(target, index);
        getElement().setClassName(""); // get rid of x-form-field-wrap which prevents wrapping around items
    }

    @Override
    protected void onAttach()
    {
        itemsWidget.onAttach();
    }
}
    