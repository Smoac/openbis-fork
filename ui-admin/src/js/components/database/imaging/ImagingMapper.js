import constants from "@src/js/components/database/imaging/constants";

export default class ImagingMapper{

    constructor(extOpenbis) {
        this.openbis = extOpenbis;
    }

    getImagingDataSetPreview(config, format, bytes, width, height, index, show, metadata) {
        let imagingDataSetPreview = new this.openbis.ImagingDataSetPreview();
        imagingDataSetPreview.config = config;
        imagingDataSetPreview.format = format;
        imagingDataSetPreview.bytes = bytes;
        imagingDataSetPreview.width = width;
        imagingDataSetPreview.height = height;
        imagingDataSetPreview.index = index;
        imagingDataSetPreview.show = show;
        imagingDataSetPreview.metadata = metadata;
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
        /*let imagingExportContainer = new this.openbis.ImagingExportContainer();
        imagingExportContainer.permId = objId;
        imagingExportContainer.type = constants.EXPORT_TYPE;
        imagingExportContainer.error = null;
        imagingExportContainer.index = activeImageIdx;
        imagingExportContainer.export = imagingDataSetExport;
        imagingExportContainer.url = null;
        return imagingExportContainer;*/
        return {
            "type" : constants.EXPORT_TYPE,
            "permId" : objId,
            "error" : null,
            "index" : activeImageIdx,
            "url" : null,
            "export" :  imagingDataSetExport
        };
    }
}