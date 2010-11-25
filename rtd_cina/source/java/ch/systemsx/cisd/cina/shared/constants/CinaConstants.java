/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cina.shared.constants;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaConstants
{
    public static final String BUNDLE_DATA_SET_TYPE_CODE = "BUNDLE";

    public static final String RAW_IMAGES_DATA_SET_TYPE_CODE = "RAW_IMAGES";

    public static final String METADATA_DATA_SET_TYPE_CODE = "METADATA";

    public static final String IMAGE_DATA_SET_TYPE_CODE = "IMAGE";

    public static final String GRID_PREP_SAMPLE_TYPE_CODE = "GRID_PREP";

    public final static String REPLICA_SAMPLE_TYPE_CODE = "GRID_REPLICA";

    public static final String DIMENSION_PREFIX = "Dimension";

    public static final String SIZE_PREFIX = "Size";

    public static final String DESCRIPTION_PROPERTY_CODE = "DESCRIPTION";

    public static final String CREATOR_EMAIL_PROPERTY_CODE = "CREATOR_EMAIL";

    // No need to instantiate this class
    private CinaConstants()
    {

    }
}
