/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;

/**
 * Translates {@link DataSetTypePropertyTypePE} to
 * {@link DataSetTypePropertyType}.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetTypePropertyTypeTranslator {

	static private class DataSetTypePropertyTypeTranslatorHelper
			extends
			AbstractEntityTypePropertyTypeTranslator<DataSetType, DataSetTypePropertyType, DataSetTypePropertyTypePE> {
		@Override
		void setSpecificFields(DataSetTypePropertyType result,
				DataSetTypePropertyTypePE etptPE) {
		}

		@Override
		DataSetType translate(EntityTypePE entityTypePE) {
			return DataSetTypeTranslator
					.translate((DataSetTypePE) entityTypePE);
		}

		@Override
		DataSetTypePropertyType create() {
			return new DataSetTypePropertyType();
		}
	}

	public static List<DataSetTypePropertyType> translate(
			Set<DataSetTypePropertyTypePE> DataSetTypePropertyTypes,
			PropertyType result) {
		return new DataSetTypePropertyTypeTranslatorHelper().translate(
				DataSetTypePropertyTypes, result);
	}

	public static DataSetTypePropertyType translate(
			DataSetTypePropertyTypePE entityTypePropertyType) {
		return new DataSetTypePropertyTypeTranslatorHelper()
				.translate(entityTypePropertyType);
	}

	public static List<DataSetTypePropertyType> translate(
			Set<DataSetTypePropertyTypePE> DataSetTypePropertyTypes,
			DataSetType result) {
		return new DataSetTypePropertyTypeTranslatorHelper().translate(
				DataSetTypePropertyTypes, result);
	}

}
