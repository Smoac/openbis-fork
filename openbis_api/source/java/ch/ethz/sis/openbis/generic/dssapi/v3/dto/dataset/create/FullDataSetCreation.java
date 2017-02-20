package ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;
import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

/**
 * @author anttil
 */
@JsonObject("dss.dto.dataset.create.FullDataSetCreation")
@TechPreview
public class FullDataSetCreation implements ICreation
{
    private static final long serialVersionUID = 1L;

    private DataSetCreation metadataCreation;

    private List<DataSetFileCreation> fileMetadata;

    public DataSetCreation getMetadataCreation()
    {
        return metadataCreation;
    }

    public void setMetadataCreation(DataSetCreation metadataCreation)
    {
        this.metadataCreation = metadataCreation;
    }

    public List<DataSetFileCreation> getFileMetadata()
    {
        return fileMetadata;
    }

    public void setFileMetadata(List<DataSetFileCreation> fileMetadata)
    {
        this.fileMetadata = fileMetadata;
    }
}
