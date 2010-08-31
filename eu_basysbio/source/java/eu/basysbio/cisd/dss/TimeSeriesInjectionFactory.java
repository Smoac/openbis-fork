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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.etlserver.utils.Column;

/**
 * @author Franz-Josef Elmer
 */
enum TimeSeriesInjectionFactory implements IInjectionFactory<TimeSeriesValue>
{
    IDENTIFIER(1, ".+")
    {
        @Override
        public IColumnInjection<TimeSeriesValue> create(final Column column)
        {
            return new AbstractColumnInjection<TimeSeriesValue>(column)
                {
                    @Override
                    void inject(TimeSeriesValue dataValue, String value)
                    {
                        dataValue.setIdentifier(value);
                    }
                };
        }
    },
    HUMAN_READABLE(2, "HumanReadable")
    {
        @Override
        public IColumnInjection<TimeSeriesValue> create(final Column column)
        {
            return new AbstractColumnInjection<TimeSeriesValue>(column)
                {
                    @Override
                    void inject(TimeSeriesValue dataValue, String value)
                    {
                        dataValue.setHumanReadable(value);
                    }
                };
        }
    },
    BSB_ID(2, "BSB_ID")
    {
        @Override
        public IColumnInjection<TimeSeriesValue> create(final Column column)
        {
            return new AbstractColumnInjection<TimeSeriesValue>(column)
                {
                    @Override
                    void inject(TimeSeriesValue dataValue, String value)
                    {
                        dataValue.setBsbId(value);
                    }
                };
        }
    },
    CONFIDENCE_LEVEL(3, "confidence level")
    {
        @Override
        public IColumnInjection<TimeSeriesValue> create(final Column column)
        {
            return new AbstractColumnInjection<TimeSeriesValue>(column)
                {
                    @Override
                    void inject(TimeSeriesValue dataValue, String value)
                    {
                        dataValue.setConfidenceLevel(value);
                    }
                };
        }
    },
    CONTROLLED_GENE(3, "ControlledGene")
    {
        @Override
        public IColumnInjection<TimeSeriesValue> create(final Column column)
        {
            return new AbstractColumnInjection<TimeSeriesValue>(column)
                {
                    @Override
                    void inject(TimeSeriesValue dataValue, String value)
                    {
                        dataValue.setControlledGene(value);
                    }
                };
        }
    },
    NUMBER_OF_REPLICATES(2, "no of replicates")
    {
        @Override
        public IColumnInjection<TimeSeriesValue> create(final Column column)
        {
            return new AbstractColumnInjection<TimeSeriesValue>(column)
                {
                    @Override
                    void inject(TimeSeriesValue dataValue, String value)
                    {
                        Integer number = null;
                        if (StringUtils.isNotBlank(value))
                        {
                            number = Integer.parseInt(value);
                        }
                        dataValue.setNumberOfReplicates(number);
                    }
                };
        }
    };
    
    private final int columnNumber;
    private final Pattern pattern;

    private TimeSeriesInjectionFactory(int columnNumber, String columnHeaderPattern)
    {
        this.columnNumber = columnNumber;
        pattern = Pattern.compile(columnHeaderPattern);
    }

    public IColumnInjection<TimeSeriesValue> tryToCreate(List<Column> columns)
    {
        int colIndex = columnNumber - 1;
        if (colIndex >= columns.size())
        {
            return null;
        }
        Column column = columns.get(colIndex);
        return pattern.matcher(column.getHeader()).matches() ? create(column) : null;
    }
    
    public abstract IColumnInjection<TimeSeriesValue> create(final Column column);
    
}
