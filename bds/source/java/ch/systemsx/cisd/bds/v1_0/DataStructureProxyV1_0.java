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

package ch.systemsx.cisd.bds.v1_0;

import java.util.Collections;
import java.util.Set;

import ch.systemsx.cisd.bds.DataSet;
import ch.systemsx.cisd.bds.ExperimentIdentifier;
import ch.systemsx.cisd.bds.ExperimentRegistrationTimestamp;
import ch.systemsx.cisd.bds.ExperimentRegistrator;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IAnnotations;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Reference;
import ch.systemsx.cisd.bds.Sample;
import ch.systemsx.cisd.bds.Version;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * A {@link IDataStructureV1_0} implementation which proxies calls to the encapsulated
 * {@link IDataStructureV1_0}.
 * 
 * @author Christian Ribeaud
 */
public class DataStructureProxyV1_0 implements IDataStructureV1_0
{
    protected final IDataStructureV1_0 dataStructure;

    private Mode mode;

    public DataStructureProxyV1_0(final IDataStructureV1_0 dataStructure)
    {
        assert dataStructure != null : "Unspecified proxied data structure.";
        this.dataStructure = dataStructure;
    }

    protected final void checkAccess()
    {
        if (mode == Mode.READ_ONLY)
        {
            ReadOnlyNode.denyAccess();
        }
    }

    //
    // IDataStructureV1_X
    //

    // Write methods

    public final void addFormatParameter(final FormatParameter formatParameter)
    {
        checkAccess();
        dataStructure.addFormatParameter(formatParameter);
    }

    public final void addReference(final Reference reference)
    {
        checkAccess();
        dataStructure.addReference(reference);
    }

    public final void setAnnotations(final IAnnotations imageAnnotations)
    {
        checkAccess();
        dataStructure.setAnnotations(imageAnnotations);
    }

    public final void setDataSet(final DataSet dataSet)
    {
        checkAccess();
        dataStructure.setDataSet(dataSet);
    }

    public final void setExperimentIdentifier(final ExperimentIdentifier experimentIdentifier)
    {
        checkAccess();
        dataStructure.setExperimentIdentifier(experimentIdentifier);
    }

    public final void setExperimentRegistrationTimestamp(
            final ExperimentRegistrationTimestamp experimentRegistrationTimestamp)
    {
        checkAccess();
        dataStructure.setExperimentRegistrationTimestamp(experimentRegistrationTimestamp);
    }

    public final void setExperimentRegistrator(final ExperimentRegistrator experimentRegistrator)
    {
        checkAccess();
        dataStructure.setExperimentRegistrator(experimentRegistrator);
    }

    public final void setFormat(final Format format)
    {
        checkAccess();
        dataStructure.setFormat(format);
    }

    public final void setSample(final Sample sample)
    {
        checkAccess();
        dataStructure.setSample(sample);
    }

    // IDataStructure methods

    public final void open(final Mode thatMode)
    {
        open(thatMode, true);
    }

    public void open(Mode thatMode, boolean validate)
    {
        dataStructure.open(thatMode, validate);
        this.mode = thatMode;
    }

    public final void close()
    {
        dataStructure.close();
    }

    public final void create()
    {
        mode = Mode.READ_WRITE;
        dataStructure.create();
    }

    public final boolean isOpenOrCreated()
    {
        return dataStructure.isOpenOrCreated();
    }

    // Read methods

    public final DataSet getDataSet()
    {
        return dataStructure.getDataSet();
    }

    public final ExperimentIdentifier getExperimentIdentifier()
    {
        return dataStructure.getExperimentIdentifier();
    }

    public final ExperimentRegistrator getExperimentRegistrator()
    {
        return dataStructure.getExperimentRegistrator();
    }

    public final ExperimentRegistrationTimestamp getExperimentRegistratorTimestamp()
    {
        return dataStructure.getExperimentRegistratorTimestamp();
    }

    public final IFormattedData getFormattedData() throws DataStructureException
    {
        return dataStructure.getFormattedData();
    }

    public final IDirectory getOriginalData()
    {
        final IDirectory originalData = dataStructure.getOriginalData();
        return mode == Mode.READ_ONLY ? ReadOnlyDirectory.tryCreateReadOnlyDirectory(originalData)
                : originalData;
    }

    public final IDirectory getStandardData()
    {
        final IDirectory standardData = dataStructure.getStandardData();
        return mode == Mode.READ_ONLY ? ReadOnlyDirectory.tryCreateReadOnlyDirectory(standardData)
                : standardData;
    }

    public final Sample getSample()
    {
        return dataStructure.getSample();
    }

    public final Set<Reference> getStandardOriginalMapping()
    {
        final Set<Reference> set = dataStructure.getStandardOriginalMapping();
        if (mode == Mode.READ_ONLY)
        {
            return Collections.unmodifiableSet(set);
        }
        return set;
    }

    public final Version getVersion()
    {
        return dataStructure.getVersion();
    }
}
