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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed;

import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.CODE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.ID;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PERM_ID;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * Helper for getting index field names of attributes and properties for detailed entity search.<br>
 * 
 * @author Piotr Buczek
 */
class IndexFieldNameHelper
{
    // associations

    static String getAssociationIndexField(EntityKind entityKind,
            AssociatedEntityKind associationKind)
    {
        switch (associationKind)
        {
            case EXPERIMENT:
                if (entityKind == EntityKind.SAMPLE || entityKind == EntityKind.DATA_SET)
                {
                    return SearchFieldConstants.EXPERIMENT_ID;
                }
                throw createAssociationNotHandledException(entityKind, associationKind);

            case SAMPLE:
                if (entityKind == EntityKind.DATA_SET)
                {
                    return SearchFieldConstants.SAMPLE_ID;
                }
                throw createAssociationNotHandledException(entityKind, associationKind);

            case DATA_SET:
                throw createAssociationNotHandledException(entityKind, associationKind);

            case SAMPLE_CONTAINER:
                if (entityKind == EntityKind.SAMPLE)
                {
                    return SearchFieldConstants.CONTAINER_ID;
                }
                throw createAssociationNotHandledException(entityKind, associationKind);

            case DATA_SET_CONTAINER:
            case DATA_SET_PARENT:
            case DATA_SET_CHILD:
            case SAMPLE_PARENT:
            case SAMPLE_CHILD:
            case MATERIAL:
                // parent-child is a many-to-many connection - it is not handled by lucene index
                throw createAssociationNotHandledException(entityKind, associationKind);
        }
        return null; // shouldn't happen
    }

    private static RuntimeException createAssociationNotHandledException(EntityKind entityKind,
            AssociatedEntityKind associationKind)
    {
        return InternalErr.error("Associations between " + entityKind + " and " + associationKind
                + " are not supported");
    }

    // properties

    static String getPropertyIndexField(String propertyCode)
    {
        assert propertyCode != null : "property code is null";
        return SearchFieldConstants.PREFIX_PROPERTIES + propertyCode;
    }

    // attributes

    static String getAttributeIndexField(EntityKind entityKind, String attributeCode)
    {
        switch (entityKind)
        {
            case DATA_SET:
                return getDataSetAttributeIndexField(DataSetAttributeSearchFieldKind
                        .valueOf(attributeCode));
            case EXPERIMENT:
                return getExperimentAttributeIndexField(ExperimentAttributeSearchFieldKind
                        .valueOf(attributeCode));
            case MATERIAL:
                return getMaterialAttributeIndexField(MaterialAttributeSearchFieldKind
                        .valueOf(attributeCode));
            case SAMPLE:
                for (SampleAttributeSearchFieldKind searchFieldKind : SampleAttributeSearchFieldKind.values())
                {
                    if (searchFieldKind.name().equals(attributeCode))
                    {
                        return getSampleAttributeIndexField(searchFieldKind);
                    }
                }
                return attributeCode;
        }
        return null; // cannot happen
    }

    private static String getDataSetAttributeIndexField(
            DataSetAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            // common fields

            case CODE:
                return CODE;
            case DATA_SET_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case METAPROJECT:
                return SearchFieldConstants.PREFIX_METAPROJECT + SearchFieldConstants.IDENTIFIER;
            case REGISTRATION_DATE:
            case REGISTRATION_DATE_FROM:
            case REGISTRATION_DATE_UNTIL:
                return SearchFieldConstants.REGISTRATION_DATE;
            case MODIFICATION_DATE:
            case MODIFICATION_DATE_FROM:
            case MODIFICATION_DATE_UNTIL:
                return SearchFieldConstants.MODIFICATION_DATE;

            // physical data set fields

            case LOCATOR_TYPE:
                return SearchFieldConstants.PREFIX_LOCATOR_TYPE + CODE;
            case LOCATION:
                return SearchFieldConstants.LOCATION;
            case SHARE_ID:
                return SearchFieldConstants.SHARE_ID;
            case SIZE:
                return SearchFieldConstants.SIZE;
            case STORAGE_FORMAT:
                return SearchFieldConstants.PREFIX_STORAGE_FORMAT + CODE;
            case FILE_TYPE:
                return SearchFieldConstants.PREFIX_FILE_FORMAT_TYPE + CODE;
            case COMPLETE:
                return SearchFieldConstants.COMPLETE;
            case STATUS:
                return SearchFieldConstants.STATUS;
            case ARCHIVING_REQUESTED:
                return SearchFieldConstants.ARCHIVING_REQUESTED;
            case PRESENT_IN_ARCHIVE:
                return SearchFieldConstants.PRESENT_IN_ARCHIVE;
            case STORAGE_CONFIRMATION:
                return SearchFieldConstants.STORAGE_CONFIRMATION;
            case SPEED_HINT:
                return SearchFieldConstants.SPEED_HINT;
            case EXTERNAL_DMS_CODE:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.PREFIX_EXTERNAL_DMS + CODE;
            case EXTERNAL_DMS_LABEL:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.PREFIX_EXTERNAL_DMS + SearchFieldConstants.LABEL;
            case EXTERNAL_DMS_ADDRESS:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.PREFIX_EXTERNAL_DMS + SearchFieldConstants.ADDRESS;
            case EXTERNAL_DMS_TYPE:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.PREFIX_EXTERNAL_DMS + SearchFieldConstants.ADDRESS_TYPE;
            case EXTERNAL_CODE:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.EXTERNAL_CODE;
            case PATH:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.PATH;
            case COMMIT_HASH:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.GIT_COMMIT_HASH;
            case COMMIT_REPOSITORY_ID:
                return SearchFieldConstants.PREFIX_CONTENT_COPY + SearchFieldConstants.GIT_REPOSITORY_ID;
            case REGISTRATOR_USER_ID:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_USER_ID;
            case REGISTRATOR_FIRST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_FIRST_NAME;
            case REGISTRATOR_LAST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_LAST_NAME;
            case REGISTRATOR_EMAIL:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_EMAIL;
            case MODIFIER_USER_ID:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_USER_ID;
            case MODIFIER_FIRST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_FIRST_NAME;
            case MODIFIER_LAST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_LAST_NAME;
            case MODIFIER_EMAIL:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_EMAIL;
        }
        throw new IllegalArgumentException(attributeKind.toString());
    }

    private static String getExperimentAttributeIndexField(
            ExperimentAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case CODE:
                return CODE;
            case EXPERIMENT_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case PERM_ID:
                return SearchFieldConstants.PERM_ID;
            case IDENTIFIER:
                return SearchFieldConstants.IDENTIFIER;
            case PROJECT:
                return SearchFieldConstants.PREFIX_PROJECT + CODE;
            case PROJECT_PERM_ID:
                return SearchFieldConstants.PREFIX_PROJECT + PERM_ID;
            case PROJECT_SPACE:
                return SearchFieldConstants.PREFIX_PROJECT + SearchFieldConstants.PREFIX_SPACE
                        + CODE;
            case METAPROJECT:
                return SearchFieldConstants.PREFIX_METAPROJECT + SearchFieldConstants.IDENTIFIER;
            case REGISTRATION_DATE:
            case REGISTRATION_DATE_FROM:
            case REGISTRATION_DATE_UNTIL:
                return SearchFieldConstants.REGISTRATION_DATE;
            case MODIFICATION_DATE:
            case MODIFICATION_DATE_FROM:
            case MODIFICATION_DATE_UNTIL:
                return SearchFieldConstants.MODIFICATION_DATE;
            case REGISTRATOR_USER_ID:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_USER_ID;
            case REGISTRATOR_FIRST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_FIRST_NAME;
            case REGISTRATOR_LAST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_LAST_NAME;
            case REGISTRATOR_EMAIL:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_EMAIL;
            case MODIFIER_USER_ID:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_USER_ID;
            case MODIFIER_FIRST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_FIRST_NAME;
            case MODIFIER_LAST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_LAST_NAME;
            case MODIFIER_EMAIL:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_EMAIL;
        }
        return null; // cannot happen
    }

    private static String getMaterialAttributeIndexField(
            MaterialAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case ID:
                return ID;
            case PERM_ID:
                return PERM_ID;
            case CODE:
                return CODE;
            case MATERIAL_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case METAPROJECT:
                return SearchFieldConstants.PREFIX_METAPROJECT + SearchFieldConstants.IDENTIFIER;
            case REGISTRATION_DATE:
            case REGISTRATION_DATE_FROM:
            case REGISTRATION_DATE_UNTIL:
                return SearchFieldConstants.REGISTRATION_DATE;
            case MODIFICATION_DATE:
            case MODIFICATION_DATE_FROM:
            case MODIFICATION_DATE_UNTIL:
                return SearchFieldConstants.MODIFICATION_DATE;
            case REGISTRATOR_USER_ID:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_USER_ID;
            case REGISTRATOR_FIRST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_FIRST_NAME;
            case REGISTRATOR_LAST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_LAST_NAME;
            case REGISTRATOR_EMAIL:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_EMAIL;
            case MODIFIER_USER_ID:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_USER_ID;
            case MODIFIER_FIRST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_FIRST_NAME;
            case MODIFIER_LAST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_LAST_NAME;
            case MODIFIER_EMAIL:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_EMAIL;
        }
        return null; // cannot happen
    }

    private static String getSampleAttributeIndexField(SampleAttributeSearchFieldKind attributeKind)
    {
        switch (attributeKind)
        {
            case CODE:
                return CODE;
            case SAMPLE_TYPE:
                return SearchFieldConstants.PREFIX_ENTITY_TYPE + CODE;
            case PERM_ID:
                return SearchFieldConstants.PERM_ID;
            case IDENTIFIER:
                return SearchFieldConstants.IDENTIFIER;
            case SPACE:
                return SearchFieldConstants.PREFIX_SPACE + CODE;
            case PROJECT:
                return SearchFieldConstants.PREFIX_PROJECT + CODE;
            case PROJECT_PERM_ID:
                return SearchFieldConstants.PREFIX_PROJECT + PERM_ID;
            case PROJECT_SPACE:
                return SearchFieldConstants.PREFIX_PROJECT + SearchFieldConstants.PREFIX_SPACE + CODE;
            case METAPROJECT:
                return SearchFieldConstants.PREFIX_METAPROJECT + SearchFieldConstants.IDENTIFIER;
            case REGISTRATION_DATE:
            case REGISTRATION_DATE_FROM:
            case REGISTRATION_DATE_UNTIL:
                return SearchFieldConstants.REGISTRATION_DATE;
            case MODIFICATION_DATE:
            case MODIFICATION_DATE_FROM:
            case MODIFICATION_DATE_UNTIL:
                return SearchFieldConstants.MODIFICATION_DATE;
            case REGISTRATOR_USER_ID:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_USER_ID;
            case REGISTRATOR_FIRST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_FIRST_NAME;
            case REGISTRATOR_LAST_NAME:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_LAST_NAME;
            case REGISTRATOR_EMAIL:
                return SearchFieldConstants.PREFIX_REGISTRATOR + SearchFieldConstants.PERSON_EMAIL;
            case MODIFIER_USER_ID:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_USER_ID;
            case MODIFIER_FIRST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_FIRST_NAME;
            case MODIFIER_LAST_NAME:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_LAST_NAME;
            case MODIFIER_EMAIL:
                return SearchFieldConstants.PREFIX_MODIFIER + SearchFieldConstants.PERSON_EMAIL;
        }
        throw new IllegalArgumentException(attributeKind.toString());
    }
}
