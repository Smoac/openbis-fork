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

package ch.systemsx.cisd.cifex.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget which allows to add and remove items. The items are little boxes with a delete element
 * for interactive deletion.
 *
 * @author Franz-Josef Elmer
 */
public class ItemsWidget extends FlowPanel
{
    private SuggestBox box;
    private List<Item> items = new ArrayList<Item>();
    private int minimumInputFieldSize = 40;

    public ItemsWidget(SuggestOracle oracle)
    {
        box = new SuggestBox(oracle);
        setTextInputFieldWithForNoItems();
        box.getElement().setClassName("items-widget-text-field");
        add(box);
        box.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>()
            {
                @Override
                public void onSelection(SelectionEvent<Suggestion> event)
                {
                    addItem(box.getValue());
                    box.setText("");
                }
            });
        getElement().setClassName("items-widget");
    }
    
    public void setMinimumInputFieldSize(int minimumInputFieldSize)
    {
        this.minimumInputFieldSize = minimumInputFieldSize;
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
        Item item = createItem(itemString);
        items.add(item);
        Widget itemWidget = item.itemWidget;
        insert(itemWidget, getWidgetCount() - 1);
        InlineLabel space = new InlineLabel(" ");
        space.getElement().setClassName("items-widget-space");
        insert(space, getWidgetCount() - 1);
        adjustInputFieldWidth(itemWidget);
    }

    public void delete(Widget itemWidget)
    {
        int index = getWidgetIndex(itemWidget);
        if (index >= 0)
        {
            items.remove(index / 2);
            remove(index + 1);
            remove(index);
            if (items.isEmpty())
            {
                setTextInputFieldWithForNoItems();
            } else
            {
                adjustInputFieldWidth(items.get(items.size() - 1).itemWidget);
            }
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
                    delete(panel);
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
        
        int leftPositionOfParent = getParent().getAbsoluteLeft();
        int leftPositionOfLastElement = lastItemWidget.getAbsoluteLeft();
        int widthOfLastElement = lastItemWidget.getOffsetWidth();
        int fullAreaWidth = getParent().getOffsetWidth();
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
