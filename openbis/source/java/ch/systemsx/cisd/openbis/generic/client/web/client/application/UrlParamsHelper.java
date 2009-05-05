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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * A class with helper methods for URL parameters handling and opening initial tab.
 * 
 * @author Piotr Buczek
 */
public final class UrlParamsHelper
{

    /** parameter key used to open an initial tab */
    private static final String INITIAL_TAB_OPEN_PARAMETER_KEY = "show";

    private static final String INITIAL_TAB_OPEN_PARAMETER_VALUE_SEPARATOR = "/";

    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final String PARAMETER_SEPARATOR = "&";

    /**
     * Parses given URL <var>string</var> and returns the key-value pairs
     */
    private final static Map<String, String> parseParamString(final String string)
    {
        assert string != null : "Given text can not be null.";
        final String[] params = string.split(PARAMETER_SEPARATOR);
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < params.length; i++)
        {
            final String[] keyVal = params[i].split(KEY_VALUE_SEPARATOR);
            assert keyVal.length == 2 : "Only two items should be found here.";
            map.put(keyVal[0], keyVal[1]);
        }
        return map;
    }

    private final static String getBaseIndexURL()
    {
        return GWT.getModuleBaseURL() + "index.html";
    }

    public final static String createURL(final EntityKind entityKind, final String identifier)
    {
        URLMethodWithParameters ulrWithParameters = new URLMethodWithParameters(getBaseIndexURL());
        ulrWithParameters.addParameter(INITIAL_TAB_OPEN_PARAMETER_KEY, entityKind.name()
                + INITIAL_TAB_OPEN_PARAMETER_VALUE_SEPARATOR + identifier);
        return ulrWithParameters.toString();
    }

    /**
     * The URL parameters.
     * <p>
     * Is never <code>null</code> but could be empty.
     * </p>
     */
    private Map<String, String> urlParams = new HashMap<String, String>();

    private IViewContext<?> viewContext;

    public UrlParamsHelper(IViewContext<?> viewContext)
    {
        this.viewContext = viewContext;
    }

    private final Map<String, String> getUrlParams()
    {
        return urlParams;
    }

    private final void setUrlParams(final Map<String, String> urlParams)
    {
        assert urlParams != null : "URL params can not be null.";
        this.urlParams = urlParams;
    }

    public final void initUrlParams()
    {
        final String paramString = GWTUtils.getParamString();
        if (StringUtils.isBlank(paramString) == false)
        {
            setUrlParams(parseParamString(paramString));
        }
    }

    public final IDelegatedAction getOpenInitialTabAction()
    {
        return new OpenInitialTabAction();
    }

    private class OpenInitialTabAction implements IDelegatedAction
    {

        public void execute()
        {
            openInitialTab();
        }

        /** opens an initial tab if a parameter is specified in URL */
        private final void openInitialTab()
        {
            String paramValueOrNull = tryGetInitialTabOpenParamValue();
            if (paramValueOrNull != null)
            {
                try
                {
                    String paramParts[] =
                            paramValueOrNull.split(INITIAL_TAB_OPEN_PARAMETER_VALUE_SEPARATOR);
                    assert paramParts.length == 2 : "There should be only two parts.";

                    final String entityKindPart = paramParts[0];
                    final String identifierPart = paramParts[1];

                    final EntityKind entityKind = EntityKind.valueOf(entityKindPart);
                    final String identifier = identifierPart;

                    openEntityDetailsTab(entityKind, identifier);
                } catch (Throwable exception)
                {
                    throw new UserFailureException("Invalid URL parameter.");
                    // TODO 2009-05-05, Piotr Buczek: show InfoBox (cannot add it anywhere now)
                    // InfoBox infoBox = new InfoBox();
                    // infoBox.displayError("Invalid URL parameter.");
                }
            }
        }

        private String tryGetInitialTabOpenParamValue()
        {
            return getUrlParams().get(INITIAL_TAB_OPEN_PARAMETER_KEY);
        }

        private void openEntityDetailsTab(EntityKind entityKind, String identifier)
        {
            viewContext.getCommonService().getEntityInformationHolder(entityKind, identifier,
                    new OpenEntityDetailsTabCallback(viewContext));

        }

    }

    private final class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolder>
    {

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
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
            new OpenEntityDetailsTabAction(result, viewContext).execute();
        }
    }

}
