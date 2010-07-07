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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;

/**
 * {@link ContentPanel} for sections with deferred request handling.
 * <p>
 * If this panel is used outside of {@link SectionsPanel} one needs call
 * {@link #setContentVisible(boolean)} to process requests coming from this section.
 * 
 * @author Izabela Adamczyk
 */
abstract public class SingleSectionPanel extends ContentPanel
{
    /** creates a section content, called when the section is shown for the first time */
    abstract protected void showContent();

    protected final IViewContext<?> viewContext;

    private String displayId;

    private boolean isContentVisible = false;

    /**
     * Whether additional components created for this section (e.g. browsers) should be
     * automatically disposed when the section is detached from its container. For sections that can
     * temporarily removed from container as in {@link SectionsPanel} it should be turned off and
     * the container should dispose section components manually.
     */
    private boolean autoDisposeComponents = true;

    public SingleSectionPanel(final String header, IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
        setHeaderVisible(true);
        setHeading(header);
        setCollapsible(true);
        setAnimCollapse(false);
        setBodyBorder(true);
        setLayout(new FitLayout());
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

    public void setContentVisible(boolean visible)
    {
        if (visible && isContentVisible == false)
        {
            showContent();
            isContentVisible = true;
        }
    }

    public boolean isContentVisible()
    {
        return isContentVisible;
    }

    public void disableAutoDisposeComponents()
    {
        this.autoDisposeComponents = false;
    }

    protected boolean isAutoDisposeComponents()
    {
        return autoDisposeComponents;
    }

    @Override
    protected void onDetach()
    {
        if (isAutoDisposeComponents())
        {
            disposeComponents();
        }
        super.onDetach();
    }

    /** disposes components created for the section (by default does nothing) */
    public void disposeComponents()
    {
    }

}
