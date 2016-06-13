/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * @author Pawel Glyzewski
 */
@Entity
@Table(name = TableNames.LINK_DATA_TABLE)
@PrimaryKeyJoinColumn(name = ColumnNames.DATA_ID_COLUMN)
@Indexed(index = "DataPE")
@ClassBridge(impl = LinkDataGlobalSearchBridge.class)
public class LinkDataPE extends DataPE
{
    private static final long serialVersionUID = IServer.VERSION;

    private ExternalDataManagementSystemPE externalDataManagementSystem;

    private String externalCode;

    /** Returns <code>externalDataManagementSystem</code>. */
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull(message = ValidationMessages.EXTERNAL_DATA_MANAGEMENT_SYSTEM_NOT_NULL_MESSAGE)
    @JoinColumn(name = ColumnNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_ID_COLUMN, updatable = true)
    @IndexedEmbedded(prefix = SearchFieldConstants.PREFIX_EXTERNAL_DMS)
    public ExternalDataManagementSystemPE getExternalDataManagementSystem()
    {
        return externalDataManagementSystem;
    }

    /** Sets <code>externalDataManagementSystem</code>. */
    public void setExternalDataManagementSystem(
            final ExternalDataManagementSystemPE externalDataManagementSystem)
    {
        this.externalDataManagementSystem = externalDataManagementSystem;
    }

    @NotNull(message = ValidationMessages.EXTERNAL_CODE_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.EXTERNAL_CODE_COLUMN)
    @Field(name = SearchFieldConstants.EXTERNAL_CODE, index = Index.YES, store = Store.YES)
    public String getExternalCode()
    {
        return externalCode;
    }

    public void setExternalCode(String externalCode)
    {
        this.externalCode = externalCode;
    }

    @Override
    @Transient
    public LinkDataPE tryAsLinkData()
    {
        return this;
    }

    @Override
    @Transient
    public boolean isLinkData()
    {
        return true;
    }
}
