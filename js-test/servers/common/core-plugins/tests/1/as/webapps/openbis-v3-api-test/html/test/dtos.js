define(['as/dto/space/create/SpaceCreation',
		'as/dto/project/create/ProjectCreation',
		'as/dto/experiment/create/ExperimentCreation',
		'as/dto/sample/create/SampleCreation',
		'as/dto/material/create/MaterialCreation',
		'as/dto/attachment/create/AttachmentCreation',
		'as/dto/space/update/SpaceUpdate',
		'as/dto/project/update/ProjectUpdate',
		'as/dto/experiment/update/ExperimentUpdate',
		'as/dto/sample/update/SampleUpdate',
		'as/dto/dataset/update/DataSetUpdate',
		'as/dto/dataset/update/PhysicalDataUpdate',
		'as/dto/material/update/MaterialUpdate',
		'as/dto/space/delete/SpaceDeletionOptions',
		'as/dto/project/delete/ProjectDeletionOptions',
		'as/dto/experiment/delete/ExperimentDeletionOptions',
		'as/dto/sample/delete/SampleDeletionOptions',
		'as/dto/dataset/delete/DataSetDeletionOptions',
		'as/dto/material/delete/MaterialDeletionOptions',
		'as/dto/entitytype/id/EntityTypePermId',
		'as/dto/space/id/SpacePermId',
		'as/dto/project/id/ProjectPermId',
		'as/dto/project/id/ProjectIdentifier',
		'as/dto/experiment/id/ExperimentPermId',
		'as/dto/experiment/id/ExperimentIdentifier',
		'as/dto/sample/id/SamplePermId',
		'as/dto/dataset/id/DataSetPermId',
		'as/dto/dataset/id/FileFormatTypePermId',
		'as/dto/material/id/MaterialPermId',
		'as/dto/tag/id/TagCode',
		'as/dto/space/search/SpaceSearchCriteria',
		'as/dto/project/search/ProjectSearchCriteria',
		'as/dto/experiment/search/ExperimentSearchCriteria',
		'as/dto/sample/search/SampleSearchCriteria',
		'as/dto/dataset/search/DataSetSearchCriteria',
		'as/dto/material/search/MaterialSearchCriteria',
		'as/dto/space/fetchoptions/SpaceFetchOptions',
		'as/dto/project/fetchoptions/ProjectFetchOptions',
		'as/dto/experiment/fetchoptions/ExperimentFetchOptions',
		'as/dto/sample/fetchoptions/SampleFetchOptions',
		'as/dto/dataset/fetchoptions/DataSetFetchOptions',
		'as/dto/material/fetchoptions/MaterialFetchOptions',
		'as/dto/deletion/fetchoptions/DeletionFetchOptions',
		'as/dto/deletion/search/DeletionSearchCriteria',
		'as/dto/service/search/CustomASServiceSearchCriteria',
		'as/dto/service/fetchoptions/CustomASServiceFetchOptions',
		'as/dto/service/id/CustomASServiceCode',
		'as/dto/service/CustomASServiceExecutionOptions',
		'as/dto/global/search/GlobalSearchCriteria',
		'as/dto/global/fetchoptions/GlobalSearchObjectFetchOptions',
		'as/dto/objectkindmodification/search/ObjectKindModificationSearchCriteria',
		'as/dto/objectkindmodification/fetchoptions/ObjectKindModificationFetchOptions' ], 
		function(
			SpaceCreation,
			ProjectCreation,
			ExperimentCreation,
			SampleCreation,
			MaterialCreation,
			AttachmentCreation,
			SpaceUpdate,
			ProjectUpdate,
			ExperimentUpdate,
			SampleUpdate,
			DataSetUpdate,
			PhysicalDataUpdate,
			MaterialUpdate,
			SpaceDeletionOptions,
			ProjectDeletionOptions,
			ExperimentDeletionOptions,
			SampleDeletionOptions,
			DataSetDeletionOptions,
			MaterialDeletionOptions,
			EntityTypePermId,
			SpacePermId,
			ProjectPermId,
			ProjectIdentifier,
			ExperimentPermId,
			ExperimentIdentifier,
			SamplePermId,
			DataSetPermId,
			FileFormatTypePermId,
			MaterialPermId,
			TagCode,
			SpaceSearchCriteria,
			ProjectSearchCriteria,
			ExperimentSearchCriteria,
			SampleSearchCriteria,
			DataSetSearchCriteria,
			MaterialSearchCriteria,
			SpaceFetchOptions,
			ProjectFetchOptions,
			ExperimentFetchOptions,
			SampleFetchOptions,
			DataSetFetchOptions,
			MaterialFetchOptions,
			DeletionFetchOptions,
			DeletionSearchCriteria,
			CustomASServiceSearchCriteria,
			CustomASServiceFetchOptions,
			CustomASServiceCode,
			CustomASServiceExecutionOptions,
			GlobalSearchCriteria,
			GlobalSearchObjectFetchOptions,
			ObjectKindModificationSearchCriteria,
			ObjectKindModificationFetchOptions) 
{

	var Dtos = function() {

		this.SpaceCreation = SpaceCreation;
		this.ProjectCreation = ProjectCreation;
		this.ExperimentCreation = ExperimentCreation;
		this.SampleCreation = SampleCreation;
		this.MaterialCreation = MaterialCreation;
		this.AttachmentCreation = AttachmentCreation;
		this.SpaceUpdate = SpaceUpdate;
		this.ProjectUpdate = ProjectUpdate;
		this.ExperimentUpdate = ExperimentUpdate;
		this.SampleUpdate = SampleUpdate;
		this.DataSetUpdate = DataSetUpdate;
		this.PhysicalDataUpdate = PhysicalDataUpdate;
		this.MaterialUpdate = MaterialUpdate;
		this.SpaceDeletionOptions = SpaceDeletionOptions;
		this.ProjectDeletionOptions = ProjectDeletionOptions;
		this.ExperimentDeletionOptions = ExperimentDeletionOptions;
		this.SampleDeletionOptions = SampleDeletionOptions;
		this.DataSetDeletionOptions = DataSetDeletionOptions;
		this.MaterialDeletionOptions = MaterialDeletionOptions;
		this.EntityTypePermId = EntityTypePermId;
		this.SpacePermId = SpacePermId;
		this.ProjectPermId = ProjectPermId;
		this.ProjectIdentifier = ProjectIdentifier;
		this.ExperimentPermId = ExperimentPermId;
		this.ExperimentIdentifier = ExperimentIdentifier;
		this.SamplePermId = SamplePermId;
		this.DataSetPermId = DataSetPermId;
		this.FileFormatTypePermId = FileFormatTypePermId;
		this.MaterialPermId = MaterialPermId;
		this.TagCode = TagCode;
		this.SpaceSearchCriteria = SpaceSearchCriteria;
		this.ProjectSearchCriteria = ProjectSearchCriteria;
		this.ExperimentSearchCriteria = ExperimentSearchCriteria;
		this.SampleSearchCriteria = SampleSearchCriteria;
		this.DataSetSearchCriteria = DataSetSearchCriteria;
		this.MaterialSearchCriteria = MaterialSearchCriteria;
		this.SpaceFetchOptions = SpaceFetchOptions;
		this.ProjectFetchOptions = ProjectFetchOptions;
		this.ExperimentFetchOptions = ExperimentFetchOptions;
		this.SampleFetchOptions = SampleFetchOptions;
		this.DataSetFetchOptions = DataSetFetchOptions;
		this.MaterialFetchOptions = MaterialFetchOptions;
		this.DeletionFetchOptions = DeletionFetchOptions;
		this.DeletionSearchCriteria = DeletionSearchCriteria;
		this.CustomASServiceSearchCriteria = CustomASServiceSearchCriteria;
		this.CustomASServiceFetchOptions = CustomASServiceFetchOptions;
		this.CustomASServiceCode = CustomASServiceCode;
		this.CustomASServiceExecutionOptions = CustomASServiceExecutionOptions;
		this.GlobalSearchCriteria = GlobalSearchCriteria;
		this.GlobalSearchObjectFetchOptions = GlobalSearchObjectFetchOptions;
		this.ObjectKindModificationSearchCriteria = ObjectKindModificationSearchCriteria;
		this.ObjectKindModificationFetchOptions = ObjectKindModificationFetchOptions;

	};
	return new Dtos();
})
