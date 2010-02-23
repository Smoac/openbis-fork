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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;

/**
 * {@link ContentPanel} configured for Experiment Viewer.
 * 
 * @author Izabela Adamczyk
 */
public class SingleSectionPanel extends ContentPanel
{
    private String displayId;

    private boolean isContentVisible;

    /**
     * A queue used to store requests for data when the selection panel is hidden to avoid
     * retrieving data while the panel is not visible. Subclasses that use abstract browser grids
     * should set the grid's queue to this queue
     */
    private final ServerRequestQueue serverRequestQueue;

    public SingleSectionPanel(final String header)
    {
        setHeaderVisible(true);
        setHeading(header);
        setCollapsible(true);
        setAnimCollapse(false);
        setBodyBorder(true);
        setLayout(new FitLayout());
        isContentVisible = false;
        serverRequestQueue = new ServerRequestQueue();
    }

    public void setDisplayID(IDisplayTypeIDGenerator generator, String suffix)
    {
        if (suffix != null)
        {
            this.displayId = generator.createID(suffix);
        } else
        {
            this.displayId = generator.createID();
        }
    }

    public String getDisplayID()
    {
        if (displayId == null)
        {
            throw new IllegalStateException("Undefined display ID");
        } else
        {
            return displayId;
        }
    }

    public boolean isContentVisible()
    {
        return isContentVisible;
    }

    public void setContentVisible(boolean isContentVisible)
    {
        this.isContentVisible = isContentVisible;
        if (this.isContentVisible)
        {
            serverRequestQueue.processUniqueRequests();
        }
        serverRequestQueue.setProcessImmediately(this.isContentVisible);
    }

    protected ServerRequestQueue getServerRequestQueue()
    {
        return serverRequestQueue;
    }
}
