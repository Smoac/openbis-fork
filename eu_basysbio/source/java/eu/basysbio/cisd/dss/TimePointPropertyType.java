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

package eu.basysbio.cisd.dss;

/**
 * Property type specification of a time point data set type.
 * 
 * @author Franz-Josef Elmer
 */
enum TimePointPropertyType
{
    BI_ID
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getBiID();
        }
    },
    CEL_LOC(true)
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getCelLoc();
        }
    },
    CG
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getControlledGene();
        }
    },
    SCALE(true)
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getScale();
        }
    },
    TECHNICAL_REPLICATE_CODE
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getTechnicalReplicateCode();
        }
    },
    TIME_SERIES_DATA_SET_TYPE
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getTimeSeriesDataSetType();
        }
    },
    UPLOADER_EMAIL
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return null;
        }
    },
    VALUE_TYPE
    {
        @Override
        public String getElementPlain(DataColumnHeader header)
        {
            return header.getValueType();
        }
    };

    private final boolean vocabulary;

    private TimePointPropertyType()
    {
        this(false);
    }

    private TimePointPropertyType(boolean vocabulary)
    {
        this.vocabulary = vocabulary;
    }

    boolean isVocabulary()
    {
        return vocabulary;
    }
    
    String getElement(DataColumnHeader header)
    {
        String element = getElementPlain(header);
        return isVocabulary() ? element.toUpperCase() : element;
    }

    protected abstract String getElementPlain(DataColumnHeader header);

}
