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

package eu.basysbio.cisd.dss;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.etlserver.utils.Column;

/**
 * @author Franz-Josef Elmer
 */
enum ChipChipInjectionFactory implements IInjectionFactory<ChipChipData>
{
    BSU_IDENTIFIER(1)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
                {
                    @Override
                    void inject(ChipChipData dataValue, String value)
                    {
                        dataValue.setBsuIdentifier(value);
                    }
                };
        }
    },
    GENE_NAME(2)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
                {
                    @Override
                    void inject(ChipChipData dataValue, String value)
                    {
                        dataValue.setGeneName(value);
                    }
                };
        }
    },
    GENE_FUNCTION(3)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
                {
                    @Override
                    void inject(ChipChipData dataValue, String value)
                    {
                        dataValue.setGeneFunction(value);
                    }
                };
        }
    },
    ARRAY_DESIGN(4)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
                {
                    @Override
                    void inject(ChipChipData dataValue, String value)
                    {
                        dataValue.setArrayDesign(value);
                    }
                };
        }
    },
    MICRO_ARRAY_ID(5)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
                {
                    @Override
                    void inject(ChipChipData dataValue, String value)
                    {
                        Integer number = null;
                        if (StringUtils.isNotBlank(value))
                        {
                            number = Integer.parseInt(value);
                        }
                        dataValue.setMicroArrayID(number);
                    }
                };
        }
    },
    INTERGENIC(9)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
                {
                    @Override
                    void inject(ChipChipData dataValue, String value)
                    {
                        dataValue.setIntergenic("true".equalsIgnoreCase(value));
                    }
                };
        }
    },
    NEARBY_GENE_NAMES(10)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
            {
                @Override
                void inject(ChipChipData dataValue, String value)
                {
                    dataValue.setNearbyGeneNames(value);
                }
            };
        }
    },
    NEARBY_GENE_IDS(11)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
            {
                @Override
                void inject(ChipChipData dataValue, String value)
                {
                    dataValue.setNearbyGeneIDs(value);
                }
            };
        }
    },
    DISTANCE_FROM_START(12)
    {
        @Override
        public IColumnInjection<ChipChipData> create(final Column column)
        {
            return new AbstractColumnInjection<ChipChipData>(column)
            {
                @Override
                void inject(ChipChipData dataValue, String value)
                {
                    dataValue.setDistancesFromStart(value);
                }
            };
        }
    };
    
    private final int columnNumber;

    private ChipChipInjectionFactory(int columnNumber)
    {
        this.columnNumber = columnNumber;
    }

    public IColumnInjection<ChipChipData> tryToCreate(List<Column> columns)
    {
        int colIndex = columnNumber - 1;
        if (colIndex >= columns.size())
        {
            return null;
        }
        Column column = columns.get(colIndex);
        return create(column);
    }
    
    public abstract IColumnInjection<ChipChipData> create(final Column column);
    
}
