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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Some constants used in {@link ModelData} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class DataModelPropertyNames
{

    private DataModelPropertyNames()
    {
        // Can not be instantiated.
    }

    public static final String CODE = "code";

    public static final String DESCRIPTION = "description";

    public static final String EMAIL = "email";

    public static final String EXPERIMENT = "experiment";

    public static final String FIRST_NAME = "firstName";

    public static final String GROUP = "group";

    public static final String INSTANCE = "instance";

    public static final String IS_GROUP_SAMPLE = "isGroupSample";

    public static final String IS_INSTANCE_SAMPLE_COLUMN = "isShared";

    public static final String IS_INVALID = "isInvalid";

    public static final String LAST_NAME = "lastName";

    public static final String LEADER = "leader";

    public static final String OBJECT = "object";

    public static final String PERSON = "person";

    public static final String REGISTRATION_DATE = "registrationDate";

    public static final String REGISTRATOR = "registrator";

    public static final String ROLE = "role";

    public static final String ROLES = "roles";

    public static final String SAMPLE_IDENTIFIER = "sampleIdentifier";

    public static final String SAMPLE_TYPE = "sampleType";

    public static final String USER_ID = "userId";

}
