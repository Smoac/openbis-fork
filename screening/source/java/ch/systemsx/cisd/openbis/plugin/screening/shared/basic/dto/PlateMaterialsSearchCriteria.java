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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes a list of materials for which we search in the Plate Material Reviewer.
 * 
 * @author Tomasz Pylak
 */
public class PlateMaterialsSearchCriteria implements IsSerializable, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /** points to one experiment */
    public static final class SingleExperimentSearchCriteria implements IsSerializable,
            Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private TechId experimentId;

        private String experimentIdentifier; // for display purposes and links in simple view mode

        // GWT only
        @SuppressWarnings("unused")
        private SingleExperimentSearchCriteria()
        {
        }

        public SingleExperimentSearchCriteria(long experimentId, String experimentIdentifier)
        {
            this.experimentId = new TechId(experimentId);
            this.experimentIdentifier = experimentIdentifier;
        }

        public TechId getExperimentId()
        {
            return experimentId;
        }

        public String getExperimentIdentifier()
        {
            return experimentIdentifier;
        }

        @Override
        public String toString()
        {
            return "experiment " + experimentIdentifier;
        }
    }

    /** points to one experiment or all of them */
    public static final class ExperimentSearchCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        // if null, all experiments are taken into account
        private SingleExperimentSearchCriteria experimentOrNull;

        public static final ExperimentSearchCriteria createAllExperiments()
        {
            return new ExperimentSearchCriteria(null);

        }

        public static ExperimentSearchCriteria createExperiment(
                SingleExperimentSearchCriteria experiment)
        {
            assert experiment != null : "experiment not specified";
            return new ExperimentSearchCriteria(experiment);
        }

        public static final ExperimentSearchCriteria createExperiment(long experimentId,
                String experimentIdentifier)
        {
            return new ExperimentSearchCriteria(new SingleExperimentSearchCriteria(experimentId,
                    experimentIdentifier));
        }

        // GWT only
        private ExperimentSearchCriteria()
        {
        }

        private ExperimentSearchCriteria(SingleExperimentSearchCriteria experimentOrNull)
        {
            this.experimentOrNull = experimentOrNull;
        }

        public SingleExperimentSearchCriteria tryGetExperiment()
        {
            return experimentOrNull;
        }

        @Override
        public String toString()
        {
            if (experimentOrNull == null)
            {
                return "all experiments";
            } else
            {
                return experimentOrNull.toString();
            }
        }
    }

    public static final class MaterialSearchCodesCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private String[] materialCodesOrProperties;

        private String[] materialTypeCodes;

        private boolean exactMatchOnly;

        // GWT only
        @SuppressWarnings("unused")
        private MaterialSearchCodesCriteria()
        {
        }

        public MaterialSearchCodesCriteria(String[] materialCodesOrProperties,
                String[] materialTypeCodes, boolean exactMatchOnly)
        {
            this.exactMatchOnly = exactMatchOnly;
            for (int i = 0; i < materialCodesOrProperties.length; i++)
            {
                assert StringUtils.isBlank(materialCodesOrProperties[i]) == false : "material search property is blank";
                materialCodesOrProperties[i] = materialCodesOrProperties[i].toUpperCase();
            }
            this.materialCodesOrProperties = materialCodesOrProperties;
            this.materialTypeCodes = materialTypeCodes;
        }

        public String[] getMaterialCodesOrProperties()
        {
            return materialCodesOrProperties;
        }

        public boolean isExactMatchOnly()
        {
            return exactMatchOnly;
        }

        public String[] getMaterialTypeCodes()
        {
            return materialTypeCodes;
        }

        @Override
        public String toString()
        {
            return "Material codes (or properties) = " + Arrays.toString(materialCodesOrProperties)
                    + ", types = " + Arrays.toString(materialTypeCodes);
        }
    }

    public static final class MaterialSearchCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        // Only one criteria is present

        // -- code or property criteria
        private MaterialSearchCodesCriteria codesOrPropertiesCriteriaOrNull;

        // -- technical id criteria
        private TechId materialIdOrNull;

        /**
         * We look for wells containing materials which have a code contained in the specified list
         * of codes and type contained in the specified list of types.
         */
        public static final MaterialSearchCriteria createCodesCriteria(String[] materialCodes,
                String[] materialTypeCodes, boolean exactMatchOnly)
        {
            return create(new MaterialSearchCodesCriteria(materialCodes, materialTypeCodes,
                    exactMatchOnly));
        }

        public static MaterialSearchCriteria create(
                MaterialSearchCodesCriteria materialCodesCriteria)
        {
            return new MaterialSearchCriteria(materialCodesCriteria, null);
        }

        public static final MaterialSearchCriteria createIdCriteria(TechId materialId)
        {
            assert materialId != null;

            return new MaterialSearchCriteria(null, materialId);
        }

        // GWT only
        private MaterialSearchCriteria()
        {
        }

        private MaterialSearchCriteria(MaterialSearchCodesCriteria codesOrPropertiesCriteriaOrNull,
                TechId materialIdOrNull)
        {
            this.codesOrPropertiesCriteriaOrNull = codesOrPropertiesCriteriaOrNull;
            this.materialIdOrNull = materialIdOrNull;
        }

        public MaterialSearchCodesCriteria tryGetMaterialCodesOrProperties()
        {
            return codesOrPropertiesCriteriaOrNull;
        }

        public TechId tryGetMaterialId()
        {
            return materialIdOrNull;
        }

        @Override
        public String toString()
        {
            if (materialIdOrNull != null)
            {
                return "Material with id = " + materialIdOrNull;
            } else if (codesOrPropertiesCriteriaOrNull != null)
            {
                return codesOrPropertiesCriteriaOrNull.toString();
            } else
            {
                throw new IllegalStateException("unexpected material search criteria");
            }
        }
    }

    private MaterialSearchCriteria materialCriteria;

    private ExperimentSearchCriteria experimentCriteria;

    // GWT
    @SuppressWarnings("unused")
    private PlateMaterialsSearchCriteria()
    {
    }

    public PlateMaterialsSearchCriteria(ExperimentSearchCriteria experimentCriteria,
            MaterialSearchCriteria materialCriteria)
    {
        assert experimentCriteria != null;
        assert materialCriteria != null;

        this.materialCriteria = materialCriteria;
        this.experimentCriteria = experimentCriteria;
    }

    @Override
    public String toString()
    {
        return "Search criteria: " + experimentCriteria + ", " + materialCriteria;
    }

    /** null means all experiments */
    public ExperimentSearchCriteria getExperimentCriteria()
    {
        return experimentCriteria;
    }

    public MaterialSearchCriteria getMaterialSearchCriteria()
    {
        return materialCriteria;
    }
}
