package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

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
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public PermlinkLocatorResolver(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(ViewLocator.PERMLINK_ACTION);
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
            openInitialEntityViewer(entityKindValueOrNull, permIdValueOrNull);
        }
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
        EntityKind entityKind = getEntityKind(entityKindValue);
        OpenEntityDetailsTabHelper.open(viewContext, entityKind, permIdValue, false);
    }

}