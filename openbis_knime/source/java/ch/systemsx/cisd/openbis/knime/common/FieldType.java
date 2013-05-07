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

package ch.systemsx.cisd.openbis.knime.common;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;

/**
 * Enumeration of field type factories.
 * 
 *
 * @author Franz-Josef Elmer
 */
public enum FieldType
{
    VARCHAR
    {
        @Override
        public IField create(String fieldParameters, IQueryApiFacade facade)
        {
            return new TextField();
        }
    },
    VOCABULARY
    {
        @Override
        public IField create(String fieldParameters, IQueryApiFacade facade)
        {
            return new VocabularyField(fieldParameters);
        }
    },
    EXPERIMENT
    {
        @Override
        public IField create(String fieldParameters, IQueryApiFacade facade)
        {
            return new OwnerField(DataSetOwnerType.EXPERIMENT, facade);
        }
    },
    SAMPLE
    {
        @Override
        public IField create(String fieldParameters, IQueryApiFacade facade)
        {
            return new OwnerField(DataSetOwnerType.SAMPLE, facade);
        }
    },
    DATA_SET
    {
        @Override
        public IField create(String fieldParameters, IQueryApiFacade facade)
        {
            return new OwnerField(DataSetOwnerType.DATA_SET, facade);
        }
    };
    
    public abstract IField create(String fieldParameters, IQueryApiFacade facade);
}