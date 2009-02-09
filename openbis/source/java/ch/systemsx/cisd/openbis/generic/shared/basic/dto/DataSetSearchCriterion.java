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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Izabela Adamczyk
 */
public class DataSetSearchCriterion implements IsSerializable
{
    private DataSetSearchField field;

    private String value;

    public static class DataSetSearchField implements IsSerializable
    {
        private DataSetSearchFieldKind kind;

        private String propertyCodeOrNull;

        public static DataSetSearchField createExperimentProperty(String propertyCode)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.EXPERIMENT_PROPERTY, propertyCode);
        }

        public static DataSetSearchField createSampleProperty(String propertyCode)
        {
            return new DataSetSearchField(DataSetSearchFieldKind.SAMPLE_PROPERTY, propertyCode);
        }

        public static DataSetSearchField createSimpleField(DataSetSearchFieldKind fieldKind)
        {
            assert fieldKind != DataSetSearchFieldKind.SAMPLE_PROPERTY;
            assert fieldKind != DataSetSearchFieldKind.EXPERIMENT_PROPERTY;
            return new DataSetSearchField(fieldKind, null);
        }

        // GWT only
        private DataSetSearchField()
        {
            this(null, null);
        }

        private DataSetSearchField(DataSetSearchFieldKind kind, String propertyCodeOrNull)
        {
            this.kind = kind;
            this.propertyCodeOrNull = propertyCodeOrNull;
        }

        public DataSetSearchFieldKind getKind()
        {
            return kind;
        }

        public String tryGetPropertyCode()
        {
            return propertyCodeOrNull;
        }

    }

    public enum DataSetSearchFieldKind
    {
        EXPERIMENT, GROUP, PROJECT, SAMPLE, EXPERIMENT_TYPE, SAMPLE_TYPE, DATA_SET_TYPE, FILE_TYPE,
        EXPERIMENT_PROPERTY, SAMPLE_PROPERTY
    }

    public DataSetSearchCriterion()
    {
    }

    public DataSetSearchField getField()
    {
        return field;
    }

    public void setField(DataSetSearchField field)
    {
        this.field = field;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

}
