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

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;

/**
 * Criteria for listing <i>data sets</i> related to {@link MatchingEntity}s like samples and
 * experiments.
 * 
 * @author Piotr Buczek
 */
public class RelatedDataSetCriteria implements IsSerializable
{

    private TableExportCriteria<? extends IEntityInformationHolder> displayedEntitiesOrNull;

    private List<? extends IEntityInformationHolder> selectedEntitiesOrNull;

    public static RelatedDataSetCriteria createDisplayedEntities(
            TableExportCriteria<? extends IEntityInformationHolder> displayedEntities)
    {
        return new RelatedDataSetCriteria(displayedEntities, null);
    }

    public static RelatedDataSetCriteria createSelectedEntities(
            List<? extends IEntityInformationHolder> selectedEntities)
    {
        return new RelatedDataSetCriteria(null, selectedEntities);
    }

    private RelatedDataSetCriteria(
            TableExportCriteria<? extends IEntityInformationHolder> displayedEntitiesOrNull,
            List<? extends IEntityInformationHolder> selectedEntitiesOrNull)
    {
        assert (displayedEntitiesOrNull == null) != (selectedEntitiesOrNull == null) : "Exactly one arg must be null and one non-null";
        this.displayedEntitiesOrNull = displayedEntitiesOrNull;
        this.selectedEntitiesOrNull = selectedEntitiesOrNull;
    }

    public TableExportCriteria<? extends IEntityInformationHolder> tryGetDisplayedEntities()
    {
        return displayedEntitiesOrNull;
    }

    public List<? extends IEntityInformationHolder> tryGetSelectedEntities()
    {
        return selectedEntitiesOrNull;
    }

    // GWT only
    private RelatedDataSetCriteria()
    {
    }

    @SuppressWarnings("unused")
    private TableExportCriteria<? extends IEntityInformationHolder> getDisplayedEntitiesOrNull()
    {
        return displayedEntitiesOrNull;
    }

    @SuppressWarnings("unused")
    private void setDisplayedEntitiesOrNull(
            TableExportCriteria<? extends IEntityInformationHolder> displayedEntitiesOrNull)
    {
        this.displayedEntitiesOrNull = displayedEntitiesOrNull;
    }

    @SuppressWarnings("unused")
    private List<? extends IEntityInformationHolder> getSelectedEntitiesOrNull()
    {
        return selectedEntitiesOrNull;
    }

    @SuppressWarnings("unused")
    private void setSelectedEntitiesOrNull(
            List<? extends IEntityInformationHolder> selectedEntitiesOrNull)
    {
        this.selectedEntitiesOrNull = selectedEntitiesOrNull;
    }

}
