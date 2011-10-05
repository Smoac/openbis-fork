/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.PermlinkLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * @author Pawel Glyzewski
 */
public class ImagingDataSetLocatorResolver extends PermlinkLocatorResolver
{
    private final IViewContext<IScreeningClientServiceAsync> viewContext;

    public ImagingDataSetLocatorResolver(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        super(viewContext.getCommonViewContext());
        this.viewContext = viewContext;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        String permIdValueOrNull =
                locator.getParameters().get(PermlinkUtilities.PERM_ID_PARAMETER_KEY);

        return super.canHandleLocator(locator)
                && EntityKind.DATA_SET.name().equals(entityKindValueOrNull)
                && permIdValueOrNull != null && permIdValueOrNull.contains(":");
    }

    /**
     * Open the entity details tab for the specified entity kind and permId.
     */
    @Override
    protected void openInitialEntityViewer(String entityKindValue, String permIdValue)
            throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(entityKindValue);
        int idx = permIdValue.indexOf(':');

        viewContext.getCommonService().getEntityInformationHolder(entityKind,
                permIdValue.substring(0, idx),
                new OpenEntityDetailsTabCallback(viewContext, permIdValue.substring(idx + 1)));
    }

    private static class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolderWithPermId>
    {
        private final String wellLocation;

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext,
                final String wellLocation)
        {
            super(viewContext);
            this.wellLocation = wellLocation;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolderWithPermId result)
        {
            new OpenEntityDetailsTabAction(new IEntityInformationHolderWithPermId()
                {
                    private static final long serialVersionUID = 1L;

                    public BasicEntityType getEntityType()
                    {
                        return result.getEntityType();
                    }

                    public EntityKind getEntityKind()
                    {
                        return result.getEntityKind();
                    }

                    public Long getId()
                    {
                        return result.getId();
                    }

                    public String getCode()
                    {
                        return result.getCode();
                    }

                    public String getPermId()
                    {
                        return result.getPermId() + ":" + wellLocation;
                    }

                }, viewContext, false).execute();
        }
    }
}
