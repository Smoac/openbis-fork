/*
 * Copyright 2011-2013 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.hcscld;

/**
 * The enumeration of types of cell-level datasets.  
 *
 * @author Bernd Rinn
 */
public enum CellLevelDatasetType
{
    /**
     * Image segmentation results.
     */
    SEGMENTATION,
    
    /**
     * Object tracking lists.
     */
    TRACKING,
    
    /**
     * Cell-level feature vectors. 
     */
    FEATURES, 
    
    /**
     * Classification result on the cell-level.
     */
    CLASSIFICATION;
}
