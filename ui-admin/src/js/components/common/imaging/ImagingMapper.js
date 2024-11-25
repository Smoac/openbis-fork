import constants from "@src/js/components/common/imaging/constants";

export default class ImagingMapper{

    constructor(extOpenbis) {
        this.openbis = extOpenbis;
    }

    getImagingDataSetPreview(config, format, bytes, width, height, index, show, metadata, tags, comment) {
        let imagingDataSetPreview = new this.openbis.ImagingDataSetPreview();
        imagingDataSetPreview.config = config;
        imagingDataSetPreview.format = format;
        imagingDataSetPreview.bytes = bytes;
        imagingDataSetPreview.width = width;
        imagingDataSetPreview.height = height;
        imagingDataSetPreview.index = index;
        imagingDataSetPreview.show = show;
        imagingDataSetPreview.metadata = metadata;
        imagingDataSetPreview.tags = tags;
        imagingDataSetPreview.comment = comment;
        return imagingDataSetPreview;
    }

    mapToImagingDataSetPreview(preview) {
        let imagingDataSetPreview = new this.openbis.ImagingDataSetPreview();
        imagingDataSetPreview.config = preview.config;
        imagingDataSetPreview.format = preview.format;
        imagingDataSetPreview.bytes = preview.bytes;
        imagingDataSetPreview.width = preview.width;
        imagingDataSetPreview.height = preview.height;
        imagingDataSetPreview.index = preview.index;
        imagingDataSetPreview.show = preview.show;
        imagingDataSetPreview.metadata = preview.metadata;
        imagingDataSetPreview.tags = preview.tags;
        imagingDataSetPreview.comment = preview.comment;
        return imagingDataSetPreview;
    }

    mapToImagingUpdateParams(objId, activeImageIdx, preview) {
        /*let imagingPreviewContainer = new this.openbis.ImagingPreviewContainer();
        imagingPreviewContainer.type = constants.PREVIEW_TYPE;
        imagingPreviewContainer.permId = objId;
        imagingPreviewContainer.error = null;
        imagingPreviewContainer.index = activeImageIdx;
        imagingPreviewContainer.preview = this.mapToImagingDataSetPreview(preview)
        return imagingPreviewContainer;*/
        return {
            "type" : "preview",
            "permId" : objId,
            "error" : null,
            "index": activeImageIdx,
            "preview" :  this.mapToImagingDataSetPreview(preview)
        };
    }

    mapToImagingExportParams(objId, activeImageIdx, exportConfig, metadata) {
        let imagingDataSetExport = new this.openbis.ImagingDataSetExport();
        imagingDataSetExport.config = exportConfig;
        imagingDataSetExport.metadata = metadata;
        return {
            "type" : constants.EXPORT_TYPE,
            "permId" : objId,
            "error" : null,
            "index" : activeImageIdx,
            "url" : null,
            "export" :  imagingDataSetExport
        };
    }

    mapToImagingMultiExportParams(exportConfig, exportList) {
        const imagingDataSetMultiExportList = exportList.map(previewObj => {
            let imagingDataSetMultiExport = new this.openbis.ImagingDataSetMultiExport();
            imagingDataSetMultiExport.config = exportConfig;
            imagingDataSetMultiExport.metadata = previewObj.metadata;
            imagingDataSetMultiExport.permId = previewObj.datasetId;
            imagingDataSetMultiExport.imageIndex = previewObj.imageIdx;
            imagingDataSetMultiExport.previewIndex = previewObj.preview.index;
            return imagingDataSetMultiExport;
        });
        return {
            "type" : constants.MULTI_EXPORT_TYPE,
            "error" : null,
            "url" : null,
            "exports" :  imagingDataSetMultiExportList
        };
    }

}