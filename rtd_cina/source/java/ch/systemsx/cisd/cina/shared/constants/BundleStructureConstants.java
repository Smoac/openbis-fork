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
 * Constants that describe the structure fo the CINA bundle files.
 * <p>
 * Root.bundle/
 * <ul>
 * <li>BUNDLE_METADATA_FILE_NAME</li>
 * <li>METADATA_FOLDER_NAME
 * <ul>
 * <li>Collection Folder
 * <ul>
 * <li>COLLECTION_METADATA_FILE_NAME</li>
 * <li>ANNOTATED_IMAGES_FOLDER_NAME
 * <ul>
 * <li>IMAGE_METADATA_FILE_NAME</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>RAW_IMAGES_FOLDER_NAME
 * <ul>
 * <li>Collection Folder</li>
 * </ul>
 * </ul>
 * A note for understanding the constants: Collections were once called replicas, and the old name
 * has not yet been removed everywere.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class BundleStructureConstants
{
    public static final String COLLECTIONS_FOLDER_NAME = "Collections";

    public static final String METADATA_FOLDER_NAME = "Annotations";
    
    public static final String RAW_IMAGES_FOLDER_NAME = "RawData";

    public static final String ANNOTATED_IMAGES_FOLDER_NAME = "Representations";

    public static final String BUNDLE_METADATA_FOLDER_NAME = "Annotations";

    public static final String BUNDLE_METADATA_FILE_NAME = "CollectionMetadata.xml";
    
    public static final String OLD_BUNDLE_METADATA_FILE_NAME = "BundleMetadata.xml";
    
    public final static String IMAGE_METADATA_FILE_NAME = "metadata.xml";

    public static final String GRID_PREP_SAMPLE_CODE_KEY = "database id";

    public static final String COLLECTION_METADATA_FILE_NAME = "CollectionMetadata.xml";

    public static final String COLLECTION_SAMPLE_CODE_KEY = "database id";

    public static final String COLLECTION_SAMPLE_DESCRIPTION_KEY = "description";

    public static final String COLLECTION_SAMPLE_CREATOR_NAME = "author";

    /**
     * No reason to instantiate this class.
     */
    private BundleStructureConstants()
    {

    }
}
