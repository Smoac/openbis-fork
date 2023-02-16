/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * ViewLocatorHandler for Permlink locators.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class PermlinkLocatorResolver extends AbstractViewLocatorResolver
{
    protected final IViewContext<ICommonClientServiceAsync> viewContext;

    public PermlinkLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(PermlinkUtilities.PERMLINK_ACTION, viewContext);
    }

    protected PermlinkLocatorResolver(String action, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(action);
        this.viewContext = viewContext;
    }

    @Override
    public void locatorExists(final ViewLocator locator, final AsyncCallback<Void> callback)
    {
        EntityKind entityKindValueOrNull = tryGetEntityKindEnum(locator);
        String permIdValueOrNull = tryGetPermId(locator);

        if (entityKindValueOrNull != null && permIdValueOrNull != null)
        {
            viewContext.getCommonService().getEntityInformationHolder(entityKindValueOrNull,
                    permIdValueOrNull,
                    new LocatorExistsCallback<IEntityInformationHolderWithPermId>(callback));
            return;
        }

        callback.onFailure(null);
    }

    @Override
    public void resolve(ViewLocator locator) throws UserFailureException
    {
        // If a permlink has been specified, open a viewer on the specified
        // object
        String entityKindValueOrNull = tryGetEntityKind(locator);
        String permIdValueOrNull = tryGetPermId(locator);

        if (null != entityKindValueOrNull || null != permIdValueOrNull)
        {
            // Make sure the permlink has been specified correctly, if not throw
            // an error
            checkRequiredParameter(entityKindValueOrNull,
                    PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
            checkRequiredParameter(permIdValueOrNull, PermlinkUtilities.PERM_ID_PARAMETER_KEY);

            String subtab = parseHistoryToken(locator.getHistoryToken()).get(PermlinkUtilities.SUBTAB_PARAMETER_KEY);

            openInitialEntityViewer(entityKindValueOrNull, permIdValueOrNull, subtab);
        }
    }

    private Map<String, String> parseHistoryToken(String token)
    {
        Map<String, String> map = new HashMap<String, String>();

        for (String parameter : token.split("&"))
        {
            String[] keyval = parameter.split("=", 2);
            if (keyval.length == 2)
            {
                map.put(keyval[0], keyval[1]);
            }
        }

        return map;
    }

    protected String tryGetEntityKind(ViewLocator locator)
    {
        return locator.tryGetEntity();
    }

    protected EntityKind tryGetEntityKindEnum(ViewLocator locator)
    {
        try
        {
            return EntityKind.valueOf(tryGetEntityKind(locator));
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    protected String tryGetPermId(ViewLocator locator)
    {
        return locator.getParameters().get(PermlinkUtilities.PERM_ID_PARAMETER_KEY);
    }

    /**
     * Open the entity details tab for the specified entity kind and permId.
     */
    protected void openInitialEntityViewer(String entityKindValue, String permIdValue)
            throws UserFailureException
    {
        openInitialEntityViewer(entityKindValue, permIdValue, "");
    }

    protected void openInitialEntityViewer(String entityKindValue, String permIdValue, String subtab)
            throws UserFailureException
    {
        EntityKind entityKind = getEntityKind(entityKindValue);
        OpenEntityDetailsTabHelper.open(viewContext, entityKind, permIdValue, false, subtab);
    }

}