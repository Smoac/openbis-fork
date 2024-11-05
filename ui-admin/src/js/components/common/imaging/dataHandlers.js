import messages from "@src/js/common/messages.js";

export async function loadDataSetTypes(imagingFacade, setDataSetTypes) {
	const dataSetTypes = await imagingFacade.loadDataSetTypes();
	dataSetTypes.push({label: 'All Properties', value: messages.get(messages.ALL)});
	setDataSetTypes(dataSetTypes);
}

export async function loadPreviewsInfo(imagingFacade, objId, objType, galleryFilter, paging, setPreviewsInfo, setIsLoaded) {
	let {previewContainerList, totalCount} = galleryFilter.text.length >= 3 ?
                await imagingFacade.filterGallery(objId, objType, galleryFilter.operator, galleryFilter.text, galleryFilter.property, paging.page, paging.pageSize)
                : await imagingFacade.loadPaginatedGalleryDatasets(objId, objType, paging.page, paging.pageSize)
            setPreviewsInfo({previewContainerList, totalCount});
            setIsLoaded(true);
}
