/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author anttil
 */
public class NormalizedSampleIdentifier
{
    private String containerSpace = "";

    private String containerCode = "";

    private String space;

    private String code;

    public NormalizedSampleIdentifier(Sample sample)
    {
        if (sample.getContainer() != null)
        {
            this.containerSpace = sample.getContainer().getSpace().getCode();
            this.containerCode = sample.getContainer().getCode();
        }

        this.space = sample.getSpace().getCode();
        if (sample.getCode().contains(":"))
        {
            this.code = sample.getCode().split(":")[1];
        } else
        {
            this.code = sample.getCode();
        }
    }

    public NormalizedSampleIdentifier(NewSample sample, String homeSpace)
    {

        String identifier = sample.getIdentifier();

        if (identifier.startsWith("/"))
        {
            String[] split = identifier.split("/");
            space = split[1];
            code = split[2];
        } else
        {
            code = identifier;
        }

        if (space == null)
        {
            space = normalizeSpaceCode(sample.getDefaultSpaceIdentifier());
        }

        if (space == null)
        {
            space = normalizeSpaceCode(homeSpace);
        }

        if (space == null)
        {
            throw UserFailureException.fromTemplate("Cannot determine space for sample "
                    + sample);
        }

        if (code.contains(":"))
        {
            String[] split = code.split(":");
            containerCode = split[0];
            code = split[1];
            containerSpace = space;

            if (identifier.contains("/") == false)
            {
                throw new UserFailureException("Invalid sample identifier: "
                        + identifier);
            }
        }

        if (sample.getCurrentContainerIdentifier() != null && containerSpace.isEmpty() == false)
        {
            throw new UserFailureException("Container specified twice: " + sample.getIdentifier()
                    + " and " + sample.getContainerIdentifier());
        } else if (sample.getCurrentContainerIdentifier() != null)
        {
            String containerIdentifier = sample.getCurrentContainerIdentifier();
            if (containerIdentifier.contains("/"))
            {
                String[] split = sample.getCurrentContainerIdentifier().split("/");
                this.containerSpace = split[1];
                this.containerCode = split[2];
            } else
            {
                this.containerSpace = space;
                this.containerCode = containerIdentifier;
            }
        }
    }

    public String getSampleIdentifier()
    {
        return "/" + space + "/" + code;
    }

    public String getContainerIdentifier()
    {
        if (containerSpace.isEmpty())
        {
            return null;
        } else
        {
            return "/" + containerSpace + "/" + containerCode;
        }
    }

    public String getContainerCode()
    {
        return containerCode;
    }

    public String getCode()
    {
        return code;
    }

    private String normalizeSpaceCode(String spaceCode)
    {
        if (spaceCode != null && spaceCode.startsWith("/"))
        {
            return spaceCode.substring(1);
        } else
        {
            return spaceCode;
        }
    }

    @Override
    public int hashCode()
    {
        return containerSpace.toUpperCase().hashCode() + containerCode.toUpperCase().hashCode()
                + space.toUpperCase().hashCode()
                + code.toUpperCase().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof NormalizedSampleIdentifier))
        {
            return false;
        }
        NormalizedSampleIdentifier nsi = (NormalizedSampleIdentifier) o;
        return nsi.containerSpace.equalsIgnoreCase(containerSpace) &&
                nsi.containerCode.equalsIgnoreCase(containerCode) &&
                nsi.space.equalsIgnoreCase(space) &&
                nsi.code.equalsIgnoreCase(code);
    }
}
