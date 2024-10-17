package ch.ethz.sis.afsserver.server.shuffling;

import java.util.List;

public interface IEncapsulatedOpenBISService
{
    List<SimpleDataSetInformationDTO> listDataSets();

    SimpleDataSetInformationDTO tryGetDataSet(String dataSetCode);

    void updateShareIdAndSize(String dataSetCode, String shareId, long size);
}
