/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl;

import ch.systemsx.cisd.openbis.uitest.type.Sample;

/**
 * @author anttil
 */
public enum IdentifiedBy
{
    SPACE_AND_CODE
    {
        @Override
        public String format(Sample sample)
        {
            return "/" + sample.getSpace().getCode() + "/" + sample.getCode();
        }
    },
    CODE
    {
        @Override
        public String format(Sample sample)
        {
            return sample.getCode();
        }
    };

    public abstract String format(Sample sample);
}
