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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.extjs.gxt.ui.client.widget.tree.TreeItemUI;

/**
 * A generic @{link TreeItem} extension with {@link ModelData} and an optional
 * {@link TreeItemAction} executed on select.
 * 
 * @author Piotr Buczek
 */
public class TreeItemWithModel extends TreeItem
{

    /**
     * Constructor of an item with given model and text equal to this models toString value, and no
     * action executed on select.
     */
    public TreeItemWithModel(ModelData model)
    {
        this(model, model.toString());
    }

    /**
     * Constructor of an item with given model and text equal to this models toString value, and
     * given action executed on select.
     */
    public TreeItemWithModel(ModelData model, TreeItemAction action)
    {
        this(model);
        this.setUI(new TreeItemUIWithActionOnSelect(this, action));
    }

    /** Constructor of an item with given model and text. */
    private TreeItemWithModel(ModelData model, String text)
    {
        super(text);
        setModel(model);
    }

    //
    // Helper classes
    //

    /** An action to be executed on tree item. */
    public interface TreeItemAction
    {
        public void execute(TreeItem treeItem);
    }

    /** A {@link TreeItemUI} extension that adds a {@link TreeItemAction} execution on select. */
    private final class TreeItemUIWithActionOnSelect extends TreeItemUI
    {

        private TreeItemAction action;

        public TreeItemUIWithActionOnSelect(TreeItem item, TreeItemAction action)
        {
            super(item);
            this.action = action;
        }

        @Override
        public void onSelectedChange(boolean selected)
        {
            super.onSelectedChange(selected);
            if (selected)
            {
                action.execute(item);
            }
        }
    }
}
