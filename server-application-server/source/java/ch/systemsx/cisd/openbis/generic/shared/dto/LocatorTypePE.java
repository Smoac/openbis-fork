/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import javax.persistence.*;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import org.hibernate.validator.constraints.Length;

/**
 * Persistence Entity representing type of locator.
 * 
 * @author Christian Ribeaud
 * @author Izabela Adamczyk
 */
@Entity
@Table(name = TableNames.LOCATOR_TYPES_TABLE, uniqueConstraints =
{ @UniqueConstraint(columnNames =
{ ColumnNames.CODE_COLUMN }) })
public final class LocatorTypePE extends AbstractTypePE
{
    private static final long serialVersionUID = IServer.VERSION;

    @Override
    @SequenceGenerator(name = SequenceNames.LOCATOR_TYPE_SEQUENCE, sequenceName = SequenceNames.LOCATOR_TYPE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.LOCATOR_TYPE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public void setCode(final String code)
    {
        this.code = code;
    }

    @Override
    @Column(name = ColumnNames.CODE_COLUMN)
    @Length(min = 1, max = Code.CODE_LENGTH_MAX, message = ValidationMessages.CODE_LENGTH_MESSAGE)
    @NotNull(message = ValidationMessages.CODE_NOT_NULL_MESSAGE)
    @Pattern(regexp = AbstractIdAndCodeHolder.CODE_PATTERN, flags = Pattern.Flag.CASE_INSENSITIVE, message = ValidationMessages.CODE_PATTERN_MESSAGE)
    public  String getCode()
    {
        return code;
    }

}
