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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu;

import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;

/**
 * Item for {@link TopMenu}
 * 
 * @author Izabela Adamczyk
 */
public class TopMenuItem extends TextToolItem
{
    public static final String ICON_STYLE = "icon-menu-show";

    public static final String BUTTON_STYLE = "x-btn-top-menu";

    public TopMenuItem(String name)
    {
        super(name);
        setIconStyle(ICON_STYLE);
        button.addStyleName(BUTTON_STYLE);
    }

}
