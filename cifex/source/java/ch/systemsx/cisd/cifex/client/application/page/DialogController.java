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

package ch.systemsx.cisd.cifex.client.application.page;

import com.extjs.gxt.ui.client.widget.Dialog;

import ch.systemsx.cisd.cifex.client.application.ViewContext;

/**
 * A common superclass for the various dialogs in CIFEX
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class DialogController
{

    protected final ViewContext context;

    protected final String panelTitle;

    protected DialogController(final ViewContext context, final String panelTitle)
    {
        super();
        this.context = context;
        this.panelTitle = panelTitle;
    }

    /**
     * The method that subclasses need to override.
     */
    public abstract Dialog getDialog();

}