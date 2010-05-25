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

import com.extjs.gxt.ui.client.widget.TabPanel;

/**
 * Abstract superclass that defines the interface for tab panels.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class MainPageTabPanel extends TabPanel
{

    /**
     * An enum for the various tabs of the tab panel
     */
    public static enum Tab
    {
        SHARE_TAB, INBOX_TAB, INVITE_TAB, ADMIN_TAB
    }

    public MainPageTabPanel()
    {
        super();

    }

    public abstract void showTab(Tab tab);

}