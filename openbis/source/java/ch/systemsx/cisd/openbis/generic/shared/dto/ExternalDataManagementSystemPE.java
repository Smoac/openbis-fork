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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * @author Pawel Glyzewski
 */
@Entity
@Table(name = TableNames.EXTERNAL_DATA_MANAGEMENT_SYSTEMS_TABLE)
public class ExternalDataManagementSystemPE extends
        AbstractIdAndCodeHolder<ExternalDataManagementSystemPE>
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private String code;

    private String label;

    private String urlTemplate;

    private boolean openBIS;

    @Override
    @Id
    @SequenceGenerator(name = SequenceNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_SEQUENCE, sequenceName = SequenceNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.EXTERNAL_DATA_MANAGEMENT_SYSTEM_SEQUENCE)
    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    @Field(index = Index.YES, store = Store.YES, name = SearchFieldConstants.CODE)
    public String getCode()
    {
        return code;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @Column(name = ColumnNames.LABEL_COLUMN)
    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    @Column(name = ColumnNames.URL_TEMPLATE_COLUMN)
    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public void setUrlTemplate(final String urlTemplate)
    {
        this.urlTemplate = urlTemplate;
    }

    @Column(name = ColumnNames.IS_OPENBIS_COLUMN)
    @NotNull
    public boolean isOpenBIS()
    {
        return openBIS;
    }

    public void setOpenBIS(final boolean openBIS)
    {
        this.openBIS = openBIS;
    }
}
