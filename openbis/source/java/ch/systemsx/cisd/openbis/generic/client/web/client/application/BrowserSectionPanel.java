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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;

/**
 * Panel of a details view showing data in a browser (i.e. table).
 *
 * @author Franz-Josef Elmer
 */
public class BrowserSectionPanel extends SectionPanel
{
    private final IDisposableComponent disposableBrowser;

    /**
     * Creates section with specified header and disposable browser.
     */
    public BrowserSectionPanel(String header, IDisposableComponent disposableBrowser)
    {
        super(header);
        this.disposableBrowser = disposableBrowser;
        add(disposableBrowser.getComponent());
    }

    public IDatabaseModificationObserver getDatabaseModificationObserver()
    {
        return disposableBrowser;
    }

    @Override
    protected void onDetach()
    {
        disposableBrowser.dispose();
        super.onDetach();
    }
}
