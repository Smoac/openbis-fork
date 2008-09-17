/*
 * Copyright 2007 ETH Zuerich, CISD
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
 * Getters and Setters of all DTOs which are Entity Types (e.g. Material Type, Experiment Type).
 * 
 * @author Franz-Josef Elmer
 */
public interface IEntityType
{

    /** Returns the code of this type. */
    public String getCode();

    /** Sets the code of this type. */
    public void setCode(final String code);

    /** Returns a description of this type. */
    public String getDescription();

    /** Sets the description of this type. */
    public void setDescription(final String description);

}
