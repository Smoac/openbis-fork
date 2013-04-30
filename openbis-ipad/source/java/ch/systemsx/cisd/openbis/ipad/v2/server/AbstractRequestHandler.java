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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IRowBuilderAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Abstract superclass for the handlers for concrete requests like ROOT.
 * <p>
 * This superclass defines behavior common to all requests.
 * <p>
 * Subclasses need to implement the method optional_headers(), which returns a list of the optional
 * headers they fill out.
 * <p>
 * Subclasses should implement retrieve_data to get the data they provide.
 * <p>
 * Subclasses should implement add_data_rows. In this method, they should call add_row. The method
 * add_row takes a dictionary as an argument. The keys of the dictionary match the headers in the
 * result columns. The dictionary should include data for the required columns and optional ones
 * they fill.
 * 
 * @author cramakri
 */
public class AbstractRequestHandler implements IRequestHandler
{
    public static void setCell(IRowBuilderAdaptor row, String key, Object value)
    {
        if (value instanceof String)
        {
            row.setCell(key, (String) value);
        } else if (value instanceof Long)
        {
            row.setCell(key, (Long) value);
        } else if (value instanceof Double)
        {
            row.setCell(key, (Double) value);
        } else if (value instanceof Date)
        {
            row.setCell(key, (Date) value);
        } else
        {
            row.setCell(key, value.toString());
        }
    }

    private static List<String> responseHeaders(List<String> optionalHeaders)
    {
        List<String> requiredHeaders = Arrays.asList("PERM_ID", "REFCON");
        ArrayList<String> allHeaders = new ArrayList<String>(requiredHeaders);
        allHeaders.addAll(optionalHeaders);
        return allHeaders;
    }

    protected final Map<String, Object> parameters;

    protected final ISimpleTableModelBuilderAdaptor builder;

    protected final ISearchService searchService;

    protected final List<String> headers;

    /**
     * Constructor to initialize the state that is used in processing requests.
     * 
     * @param parameters The request parameters.
     * @param builder A table model builder.
     * @param searchService The service that supports searching for openBIS entities.
     * @param optionalHeaders Non-required headers that are returned by this request.
     */
    protected AbstractRequestHandler(Map<String, Object> parameters,
            ISimpleTableModelBuilderAdaptor builder, ISearchService searchService,
            List<String> optionalHeaders)
    {
        this.parameters = parameters;
        this.builder = builder;
        this.searchService = searchService;
        this.headers = responseHeaders(optionalHeaders);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public ISimpleTableModelBuilderAdaptor getBuilder()
    {
        return builder;
    }

    public List<String> getHeaders()
    {
        return headers;
    }

    public ISearchService getSearchService()
    {
        return searchService;
    }

    /**
     * A helper method to get the value of the entities parameter.
     * 
     * @return Return the entities parameter or an empty list if no entities were specified.
     */
    protected List<String> getEntitiesParameter()
    {
        @SuppressWarnings("unchecked")
        List<String> entities = (List<String>) parameters.get("entities");
        if (null == entities)
        {
            return Collections.emptyList();
        }
        return entities;
    }

    /**
     * Get the data for the request. Subclass responsibility.
     */
    protected void retrieveData()
    {

    }

    /**
     * Take the information from the data and put it into the table. Subclass responsibility.
     */
    protected void addDataRows()
    {

    }

    /**
     * Configure the headers for this request.
     * <p>
     * The possible headers come from the following list:
     * <ul>
     * <li>PERM_ID : A stable identifier for the object. (required)</li>
     * <li>REFCON : Data that is passed unchanged back to the server when a row is modified. This
     * can be used by the server to encode whatever it needs in order to modify the row. (required)</li>
     * <li>CATEGORY : A category identifier for grouping entities.</li>
     * <li>SUMMARY_HEADER : A short summary of the entity.</li>
     * <li>SUMMARY : A potentially longer summary of the entity.</li>
     * <li>CHILDREN : The permIds of the children of this entity. Transmitted as JSON.</li>
     * <li>IDENTIFIER : An identifier for the object.</li>
     * <li>IMAGES : A map with keys coming from the set 'MARQUEE', 'TILED'. The values are image
     * specs or lists of image specs. Image specs are maps with the keys: 'URL' (a URL for the
     * iamge) or 'DATA'. The data key contains a map that includes the image data and may include
     * some image metadata as well. This format has not yet been specified.</li>
     * <li>PROPERTIES : Properties (metadata) that should be displayed for this entity. Transmitted
     * as JSON.</li>
     * <li>ROOT_LEVEL : True if the entity should be shown on the root level.</li>
     * </ul>
     * <p>
     * The relevant headers are determined by the request.
     */
    protected void addHeaders()
    {
        for (String header : headers)
        {
            builder.addHeader(header);
        }
    }

    /**
     * Append a row of data to the table.
     */
    protected void addRow(Map<String, Object> entry)
    {
        IRowBuilderAdaptor row = builder.addRow();
        for (String header : headers)
        {
            Object value = entry.get(header);

            if (value != null)
            {
                setCell(row, header, value);
            } else
            {
                row.setCell(header, "");
            }
        }
    }

    /**
     * Take a collection of dictionaries and add a row for each one.
     */
    protected void addRows(List<Map<String, Object>> entries)
    {
        for (Map<String, Object> entry : entries)
        {
            addRow(entry);
        }
    }

    /**
     * Execute the steps necessary to process the request.
     */
    public void processRequest()
    {
        addHeaders();
        retrieveData();
        addDataRows();
    }
}
