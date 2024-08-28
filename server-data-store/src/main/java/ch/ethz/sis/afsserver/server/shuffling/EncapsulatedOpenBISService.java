/*
 * Copyright ETH 2007 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.server.shuffling;

import java.util.List;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKind;

public class EncapsulatedOpenBISService
{
    // TODO temporary implementation (not safe with multiple threads)

    private final OpenBIS openBIS;

    private final String openBISUser;

    private final String openBISPassword;

    public EncapsulatedOpenBISService(OpenBIS openBIS, String openBISUser, String openBISPassword)
    {
        this.openBIS = openBIS;
        this.openBISUser = openBISUser;
        this.openBISPassword = openBISPassword;
    }

    public SimpleDataSetInformationDTO[] listPhysicalDataSets()
    {
        try
        {
            String sessionToken = openBIS.login(openBISUser, openBISPassword);

            if (sessionToken == null)
            {
                throw new RuntimeException(
                        "Could not login to the AS server. Please check openBIS user and openBIS password in the AFS server configuration.");
            }

            DataSetSearchCriteria criteria = new DataSetSearchCriteria();
            criteria.withDataStore().withKind().thatIn(DataStoreKind.AFS);

            DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
            fetchOptions.withType();
            fetchOptions.withPhysicalData();
            fetchOptions.withDataStore();
            fetchOptions.withExperiment().withProject().withSpace();
            fetchOptions.withSample();

            SearchResult<DataSet> searchResult = openBIS.searchDataSets(criteria, fetchOptions);

            return searchResult.getObjects().stream().map(dataSet ->
            {
                SimpleDataSetInformationDTO simpleDTO = new SimpleDataSetInformationDTO();
                simpleDTO.setDataSetCode(dataSet.getCode());
                simpleDTO.setDataSetType(dataSet.getType().getCode());
                simpleDTO.setDataStoreCode(dataSet.getDataStore().getCode());
                simpleDTO.setDataSetShareId(dataSet.getPhysicalData().getShareId());
                simpleDTO.setDataSetLocation(dataSet.getPhysicalData().getLocation());
                simpleDTO.setDataSetSize(dataSet.getPhysicalData().getSize());
                simpleDTO.setStatus(DataSetArchivingStatus.valueOf(dataSet.getPhysicalData().getStatus().name()));
                simpleDTO.setPresentInArchive(dataSet.getPhysicalData().isPresentInArchive());
                simpleDTO.setSpeedHint(dataSet.getPhysicalData().getSpeedHint());
                simpleDTO.setStorageConfirmed(dataSet.getPhysicalData().isStorageConfirmation());
                simpleDTO.setRegistrationTimestamp(dataSet.getRegistrationDate());
                simpleDTO.setModificationTimestamp(dataSet.getModificationDate());
                simpleDTO.setAccessTimestamp(dataSet.getAccessDate());

                if (dataSet.getExperiment() != null)
                {
                    simpleDTO.setExperimentCode(dataSet.getExperiment().getCode());
                    simpleDTO.setProjectCode(dataSet.getExperiment().getProject().getCode());
                    simpleDTO.setSpaceCode(dataSet.getExperiment().getProject().getSpace().getCode());
                }

                if (dataSet.getSample() != null)
                {
                    simpleDTO.setSampleCode(dataSet.getSample().getCode());
                }

                return simpleDTO;
            }).toArray(SimpleDataSetInformationDTO[]::new);

        } finally
        {
            if (openBIS.getSessionToken() != null)
            {
                openBIS.logout();
            }
        }
    }

    public void updateShareIdAndSize(final String dataSetCode, final String shareId, final long size)
    {
        // TODO we cannot update AFS data sets yet via V3 API
    }

    public boolean dataSetExists(final String dataSetCode)
    {
        return true;
    }

    public boolean isDataSetOnTrashCanOrDeleted(final String dataSetCode)
    {
        return false;
    }

    public List<DataSetShareId> listDataSetShareIds()
    {
        try
        {
            String sessionToken = openBIS.login(openBISUser, openBISPassword);

            if (sessionToken == null)
            {
                throw new RuntimeException(
                        "Could not login to the AS server. Please check openBIS user and openBIS password in the AFS server configuration.");
            }

            DataSetSearchCriteria criteria = new DataSetSearchCriteria();
            criteria.withDataStore().withKind().thatIn(DataStoreKind.AFS);

            DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
            fetchOptions.withPhysicalData();

            SearchResult<DataSet> searchResult = openBIS.searchDataSets(criteria, fetchOptions);

            return searchResult.getObjects().stream().map(dataSet ->
            {
                DataSetShareId shareId = new DataSetShareId();
                shareId.setDataSetCode(dataSet.getCode());
                shareId.setShareId(dataSet.getPhysicalData().getShareId());
                return shareId;
            }).collect(Collectors.toList());

        } finally
        {
            if (openBIS.getSessionToken() != null)
            {
                openBIS.logout();
            }
        }
    }

    public DataSetShareId getDataSetShareId(final String dataSetCode)
    {
        try
        {
            String sessionToken = openBIS.login(openBISUser, openBISPassword);

            if (sessionToken == null)
            {
                throw new RuntimeException(
                        "Could not login to the AS server. Please check openBIS user and openBIS password in the AFS server configuration.");
            }

            DataSetSearchCriteria criteria = new DataSetSearchCriteria();
            criteria.withDataStore().withKind().thatIn(DataStoreKind.AFS);
            criteria.withDataStore().withCode().thatEquals(dataSetCode);

            DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
            fetchOptions.withPhysicalData();

            SearchResult<DataSet> searchResult = openBIS.searchDataSets(criteria, fetchOptions);

            if (!searchResult.getObjects().isEmpty())
            {
                DataSet dataSet = searchResult.getObjects().get(0);
                DataSetShareId shareId = new DataSetShareId();
                shareId.setDataSetCode(dataSetCode);
                shareId.setShareId(dataSet.getPhysicalData().getShareId());
                return shareId;
            } else
            {
                return null;
            }

        } finally
        {
            if (openBIS.getSessionToken() != null)
            {
                openBIS.logout();
            }
        }
    }
}
