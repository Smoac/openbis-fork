/*
 * Copyright 2008 ETH Zuerich, CISD
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

/**
 * Type of hierarchy for {@link SamplePE}.
 * 
 * @author Christian Ribeaud
 */
// TODO 2008-12-12, Tomasz Pylak: Move to different package. This object allows BO and DAO layers to
// communicate, there is no need to put it in the client-visible DTO package.
public enum HierarchyType
{
    CHILD("generatedFrom"), CONTAINED("container");

    private final String parentFieldName;

    private HierarchyType(final String parentFieldName)
    {
        this.parentFieldName = parentFieldName;
    }

    /**
     * Returns the parent field name (for <i>Hibernate</i> searching).
     */
    public final String getParentFieldName()
    {
        return parentFieldName;
    }

}