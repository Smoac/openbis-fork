/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

/**
 * The definition of a property assigned to entity type.
 * 
 * @author Jakub Straszewski
 */
public interface IPropertyAssignmentImmutable
{
    /**
     * Return the code of the assigned entity type.
     */
    String getEntityTypeCode();

    /**
     * Return the label of the assigned property type
     */
    String getPropertyTypeLabel();

    /**
     * Return the description of the assigned property type
     */
    String getPropertyTypeDescription();

    /**
     * Return the code of the assigned property type.
     */
    String getPropertyTypeCode();

    /**
     * Return <code>true</code> if the property is mandatory for the assigned entity type.
     */
    boolean isMandatory();

    /**
     * Return the name of the form section.
     */
    String getSection();

    /**
     * Return the position at which the property will be rendered when editing/registering objects of the specified entity type.
     */
    Long getPositionInForms();

    /**
     * Return the name of the script in case of a dynamic or managed property
     */
    String getScriptName();

    /**
     * Return <code>true</code> if the property is dynamic
     */
    boolean isDynamic();

    /**
     * Return <code>true</code> if the property is managed
     */
    boolean isManaged();

    /**
     * Return <code>true</code> if the property is shown in the edit views
     */
    boolean shownInEditViews();
}
