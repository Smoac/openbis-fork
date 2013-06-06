/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.ipad.v2.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Abstract superclass for the handlers for CLIENT_PREFS request.
 * <p>
 * This request has a slightly different structure, since it does not return entities.
 * <p>
 * Subclasses should override the preferences_dict method to return the preferences dictionary. The superclass implements this method with the default
 * values for the standard keys.
 * 
 * @author cramakri
 */
public class ClientPreferencesRequestHandler implements IRequestHandler
{

    protected final Map<String, Object> parameters;

    protected final ISimpleTableModelBuilderAdaptor builder;

    protected final List<String> headers;

    /**
     * Constructor to initialize the state that is used in processing requests.
     * 
     * @param parameters The request parameters.
     * @param builder A table model builder.
     * @param searchService Ignored.
     */
    protected ClientPreferencesRequestHandler(Map<String, Object> parameters,
            ISimpleTableModelBuilderAdaptor builder, ISearchService searchService)
    {
        this.parameters = parameters;
        this.builder = builder;
        this.headers = Arrays.asList("KEY", "VALUE");
    }

    /**
     * The dictionary containing the value for the client preferences.
     * <p>
     * Subclasses may override if they want to change any of the values. The best way to override is to call default_preferences_dict then
     * modify/extend the resulting dictionary
     */
    protected Map<String, Object> getPreferencesDict()
    {
        return getDefaultPreferencesDict();
    }

    /**
     * The dictionary containing the standard keys and and default values for those keys.
     */
    protected final Map<String, Object> getDefaultPreferencesDict()
    {
        HashMap<String, Object> prefs = new HashMap<String, Object>();
        // The refresh interval is a value in seconds
        prefs.put("ROOT_SET_REFRESH_INTERVAL", 60 * 30);
        // The search domains are specified in the getSearchDomains method
        prefs.put("SEARCH_DOMAINS", IpadServiceUtilities.jsonEncodedValue(getSearchDomains()));

        return prefs;
    }

    /**
     * Return a list of search domains supported by this server. The default search domains are GLOBAL, which searches all metadata, and BARCODE,
     * which searches for barcodes.
     * <p>
     * Subclasses may override. The natural way to do this is to call super and extend the result, but there are use cases where it makes sense to
     * ignore the default implementation.
     */
    protected List<Map<String, String>> getSearchDomains()
    {
        List<Map<String, String>> searchDomains = new ArrayList<Map<String, String>>();
        // Global search domain
        searchDomains.add(getSearchDomain("GLOBAL", "Global"));

        // Barcode search domain
        if (areBarcodesSupported())
        {
            searchDomains.add(getSearchDomain("BARCODE", "Barcode"));
        }

        return searchDomains;
    }

    /**
     * Configuration method to specify if the barcode search domain should be turned on or not. Default is off.
     * <p>
     * Subclasses may override.
     */
    protected boolean areBarcodesSupported()
    {
        return false;
    }

    /**
     * Convenience method to create a search domain from a indentifiying key and label.
     */
    protected HashMap<String, String> getSearchDomain(String key, String label)
    {
        HashMap<String, String> globalSearchDomain = new HashMap<String, String>();
        globalSearchDomain.put("key", key);
        globalSearchDomain.put("label", label);
        return globalSearchDomain;
    }

    /**
     * Take the information from the preferences dict and put it into the table.
     */
    protected void addDataRows()
    {
        Map<String, Object> prefs = getPreferencesDict();
        for (String key : prefs.keySet())
        {
            IRowBuilderAdaptor row = builder.addRow();
            row.setCell("KEY", key);
            AbstractRequestHandler.setCell(row, "VALUE", prefs.get(key));
        }
    }

    /**
     * Configure the headers for this request.
     * <p>
     * For preference request, the headers are
     * <ul>
     * <li>KEY : The key of the preference.</li>
     * <li>VALUE : The value of the preference.</li>
     * </ul>
     */
    protected void addHeaders()
    {
        for (String header : headers)
        {
            builder.addHeader(header);
        }
    }

    /**
     * Execute the steps necessary to process the request.
     */
    public void processRequest()
    {
        addHeaders();
        addDataRows();
    }
}
