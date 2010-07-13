/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ProjectViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;

/**
 * Helper class with methods for opening entity details tab.
 * 
 * @author Piotr Buczek
 */
public class OpenEntityDetailsTabHelper
{
    public static void open(IViewContext<?> viewContext, EntityKind entityKind, String permId,
            boolean keyPressed)
    {
        viewContext.getCommonService().getEntityInformationHolder(entityKind, permId,
                new OpenEntityDetailsTabCallback(viewContext, keyPressed));

    }

    public static void open(IViewContext<?> viewContext, MaterialIdentifier identifier,
            boolean keyPressed) throws UserFailureException
    {
        viewContext.getCommonService().getMaterialInformationHolder(identifier,
                new OpenEntityDetailsTabCallback(viewContext, keyPressed));
    }

    private static class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolder>
    {

        private final boolean keyPressed;

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext,
                final boolean keyPressed)
        {
            super(viewContext);
            this.keyPressed = keyPressed;
        }

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext)
        {
            this(viewContext, false);
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolder result)
        {
            new OpenEntityDetailsTabAction(result, viewContext, keyPressed).execute();
        }
    }

    public static void open(final IViewContext<?> viewContext, final Project project,
            boolean keyPressed)
    {
        AbstractTabItemFactory tabFactory;
        final TechId projectId = TechId.create(project);
        tabFactory = new AbstractTabItemFactory()
            {
                @Override
                public ITabItem create()
                {
                    final DatabaseModificationAwareComponent viewer =
                            ProjectViewer.create(viewContext.getCommonViewContext(), projectId);
                    return DefaultTabItem.create(getViewerTitle(), viewer, viewContext, false);
                }

                @Override
                public String getId()
                {
                    return ProjectViewer.createId(projectId);
                }

                private String getViewerTitle()
                {
                    return AbstractViewer.getTitle(viewContext, Dict.PROJECT, project);
                }

                @Override
                public HelpPageIdentifier getHelpPageIdentifier()
                {
                    return new HelpPageIdentifier(HelpPageDomain.PROJECT, HelpPageAction.VIEW);
                }
            };
        tabFactory.setInBackground(keyPressed);
        DispatcherHelper.dispatchNaviEvent(tabFactory);
    }

}
