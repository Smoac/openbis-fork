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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.List;

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;

/**
 * A sample to register.
 * 
 * @author Christian Ribeaud
 */
public class NewSample extends Identifier<NewSample> implements Comparable<NewSample>,
        IPropertiesBean
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final String SAMPLE_REGISTRATION_TEMPLATE_COMMENT =
            "# The \"container\" and \"parents\" columns are optional, only one should be specified.\n"
                    + "# \"container\" should contain a sample identifier, e.g. /SPACE/SAMPLE_1, while \"parents\" should contain comma separated list of sample identifiers. \n"
                    + "# If \"container\" sample is provided, the registered sample will become a \"component\" of it.\n"
                    + "# If \"parents\" are provided, the registered sample will become a \"child\" of all specified samples.\n";

    public static String WITH_EXPERIMENTS_COMMENT =
            "# The \"experiment\" column is optional, cannot be specified for shared samples and should contain experiment identifier, e.g. /SPACE/PROJECT/EXP_1\n";

    public static String WITH_SPACE_COMMENT =
            "# The \"default_space\" column is optional, it can be used to override home space for the row\n";

    public static final String CONTAINER = "container";

    public static final String DEFAULT_CONTAINER = "default_container";

    public static final String PARENT = "parent";

    public static final String PARENTS = "parents";

    public static final String EXPERIMENT = "experiment";

    public static final String SPACE = "default_space";

    private SampleType sampleType;

    /**
     * Set of parent sample codes or identifiers. It will be assumed that all the samples belong to
     * the same group as the child sample.
     */
    private String[] parentsOrNull;

    /**
     * The container identifier.
     */
    private String containerIdentifier;

    /**
     * The experiment identifier.
     */
    private String experimentIdentifier;

    /***
     * The space code for this row home space
     */
    private String defaultSpaceIdentifier;

    private IEntityProperty[] properties = IEntityProperty.EMPTY_ARRAY;

    private List<NewAttachment> attachments;

    public NewSample()
    {
    }

    private NewSample(final String identifier, final SampleType sampleType,
            final String containerIdentifier)
    {
        setIdentifier(identifier);
        setSampleType(sampleType);
        setContainerIdentifier(containerIdentifier);
    }

    public static NewSample createWithParent(final String identifier, final SampleType sampleType,
            final String containerIdentifier, final String parentIdentifier)
    {
        NewSample result = new NewSample(identifier, sampleType, containerIdentifier);
        result.setParentIdentifier(parentIdentifier);
        return result;
    }

    public static NewSample createWithParents(final String identifier, final SampleType sampleType,
            final String containerIdentifier, final String[] parents)
    {
        NewSample result = new NewSample(identifier, sampleType, containerIdentifier);
        result.setParentsOrNull(parents);
        return result;
    }

    public NewSample(final String identifier, SampleType sampleType, String containerIdentifier,
            String[] parentsOrNull, String experimentIdentifier, String spaceCode,
            IEntityProperty[] properties, List<NewAttachment> attachments)
    {
        this(identifier, sampleType, containerIdentifier);
        this.parentsOrNull = parentsOrNull;
        this.experimentIdentifier = experimentIdentifier;
        this.defaultSpaceIdentifier = spaceCode;
        this.properties = properties;
        this.attachments = attachments;
    }

    public List<NewAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(List<NewAttachment> attachments)
    {
        this.attachments = attachments;
    }

    public final SampleType getSampleType()
    {
        return sampleType;
    }

    public final void setSampleType(final SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    public String[] getParentsOrNull()
    {
        return parentsOrNull;
    }

    public void setParentsOrNull(String[] parents)
    {
        this.parentsOrNull = parents;
    }

    @BeanProperty(label = PARENTS, optional = true)
    public void setParents(String parents)
    {
        if (parents != null)
        {
            String[] split = parents.split(",");
            setParentsOrNull(split);
        } else
        {
            setParentsOrNull(new String[0]);
        }
    }

    /** @deprecated convenience method for tests - use {@link #getParentsOrNull()} instead */
    @Deprecated
    public final String getParentIdentifier()
    {
        if (getParentsOrNull() == null || getParentsOrNull().length == 0)
        {
            return null;
        } else if (getParentsOrNull().length > 1)
        {
            throw new IllegalStateException("Sample " + getIdentifier()
                    + " has more than one parent");
        } else
        {
            return getParentsOrNull()[0];
        }
    }

    /**
     * @deprecated kept for backward compatibility and used as a convenience method for tests - use
     *             {@link #setParents(String)} instead
     */
    @Deprecated
    @BeanProperty(label = PARENT, optional = true)
    public final void setParentIdentifier(final String parent)
    {
        setParents(parent);
    }

    public final String getContainerIdentifier()
    {
        return containerIdentifier;
    }

    @BeanProperty(label = CONTAINER, optional = true)
    public final void setContainerIdentifier(final String container)
    {
        this.containerIdentifier = container;
    }

    public final String getDefaultContainerIdentifier()
    {
        return containerIdentifier;
    }

    @BeanProperty(label = DEFAULT_CONTAINER, optional = true)
    public final void setDefaultContainerIdentifier(final String defaultContainer)
    {
        this.containerIdentifier = defaultContainer;
    }

    public String getExperimentIdentifier()
    {
        return experimentIdentifier;
    }

    @BeanProperty(label = EXPERIMENT, optional = true)
    public void setExperimentIdentifier(String experimentIdentifier)
    {
        this.experimentIdentifier = experimentIdentifier;
    }

    public String getDefaultSpaceIdentifier()
    {
        return defaultSpaceIdentifier;
    }

    @BeanProperty(label = SPACE, optional = true)
    public void setDefaultSpaceIdentifier(String spaceIdentifier)
    {
        this.defaultSpaceIdentifier = spaceIdentifier;
    }

    public final IEntityProperty[] getProperties()
    {
        return properties;
    }

    public final void setProperties(final IEntityProperty[] properties)
    {
        this.properties = properties;
    }

    //
    // Object
    //

    @Override
    public final String toString()
    {
        return getIdentifier();
    }

    // NOTE:
    // Special equality check for NewSamples that is not complete but speeds up uniqueness check
    // of new sample codes during import. The check on the DB level is complete.
    //
    // Here we compare a pair of container's identifier and new sample's identifier.
    // 1. This comparison doesn't have the knowledge about home group so it will say that
    // 'SAMPLE_1' != /HOME_GROUP/SAMPLE_1'.
    // 2. We need to also use container identifier because when samples are registered container
    // code is not required in its identifier.
    //
    // So this equals may return 'false' for NewSample objects that would in fact create
    // samples with the same identifiers, but when it returns 'true' it is always correct.
    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof NewSample == false)
        {
            return false;
        }
        final NewSample that = (NewSample) obj;
        final String thisCombinedIdentifier =
                StringUtils.emptyIfNull(this.getDefaultSpaceIdentifier()) + this.getIdentifier()
                        + this.getContainerIdentifier();
        final String thatCombinedIdentifier =
                StringUtils.emptyIfNull(this.getDefaultSpaceIdentifier()) + that.getIdentifier()
                        + that.getContainerIdentifier();
        return thisCombinedIdentifier.equals(thatCombinedIdentifier);
    }
}
