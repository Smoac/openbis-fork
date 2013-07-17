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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.utils.IValidator;

/**
 * A widget which allows to add and remove items. The items are little boxes with a delete element for interactive deletion.
 * 
 * @author Franz-Josef Elmer
 */
public class ItemsWidget extends FlowPanel
{
    private SuggestBox box;

    private List<Item> items = new ArrayList<Item>();

    private int minimumInputFieldSize = 40;

    private final IValidator validatorOrNull;
    
    private final Set<IItemsWidgetChangeListener> changeListeners = new LinkedHashSet<IItemsWidgetChangeListener>();

    private boolean readOnly;

    /**
     * Creates a new instance for the specified oracle and/or validator. At least one of the arguments has to be not <code>null</code>.
     * 
     * @param oracleOrNull Suggestion oracle which allows to provide a list of selectable items to be added.
     * @param validatorOrNull Validator which checks every input. If <code>null</code> every input is valid.
     */
    public ItemsWidget(SuggestOracle oracleOrNull, final IValidator validatorOrNull)
    {
        this.validatorOrNull = validatorOrNull;
        box = oracleOrNull == null ? new SuggestBox() : new SuggestBox(oracleOrNull);
        setTextInputFieldWithForNoItems();
        box.getElement().setClassName("items-widget-text-field");
        add(box);
        if (validatorOrNull != null)
        {
            box.addValueChangeHandler(new ValueChangeHandler<String>()
                {
                    @Override
                    public void onValueChange(ValueChangeEvent<String> event)
                    {
                        if (readOnly == false)
                        {
                            addItem(event.getValue());
                        }
                    }
                });
        }
        box.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>()
            {
                @Override
                public void onSelection(SelectionEvent<Suggestion> event)
                {
                    if (readOnly == false)
                    {
                        addItem(box.getValue());
                    }
                }
            });
        getElement().setClassName("items-widget");
    }
    
    /**
     * Adds a change listener. It will be informed after an item ahs been added or deleted.
     */
    public void addItemsChangeListener(IItemsWidgetChangeListener listener)
    {
        changeListeners.add(listener);
    }
    
    @Override
    protected void onAttach()
    {
        super.onAttach();
    }

    /**
     * Sets minimum size of the input field for entering new items. Default value is 40.
     */
    public void setMinimumInputFieldSize(int minimumInputFieldSize)
    {
        this.minimumInputFieldSize = minimumInputFieldSize;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
        box.setVisible(readOnly == false);
    }

    public List<String> getItems()
    {
        List<String> result = new ArrayList<String>();
        for (Item item : items)
        {
            result.add(item.itemString);
        }
        return result;
    }

    public void addItem(String itemString)
    {
        if (validatorOrNull != null && validatorOrNull.validate(itemString) != null)
        {
            return;
        }
        Item item = createItem(itemString);
        items.add(item);
        Widget itemWidget = item.itemWidget;
        insert(itemWidget, getWidgetCount() - 1);
        InlineLabel space = new InlineLabel(" ");
        space.getElement().setClassName("items-widget-space");
        insert(space, getWidgetCount() - 1);
        adjust();
        box.setText("");
    }

    public void delete(Widget itemWidget)
    {
        int index = getWidgetIndex(itemWidget);
        if (index >= 0)
        {
            items.remove(index / 2);
            remove(index + 1);
            remove(index);
            adjust();
        }
    }

    private void adjust()
    {
        if (items.isEmpty())
        {
            setTextInputFieldWithForNoItems();
        } else
        {
            adjustInputFieldWidth(items.get(items.size() - 1).itemWidget);
        }
        List<String> itemsAsString = getItems();
        for (IItemsWidgetChangeListener changeListener : changeListeners)
        {
            changeListener.itemsChanged(itemsAsString);
        }
    }

    private Item createItem(String itemString)
    {
        final FlowPanel panel = new FlowPanel();
        panel.getElement().setClassName("item-widget");

        Label itemLabel = new InlineLabel(itemString);
        itemLabel.getElement().setClassName("item-widget-label");
        panel.add(itemLabel);

        Button deleteButton = new Button("x");
        deleteButton.addClickHandler(new ClickHandler()
            {
                @Override
                public void onClick(ClickEvent event)
                {
                    if (readOnly == false)
                    {
                        delete(panel);
                    }
                }
            });
        deleteButton.getElement().setClassName("item-widget-delete-button");
        panel.add(deleteButton);

        return new Item(itemString, panel);
    }

    private void setTextInputFieldWithForNoItems()
    {
        box.setWidth("100%");
    }

    private void adjustInputFieldWidth(Widget lastItemWidget)
    {
        lastItemWidget.getElement().getStyle().setDisplay(Display.INLINE);

        int leftPositionOfParent = getAbsoluteLeft();
        int leftPositionOfLastElement = lastItemWidget.getAbsoluteLeft();
        int widthOfLastElement = lastItemWidget.getOffsetWidth();
        int fullAreaWidth = getOffsetWidth();
        int componentAreaLeft = fullAreaWidth - (leftPositionOfLastElement - leftPositionOfParent) - widthOfLastElement - 10;

        box.setWidth(componentAreaLeft < minimumInputFieldSize ? "100%" : componentAreaLeft + "px");
    }

    private static final class Item
    {
        private final String itemString;

        private final Widget itemWidget;

        private Item(String itemString, Widget itemWidget)
        {
            this.itemString = itemString;
            this.itemWidget = itemWidget;
        }
    }

}
