/*
 * Copyright 2012 ETH Zuerich, CISD
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
 * A role for checking that a dataset is in a consistent state.
 *
 * @author Bernd Rinn
 */
interface IDatasetVerifyer
{
    /**
     * Verifies that the dataset is in a consistent state.
     * 
     * @return <code>null</code> if the dataset is consistent, an error message otherwise.
     */
    public String verify() throws IllegalStateException;
    
    /**
     * The code of the dataset of this verifyer.
     */
    public String getDatasetCode();
}
