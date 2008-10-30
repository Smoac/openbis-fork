/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.TabItem;

/**
 * An interface which contains everything to create a {@link TabItem}.
 * 
 * @author Christian Ribeaud
 */
public interface ITabItem
{

    /**
     * Returns the title of this tab item.
     * <p>
     * Is typically used in {@link TabItem#setText(String)}.
     * </p>
     */
    public String getTitle();

    /**
     * Returns the {@link Component} which composes this tab item.
     */
    public Component getComponent();
}
