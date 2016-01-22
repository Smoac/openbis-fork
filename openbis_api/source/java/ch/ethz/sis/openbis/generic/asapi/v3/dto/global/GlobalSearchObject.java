/*
 * Copyright 2014 ETH Zuerich, CISD
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.global;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/*
 * Class automatically generated with DtoGenerator
 */
@JsonObject("dto.global.GlobalSearchObject")
public class GlobalSearchObject implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JsonProperty
    private GlobalSearchObjectFetchOptions fetchOptions;

    @JsonProperty
    private GlobalSearchObjectKind entityKind;

    @JsonProperty
    private EntityTypePermId entityTypeId;

    @JsonProperty
    private IObjectId objectId;

    @JsonProperty
    private String registratorEmail;

    @JsonProperty
    private String matchingField;

    @JsonProperty
    private String matchingText;

    @JsonProperty
    private DataSet dataSet;

    @JsonProperty
    private Sample sample;

    @JsonProperty
    private Material material;

    @JsonProperty
    private Project project;

    @JsonProperty
    private Experiment experiment;

    @JsonProperty
    private Space space;

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public GlobalSearchObjectFetchOptions getFetchOptions()
    {
        return fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    public void setFetchOptions(GlobalSearchObjectFetchOptions fetchOptions)
    {
        this.fetchOptions = fetchOptions;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public GlobalSearchObjectKind getEntityKind()
    {
        return entityKind;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityKind(GlobalSearchObjectKind entityKind)
    {
        this.entityKind = entityKind;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public EntityTypePermId getEntityTypeId()
    {
        return entityTypeId;
    }

    // Method automatically generated with DtoGenerator
    public void setEntityTypeId(EntityTypePermId entityTypeId)
    {
        this.entityTypeId = entityTypeId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public IObjectId getObjectId()
    {
        return objectId;
    }

    // Method automatically generated with DtoGenerator
    public void setObjectId(IObjectId objectId)
    {
        this.objectId = objectId;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getRegistratorEmail()
    {
        return registratorEmail;
    }

    // Method automatically generated with DtoGenerator
    public void setRegistratorEmail(String registratorEmail)
    {
        this.registratorEmail = registratorEmail;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getMatchingField()
    {
        return matchingField;
    }

    // Method automatically generated with DtoGenerator
    public void setMatchingField(String matchingField)
    {
        this.matchingField = matchingField;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public String getMatchingText()
    {
        return matchingText;
    }

    // Method automatically generated with DtoGenerator
    public void setMatchingText(String matchingText)
    {
        this.matchingText = matchingText;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public DataSet getDataSet()
    {
        if (getFetchOptions().hasDataSet())
        {
            return dataSet;
        }
        else
        {
            throw new NotFetchedException("Data Set has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setDataSet(DataSet dataSet)
    {
        this.dataSet = dataSet;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Sample getSample()
    {
        if (getFetchOptions().hasSample())
        {
            return sample;
        }
        else
        {
            throw new NotFetchedException("Sample has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSample(Sample sample)
    {
        this.sample = sample;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Material getMaterial()
    {
        if (getFetchOptions().hasMaterial())
        {
            return material;
        }
        else
        {
            throw new NotFetchedException("Material has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setMaterial(Material material)
    {
        this.material = material;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Project getProject()
    {
        if (getFetchOptions().hasProject())
        {
            return project;
        }
        else
        {
            throw new NotFetchedException("Project has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setProject(Project project)
    {
        this.project = project;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Experiment getExperiment()
    {
        if (getFetchOptions().hasExperiment())
        {
            return experiment;
        }
        else
        {
            throw new NotFetchedException("Experiment has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setExperiment(Experiment experiment)
    {
        this.experiment = experiment;
    }

    // Method automatically generated with DtoGenerator
    @JsonIgnore
    public Space getSpace()
    {
        if (getFetchOptions().hasSpace())
        {
            return space;
        }
        else
        {
            throw new NotFetchedException("Space has not been fetched.");
        }
    }

    // Method automatically generated with DtoGenerator
    public void setSpace(Space space)
    {
        this.space = space;
    }

}
