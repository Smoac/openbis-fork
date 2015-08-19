/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions;

public enum FetchOptionsMatchType
{

    ALL_PARTS_AND_ALL_PAGING_AND_SORTING
    {
        @Override
        public boolean isBetterThan(FetchOptionsMatchType matchType)
        {
            return matchType == ALL_PARTS_AND_SUB_PAGING_AND_SORTING;
        }

    },
    ALL_PARTS_AND_SUB_PAGING_AND_SORTING
    {
        @Override
        public boolean isBetterThan(FetchOptionsMatchType matchType)
        {
            return false;
        }
    };

    public abstract boolean isBetterThan(FetchOptionsMatchType matchType);

}