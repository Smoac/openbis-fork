import messages from "@src/js/common/messages.js";
import constants from '@src/js/components/common/imaging/constants.js';

export async function loadGalleryViewFilters(imagingFacade, setDataSetTypes) {
	const imagingDatasetTypes = await imagingFacade.loadDataSetTypes();
	const tagsVocabularyTerms = await imagingFacade.loadImagingTagsVocabularyTerms();
	imagingDatasetTypes.push({label: constants.IMAGING_TAGS_LABEL, value: constants.IMAGING_TAGS, options: tagsVocabularyTerms})
	imagingDatasetTypes.push({label: messages.get(messages.ALL_PROPERTIES), value: messages.ALL});
	setDataSetTypes(imagingDatasetTypes);
}

export async function loadPreviewsInfo(imagingFacade, objId, objType, galleryFilter, paging, setPreviewsInfo, setIsLoaded) {
	let {previewContainerList, totalCount} = galleryFilter.text.length >= 3 ?
                await imagingFacade.filterGallery(objId, objType, galleryFilter.operator, galleryFilter.text, galleryFilter.property, paging.page, paging.pageSize)
                : await imagingFacade.loadPaginatedGalleryDatasets(objId, objType, paging.page, paging.pageSize)
            setPreviewsInfo({previewContainerList, totalCount});
            setIsLoaded(true);
}
