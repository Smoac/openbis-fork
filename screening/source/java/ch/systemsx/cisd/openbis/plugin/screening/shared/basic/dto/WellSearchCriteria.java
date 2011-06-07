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
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.WellSearchGrid;

/**
 * Describes a list of materials for which we search in the {@link WellSearchGrid}.
 * 
 * @author Tomasz Pylak
 */
public class WellSearchCriteria implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /** points to one experiment */
    public static final class SingleExperimentSearchCriteria implements ISerializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private TechId experimentId;

        private String experimentPermId;

        private String experimentIdentifier; // for display purposes and links in simple view mode

        // GWT only
        @SuppressWarnings("unused")
        private SingleExperimentSearchCriteria()
        {
        }

        public SingleExperimentSearchCriteria(IEntityInformationHolderWithIdentifier experiment)
        {
            this(experiment.getId(), experiment.getPermId(), experiment.getIdentifier());
        }

        public SingleExperimentSearchCriteria(long experimentId, String experimentPermId,
                String experimentIdentifier)
        {
            this.experimentId = new TechId(experimentId);
            this.experimentPermId = experimentPermId;
            this.experimentIdentifier = experimentIdentifier;
        }

        public TechId getExperimentId()
        {
            return experimentId;
        }

        public String getExperimentPermId()
        {
            return experimentPermId;
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

    /** Points to one experiment, all experiments of a project or all accessible experiments. */
    public static final class ExperimentSearchCriteria implements IsSerializable, Serializable
    {
        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        // if null, all experiments are taken into account (from one or all projects)
        private SingleExperimentSearchCriteria experimentOrNull;

        private BasicProjectIdentifier projectIdOrNull;

        /**
         * Valid only if single experiment is used as a criteria.
         */
        // TODO 2011-06-07, Tomasz Pylak: Determines UI behaviour, should be refactored out from
        // this server side DTO.
        private boolean restrictGlobalScopeLinkToProject = false;

        // GWT only
        private ExperimentSearchCriteria()
        {
        }

        public SingleExperimentSearchCriteria tryGetExperiment()
        {
            return experimentOrNull;
        }

        private ExperimentSearchCriteria(SingleExperimentSearchCriteria experimentOrNull,
                boolean restrictGlobalScopeLinkToProject)
        {
            this.experimentOrNull = experimentOrNull;
            this.projectIdOrNull = null;
            this.restrictGlobalScopeLinkToProject = restrictGlobalScopeLinkToProject;
        }

        private ExperimentSearchCriteria(BasicProjectIdentifier projectIdOrNull)
        {
            this.projectIdOrNull = projectIdOrNull;
        }

        public static ExperimentSearchCriteria createExperiment(
                SingleExperimentSearchCriteria experiment)
        {
            return createExperiment(experiment);
        }

        public static ExperimentSearchCriteria createExperiment(
                SingleExperimentSearchCriteria experiment, boolean restrictGlobalScopeLinkToProject)
        {
            return new ExperimentSearchCriteria(experiment, restrictGlobalScopeLinkToProject);
        }

        public static ExperimentSearchCriteria createExperiment(
                IEntityInformationHolderWithIdentifier experiment)
        {
            return createExperiment(experiment, false);
        }

        public static ExperimentSearchCriteria createExperiment(
                IEntityInformationHolderWithIdentifier experiment,
                boolean restrictGlobalScopeLinkToProject)
        {
            return createExperiment(new SingleExperimentSearchCriteria(experiment),
                    restrictGlobalScopeLinkToProject);
        }

        public static ExperimentSearchCriteria createAllExperiments()
        {
            return new ExperimentSearchCriteria(null, false);
        }

        public static final ExperimentSearchCriteria createAllExperimentsForProject(
                BasicProjectIdentifier projectIdentifier)
        {
            if (projectIdentifier == null)
            {
                throw new IllegalArgumentException("Project identifier cannot be null");
            }
            return new ExperimentSearchCriteria(projectIdentifier);
        }

        public BasicProjectIdentifier tryGetProjectIdentifier()
        {
            return projectIdOrNull;
        }

        /**
         * Valid only if single experiment is used as a criteria.<br>
         * It determines the behavior of the link from material detail view in a single experiment
         * context to the material detail view in 'global' context. If this parameter is true, the
         * context will be the project to which the current experiment belongs, otherwise the
         * context will be switched to all experiments.
         */
        public boolean getRestrictGlobalSearchLinkToProject()
        {
            return restrictGlobalScopeLinkToProject;
        }

        public ExperimentSearchByProjectCriteria tryAsSearchByProjectCriteria()
        {
            if (tryGetExperiment() != null)
            {
                return null;
            } else if (projectIdOrNull != null)
            {
                return ExperimentSearchByProjectCriteria
                        .createAllExperimentsForProject(projectIdOrNull);
            } else
            {
                return ExperimentSearchByProjectCriteria.createAllExperimentsForAllProjects();
            }
        }

        @Override
        public String toString()
        {
            if (experimentOrNull == null)
            {
                if (projectIdOrNull == null)
                {
                    return "all experiments";
                } else
                {
                    return "all experiments from project " + projectIdOrNull;
                }
            } else
            {
                return experimentOrNull.toString();
            }
        }

    }

    public static final class ExperimentSearchByProjectCriteria implements IsSerializable,
            Serializable
    {

        private static final long serialVersionUID = ServiceVersionHolder.VERSION;

        private BasicProjectIdentifier projectIdOrNull;

        public static final ExperimentSearchByProjectCriteria createAllExperimentsForProject(
                BasicProjectIdentifier projectIdentifier)
        {
            if (projectIdentifier == null)
            {
                throw new IllegalArgumentException("Project identifier cannot be null");
            }
            return new ExperimentSearchByProjectCriteria(projectIdentifier);
        }

        public static final ExperimentSearchByProjectCriteria createAllExperimentsForAllProjects()
        {
            return new ExperimentSearchByProjectCriteria(null);
        }

        // GWT only
        private ExperimentSearchByProjectCriteria()
        {
        }

        private ExperimentSearchByProjectCriteria(BasicProjectIdentifier projectIdOrNull)
        {
            this.projectIdOrNull = projectIdOrNull;
        }

        public BasicProjectIdentifier tryGetProjectIdentifier()
        {
            return projectIdOrNull;
        }

        public boolean isAllExperiments()
        {
            return projectIdOrNull == null;
        }

        public ExperimentSearchCriteria asExtendedCriteria()
        {
            if (projectIdOrNull != null)
            {
                return ExperimentSearchCriteria.createAllExperimentsForProject(projectIdOrNull);
            } else
            {
                return ExperimentSearchCriteria.createAllExperiments();
            }
        }

        @Override
        public String toString()
        {
            if (isAllExperiments())
            {
                return super.toString();
            } else
            {
                return projectIdOrNull.toString();
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
                    + ", types = " + Arrays.toString(materialTypeCodes) + ", exactMatchOnly = "
                    + exactMatchOnly;
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
    private WellSearchCriteria()
    {
    }

    public WellSearchCriteria(ExperimentSearchCriteria experimentCriteria,
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
