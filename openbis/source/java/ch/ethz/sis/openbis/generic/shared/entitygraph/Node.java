/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.shared.entitygraph;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IProjectHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISampleHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;

public class Node<T extends ICodeHolder & IModificationDateHolder & IModifierHolder & IPermIdHolder & IRegistrationDateHolder & IRegistratorHolder>
{
    private final T entity;

    private final List<EdgeNodePair> connections;

    private final List<String> binaryData;

    public T getEntity()
    {
        return entity;
    }

    public Node(T entity)
    {
        this.entity = entity;
        this.connections = new ArrayList<EdgeNodePair>();
        this.binaryData = new ArrayList<String>();
    }

    @Override
    public int hashCode()
    {
        return this.getIdentifier().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node<?> other = (Node<?>) obj;
        if (entity == null)
        {
            if (other.entity != null)
            {
                return false;
            }
        } else if (this.getIdentifier().equals(other.getIdentifier()) == false)
        {
            return false;
        }
        else if (getEntityKind().equals(other.getEntityKind()) == false)
        {
            return false;
        }
        return true;
    }

    public String getCode()
    {
        return this.entity.getCode();
    }

    public String getPermId()
    {
        return this.entity.getPermId().toString();
    }

    public Map<String, String> getPropertiesOrNull()
    {
        if (IPropertiesHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        return ((IPropertiesHolder) entity).getProperties();
    }

    public String getSpaceOrNull()
    {
        if (ISpaceHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        return ((ISpaceHolder) entity).getSpace().getCode();
    }

    public Experiment getExperimentOrNull()
    {
        if (IExperimentHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        else
        {
            return ((IExperimentHolder) entity).getExperiment();
        }
    }

    public String getExperimentIdentifierOrNull()
    {
        Experiment exp = getExperimentOrNull();
        if (exp != null)
        {
            return exp.getIdentifier().toString();
        }
        return null;
    }

    public String getProjectOrNull()
    {
        if (IProjectHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        return ((IProjectHolder) entity).getProject().getCode();
    }

    public Sample getSampleOrNull()
    {
        if (ISampleHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        else 
        {
            return ((ISampleHolder) entity).getSample();
        }
    }

    public String getSampleIdentifierOrNull()
    {
        Sample sample = getSampleOrNull();
        if (sample != null)
        {
            return sample.getIdentifier().toString();
        }
        return null;
    }

    public String getIdentifier()
    {
        if (entity instanceof Project)
        {
            return ((Project) entity).getIdentifier().getIdentifier();
        }
        if (entity instanceof Experiment)
        {
            return ((Experiment) entity).getIdentifier().getIdentifier();
        }
        if (entity instanceof Sample)
        {
            return ((Sample) entity).getIdentifier().getIdentifier();
        }
        else if (entity instanceof DataSet)
        {
            return ((DataSet) entity).getPermId().toString();
        }
        // TODO exception
        return null;
    }

    public String getTypeCodeOrNull()
    {
        if (IPropertiesHolder.class.isAssignableFrom(entity.getClass()) == false)
        {
            return null;
        }
        if(entity instanceof Sample) {
            return ((Sample) entity).getType().getCode();
        }
        else if (entity instanceof Experiment)
        {
            return ((Experiment) entity).getType().getCode();
        }
        else if (entity instanceof DataSet)
        {
            return ((DataSet) entity).getType().getCode();
        }
        // TODO exception
        return null;
    }

    public String getEntityKind()
    {
        if (entity instanceof Project)
        {
            return "PROJECT";
        }
        else if (entity instanceof Experiment)
        {
            return "EXPERIMENT";
        }
        else if (entity instanceof Sample)
        {
            return "SAMPLE";
        }
        else if (entity instanceof DataSet)
        {
            return "DATA_SET";
        }
        // TODO exception
        return null;
    }

    public Date getRegistrationDate()
    {
        return entity.getRegistrationDate();
    }

    public void addConnection(EdgeNodePair enPair)
    {
        connections.add(enPair);
    }

    public List<EdgeNodePair> getConnections()
    {
        return connections;
    }

    public void addBinaryData(String link)
    {
        binaryData.add(link);
    }

    public List<String> getBinaryData()
    {
        return binaryData;
    }

    public boolean hasBinaryData() {
        if (entity instanceof DataSet)
        {
            return true;
        }
        return binaryData.size() > 0;
    }
}
