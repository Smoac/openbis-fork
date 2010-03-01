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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;

/**
 * A view locator represents the information necessary to open a view including populating it any
 * parameters. The concept is similar to a URL (Universal Resource Locator), but made specific to
 * view in openBIS.
 * <p>
 * The view locator may be initialized from URL-encoded parameters. One parameter, ACTION, is
 * required. ENTITY, though not required, is often used. These two parameters are thus handled
 * specially.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewLocator
{
    // Constants
    private static final String KEY_VALUE_SEPARATOR = "=";

    private static final String PARAMETER_SEPARATOR = "&";

    public static final String ACTION_PARAMETER = "ACTION";

    public static final String ENTITY_PARAMETER = PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY;

    private static final String VIEW_ACTION = "VIEW";

    private static final String SEARCH_ACTION = "SEARCH";

    // Instance Variables
    private String actionOrNull;

    private String entityOrNull;

    // A map of all parameters, excluding action and entity
    private final Map<String, String> parameters = new HashMap<String, String>();

    // Public API
    /**
     * Create a ViewLocator initialized from the url parameters
     * 
     * @param urlParams
     */
    public ViewLocator(String urlParams)
    {
        initializeFromURLParameterString(urlParams);
    }

    /**
     * The action parameter for the view locator. If the locator is valid, then action is non-null.
     */
    public String tryGetAction()
    {
        return actionOrNull;
    }

    /**
     * The entity view for this view locator
     */
    public String tryGetEntity()
    {
        return entityOrNull;
    }

    /**
     * A map of all parameters, excluding action and entity. Do not modify the returned map.
     */
    public Map<String, String> getParameters()
    {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Return true if this view locator meets the minimal criteria for validity. If the locator is
     * valid, then action is non-null.
     */
    public boolean isValid()
    {
        return actionOrNull != null;
    }

    /**
     * Return true if this view locator does not meet the minimal criteria for validity.
     */
    public boolean isInvalid()
    {
        return isValid() == false;
    }

    // Private methods
    /**
     * Extract the information for locating a view from the URL parameters
     */
    private void initializeFromURLParameterString(String urlParams)
    {
        assert urlParams != null;

        final String[] params = urlParams.split(PARAMETER_SEPARATOR);
        for (int i = 0; i < params.length; i++)
        {
            final String[] paramPair = params[i].split(KEY_VALUE_SEPARATOR);
            assert paramPair.length == 2 : "Incorrectly formatted URL parameters";

            if (ACTION_PARAMETER.equalsIgnoreCase(paramPair[0]))
            {
                actionOrNull = paramPair[1];
            } else if (ENTITY_PARAMETER.equalsIgnoreCase(paramPair[0]))
            {
                entityOrNull = paramPair[1];
            } else if (PermlinkUtilities.PERM_ID_PARAMETER_KEY.equalsIgnoreCase(paramPair[0]))
            {
                // Permlink URLs have an implied action
                actionOrNull = VIEW_ACTION;
                parameters.put(paramPair[0], paramPair[1]);
            } else if (SearchlinkUtilities.ENTITY_KIND_PARAMETER_KEY.equalsIgnoreCase(paramPair[0]))
            {
                // Search URLs have an implied action as well
                actionOrNull = SEARCH_ACTION;
                parameters.put(paramPair[0], paramPair[1]);
            } else
            {
                parameters.put(paramPair[0], paramPair[1]);
            }
        }
    }
}
