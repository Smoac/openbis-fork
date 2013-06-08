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

import java.util.List;

/**
 * Writable dataset of cell-level image segmentation results.
 * 
 * @author Bernd Rinn
 */
public interface ICellLevelSegmentationWritableDataset extends ICellLevelWritableDataset,
        ICellLevelSegmentationDataset
{
    /**
     * Writes the image segmentation for the given <var>id</var> and <var>objectType</var>.
     * 
     * @throws WrongObjectTypeException If <var>objectType</var> is not for this dataset.
     */
    public void writeImageSegmentation(ImageId id, ObjectType objectType,
            List<SegmentedObject> objects) throws WrongObjectTypeException;
}
