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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Defines a set of datasets by either enumerating their codes or providing the grid configuration
 * for it (together with the cache identifier).
 * 
 * @author Tomasz Pylak
 */
public final class DisplayedOrSelectedDatasetCriteria implements IsSerializable
{

    private IResultSetConfig<String, ExternalData> displayedItemsOrNull;

    private List<String> selectedDatasetCodesOrNull;

    public static DisplayedOrSelectedDatasetCriteria createDisplayedItems(
            IResultSetConfig<String, ExternalData> displayedItems)
    {
        return new DisplayedOrSelectedDatasetCriteria(displayedItems, null);
    }

    public static DisplayedOrSelectedDatasetCriteria createSelectedItems(
            List<String> selectedDatasetCodes)
    {
        return new DisplayedOrSelectedDatasetCriteria(null, selectedDatasetCodes);
    }

    private DisplayedOrSelectedDatasetCriteria(
            IResultSetConfig<String, ExternalData> displayedItemsOrNull,
            List<String> selectedDatasetCodesOrNull)
    {
        assert (displayedItemsOrNull == null) != (selectedDatasetCodesOrNull == null) : "Exactly one arg must be null and one non-null";
        this.displayedItemsOrNull = displayedItemsOrNull;
        this.selectedDatasetCodesOrNull = selectedDatasetCodesOrNull;
    }

    public IResultSetConfig<String, ExternalData> tryGetDisplayedItems()
    {
        return displayedItemsOrNull;
    }

    public List<String> tryGetSelectedItems()
    {
        return selectedDatasetCodesOrNull;
    }

    // GWT only
    private DisplayedOrSelectedDatasetCriteria()
    {
    }

    @SuppressWarnings("unused")
    // GWT only
    private IResultSetConfig<String, ExternalData> getDisplayedItemsOrNull()
    {
        return displayedItemsOrNull;
    }

    @SuppressWarnings("unused")
    // GWT only
    private void setDisplayedItemsOrNull(IResultSetConfig<String, ExternalData> displayedItemsOrNull)
    {
        this.displayedItemsOrNull = displayedItemsOrNull;
    }

    @SuppressWarnings("unused")
    // GWT only
    private List<String> getSelectedDatasetCodesOrNull()
    {
        return selectedDatasetCodesOrNull;
    }

    @SuppressWarnings("unused")
    // GWT only
    private void setSelectedDatasetCodesOrNull(List<String> selectedDatasetCodesOrNull)
    {
        this.selectedDatasetCodesOrNull = selectedDatasetCodesOrNull;
    }

}
