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
            "type" : "export",
            "permId" : objId,
            "error" : null,
            "index" : activeImageIdx,
            "url" : null,
            "export" :  imagingDataSetExport
        };
    }
}