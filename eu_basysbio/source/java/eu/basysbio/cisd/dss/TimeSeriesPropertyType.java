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
 * Property type specification of a time series data set type.
 * 
 * @author Izabela Adamczyk
 */
enum TimeSeriesPropertyType {
	VALUE_TYPE_LIST(DataHeaderProperty.ValueType, true),

	CEL_LOC(DataHeaderProperty.CelLoc),

	CG_LIST(DataHeaderProperty.CG, true),

	CULTIVATION_METHOD_EXPERIMENT_CODE(
			DataHeaderProperty.CultivationMethodExperimentCode),

	EXPERIMENT_CODE(DataHeaderProperty.ExperimentCode),
	
	GENOTYPE(DataHeaderProperty.GENOTYPE),

	GROWTH_PHASE(DataHeaderProperty.GROWTH_PHASE),
	
	SCALE_LIST(DataHeaderProperty.Scale, true),

	TIME_POINT_LIST(DataHeaderProperty.TimePoint, true),

	TIME_POINT_TYPE(DataHeaderProperty.TimePointType),

	BI_ID(DataHeaderProperty.BiID),

	BIOLOGICAL_REPLICATE_CODE(DataHeaderProperty.BiologicalReplicatateCode),

	TECHNICAL_REPLICATE_CODE_LIST(DataHeaderProperty.TechnicalReplicateCode, true),

	TIME_SERIES_DATA_SET_TYPE(DataHeaderProperty.DataSetType),

	UPLOADER_EMAIL;

	private final DataHeaderProperty headerPropertyOrNull;

	private final boolean multipleValues;

	public boolean isMultipleValues() {
		return multipleValues;
	}

	private TimeSeriesPropertyType() {
		this(null);
	}

	private TimeSeriesPropertyType(DataHeaderProperty headerPropertyOrNull) {
		this(headerPropertyOrNull, false);
	}

	private TimeSeriesPropertyType(DataHeaderProperty headerPropertyOrNull,
			boolean multipleValues) {
		assert multipleValues==false || name().endsWith("_LIST") : "Inconsistent property: " + name();
		this.headerPropertyOrNull = headerPropertyOrNull;
		this.multipleValues = multipleValues;
	}
	
	boolean isOptional()
	{
	    return headerPropertyOrNull == null ? false : headerPropertyOrNull.isOptional();
	}

	public DataHeaderProperty getHeaderProperty() {
		if (headerPropertyOrNull == null) {
			throw new UnsupportedOperationException(name()
					+ " does not have header property.");
		}
		return headerPropertyOrNull;
	}
}
