import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import withStyles from '@mui/styles/withStyles';
import Container from '@src/js/components/common/form/Container.jsx'
import AppController from '@src/js/components/AppController.js'
import GridContainer from '@src/js/components/common/grid/GridContainer.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import EntityTypesGrid from '@src/js/components/types/common/EntityTypesGrid.jsx'
import VocabularyTypesGrid from '@src/js/components/types/common/VocabularyTypesGrid.jsx'
import PropertyTypesGrid from '@src/js/components/types/common/PropertyTypesGrid.jsx'
import Message from '@src/js/components/common/form/Message.jsx'
import ids from '@src/js/common/consts/ids.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import util from '@src/js/common/util.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  grid: {
    marginBottom: theme.spacing(2)
  }
})

class TypeSearch extends React.Component {
  constructor(props) {
    super(props)
    autoBind(this)

    this.gridControllers = {}

    this.state = {
      loaded: false
    }
  }

  componentDidMount() {
    this.load()
  }

  async load() {
    try {
      await Promise.all([
        this.loadObjectTypes(),
        this.loadCollectionTypes(),
        this.loadDataSetTypes(),
        this.loadMaterialTypes(),
        this.loadVocabularyTypes(),
        this.loadPropertyTypes()
      ])
      this.setState(() => ({
        loaded: true
      }))
    } catch (error) {
      AppController.getInstance().errorChange(error)
    }
  }

  async loadObjectTypes() {
    if (!this.shouldLoad(objectTypes.OBJECT_TYPE)) {
      return
    }

    const fo = new openbis.SampleTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchSampleTypes(
      new openbis.SampleTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        exportableId: {
          exportable_kind: GridExportOptions.EXPORTABLE_KIND.SAMPLE_TYPE,
          perm_id: object.getPermId().getPermId()
        },
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        internal: _.get(object, 'managedInternally'),
        subcodeUnique: _.get(object, 'subcodeUnique', false),
        autoGeneratedCode: _.get(object, 'autoGeneratedCode', false),
        generatedCodePrefix: _.get(object, 'generatedCodePrefix'),
        validationPlugin: _.get(object, 'validationPlugin.name'),
        modificationDate: _.get(object, 'modificationDate')
      }))

    this.setState({
      objectTypes: types
    })
  }

  async loadCollectionTypes() {
    if (!this.shouldLoad(objectTypes.COLLECTION_TYPE)) {
      return
    }

    const fo = new openbis.ExperimentTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchExperimentTypes(
      new openbis.ExperimentTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        exportableId: {
          exportable_kind: GridExportOptions.EXPORTABLE_KIND.EXPERIMENT_TYPE,
          perm_id: object.getPermId().getPermId()
        },
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        internal: _.get(object, 'managedInternally'),
        validationPlugin: _.get(object, 'validationPlugin.name'),
        modificationDate: _.get(object, 'modificationDate')
      }))

    this.setState({
      collectionTypes: types
    })
  }

  async loadDataSetTypes() {
    if (!this.shouldLoad(objectTypes.DATA_SET_TYPE)) {
      return
    }

    const fo = new openbis.DataSetTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchDataSetTypes(
      new openbis.DataSetTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        exportableId: {
          exportable_kind: GridExportOptions.EXPORTABLE_KIND.DATASET_TYPE,
          perm_id: object.getPermId().getPermId()
        },
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        internal: _.get(object, 'managedInternally'),
        validationPlugin: _.get(object, 'validationPlugin.name'),
        mainDataSetPattern: _.get(object, 'mainDataSetPattern'),
        mainDataSetPath: _.get(object, 'mainDataSetPath'),
        disallowDeletion: _.get(object, 'disallowDeletion', false),
        modificationDate: _.get(object, 'modificationDate')
      }))

    this.setState({
      dataSetTypes: types
    })
  }

  async loadMaterialTypes() {
    if (!this.shouldLoad(objectTypes.MATERIAL_TYPE)) {
      return
    }

    const fo = new openbis.MaterialTypeFetchOptions()
    fo.withValidationPlugin()

    const result = await openbis.searchMaterialTypes(
      new openbis.MaterialTypeSearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: _.get(object, 'code'),
        code: _.get(object, 'code'),
        description: _.get(object, 'description'),
        internal: _.get(object, 'managedInternally'),
        validationPlugin: _.get(object, 'validationPlugin.name'),
        modificationDate: _.get(object, 'modificationDate')
      }))

    this.setState({
      materialTypes: types
    })
  }

  async loadVocabularyTypes() {
    if (!this.shouldLoad(objectTypes.VOCABULARY_TYPE)) {
      return
    }

    const fo = new openbis.VocabularyFetchOptions()
    fo.withRegistrator()

    const result = await openbis.searchVocabularies(
      new openbis.VocabularySearchCriteria(),
      fo
    )

    const types = util
      .filter(result.objects, this.props.searchText, ['code', 'description'])
      .map(object => ({
        id: object.code,
        exportableId: {
          exportable_kind: GridExportOptions.EXPORTABLE_KIND.VOCABULARY_TYPE,
          perm_id: object.getPermId().getPermId()
        },
        code: object.code,
        description: object.description,
        urlTemplate: object.urlTemplate,
        internal: _.get(object, 'managedInternally'),
        registrator: _.get(object, 'registrator.userId'),
        registrationDate: _.get(object, 'registrationDate'),
        modificationDate: _.get(object, 'modificationDate')
      }))

    this.setState({
      vocabularyTypes: types
    })
  }

  async loadPropertyTypes() {
    if (!this.shouldLoad(objectTypes.PROPERTY_TYPE)) {
      return
    }

    const [propertyTypes, propertyTypeUsages] = await Promise.all([
      this.loadPropertyTypesTypes(),
      this.loadPropertyTypesUsages()
    ])

    const types = util
      .filter(propertyTypes.objects, this.props.searchText, [
        'code',
        'description'
      ])
      .map(object => ({
        id: _.get(object, 'code'),
        code: _.get(object, 'code'),
        internal: _.get(object, 'managedInternally'),
        label: _.get(object, 'label'),
        description: _.get(object, 'description'),
        dataType: _.get(object, 'dataType'),
        vocabulary: _.get(object, 'vocabulary.code'),
        materialType: _.get(object, 'materialType.code'),
        sampleType: _.get(object, 'sampleType.code'),
        schema: _.get(object, 'schema'),
        transformation: _.get(object, 'transformation'),
        usages: _.get(propertyTypeUsages, object.code),
        registrator: _.get(object, 'registrator.userId'),
        registrationDate: _.get(object, 'registrationDate')
      }))

    this.setState({
      propertyTypes: types
    })
  }

  async loadPropertyTypesTypes() {
    const fo = new openbis.PropertyTypeFetchOptions()
    fo.withVocabulary()
    fo.withMaterialType()
    fo.withSampleType()
    fo.withRegistrator()

    const propertyTypes = await openbis.searchPropertyTypes(
      new openbis.PropertyTypeSearchCriteria(),
      fo
    )

    return propertyTypes
  }

  async loadPropertyTypesUsages() {
    const usages = {}

    const fo = new openbis.PropertyAssignmentFetchOptions()
    fo.withEntityType()
    fo.withPropertyType()

    const propertyAssignments = await openbis.searchPropertyAssignments(
      new openbis.PropertyAssignmentSearchCriteria(),
      fo
    )

    propertyAssignments.objects.forEach(propertyAssignment => {
      let propertyUsages = usages[propertyAssignment.propertyType.code]
      if (!propertyUsages) {
        propertyUsages = {
          sampleTypes: [],
          experimentTypes: [],
          dataSetTypes: [],
          materialTypes: []
        }
        usages[propertyAssignment.propertyType.code] = propertyUsages
      }

      const entityType = propertyAssignment.entityType['@type']

      if (entityType === 'as.dto.sample.SampleType') {
        propertyUsages.sampleTypes.push(propertyAssignment.entityType.code)
      } else if (entityType === 'as.dto.experiment.ExperimentType') {
        propertyUsages.experimentTypes.push(propertyAssignment.entityType.code)
      } else if (entityType === 'as.dto.dataset.DataSetType') {
        propertyUsages.dataSetTypes.push(propertyAssignment.entityType.code)
      } else if (entityType === 'as.dto.material.MaterialType') {
        propertyUsages.materialTypes.push(propertyAssignment.entityType.code)
      }
    })

    Object.keys(usages).forEach(propertyTypeCode => {
      const propertyUsages = usages[propertyTypeCode]
      propertyUsages.sampleTypes.sort()
      propertyUsages.experimentTypes.sort()
      propertyUsages.dataSetTypes.sort()
      propertyUsages.materialTypes.sort()
      propertyUsages.count =
        propertyUsages.sampleTypes.length +
        propertyUsages.experimentTypes.length +
        propertyUsages.dataSetTypes.length +
        propertyUsages.materialTypes.length
    })

    return usages
  }

  shouldLoad(objectType) {
    return this.props.objectType === objectType || !this.props.objectType
  }

  handleContainerClick() {
    for (let gridObjectType in this.gridControllers) {
      this.gridControllers[gridObjectType].selectRow(null)
    }
  }

  handleSelectedRowChange(objectType) {
    return row => {
      if (!row) {
        return
      }
      for (let gridObjectType in this.gridControllers) {
        if (gridObjectType !== objectType) {
          this.gridControllers[gridObjectType].selectRow(null)
        }
      }
    }
  }

  render() {
    logger.log(logger.DEBUG, 'TypeSearch.render')

    if (!this.state.loaded) {
      return null
    }

    return (
      <GridContainer onClick={this.handleContainerClick}>
        {this.renderNoResultsFoundMessage()}
        {this.renderObjectTypes()}
        {this.renderCollectionTypes()}
        {this.renderDataSetTypes()}
        {this.renderMaterialTypes()}
        {this.renderVocabularyTypes()}
        {this.renderPropertyTypes()}
      </GridContainer>
    )
  }

  renderNoResultsFoundMessage() {
    const { objectType } = this.props
    const {
      objectTypes = [],
      collectionTypes = [],
      dataSetTypes = [],
      materialTypes = [],
      vocabularyTypes = [],
      propertyTypes = []
    } = this.state

    if (
      !objectType &&
      objectTypes.length === 0 &&
      collectionTypes.length === 0 &&
      dataSetTypes.length === 0 &&
      materialTypes.length === 0 &&
      vocabularyTypes.length === 0 &&
      propertyTypes.length === 0
    ) {
      return (
        <Container>
          <Message type='info'>
            {messages.get(messages.NO_RESULTS_FOUND)}
          </Message>
        </Container>
      )
    } else {
      return null
    }
  }

  renderObjectTypes() {
    if (this.shouldRender(objectTypes.OBJECT_TYPE, this.state.objectTypes)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <EntityTypesGrid
            id={ids.OBJECT_TYPES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.OBJECT_TYPE] = controller)
            }
            kind={openbis.EntityKind.SAMPLE}
            rows={this.state.objectTypes}
            exportable={{
              fileFormat: GridExportOptions.FILE_FORMAT.XLS,
              filePrefix: 'object-types',
              fileContent: GridExportOptions.FILE_CONTENT.TYPES
            }}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.OBJECT_TYPE
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderCollectionTypes() {
    if (
      this.shouldRender(objectTypes.COLLECTION_TYPE, this.state.collectionTypes)
    ) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <EntityTypesGrid
            id={ids.COLLECTION_TYPES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.COLLECTION_TYPE] = controller)
            }
            kind={openbis.EntityKind.EXPERIMENT}
            rows={this.state.collectionTypes}
            exportable={{
              fileFormat: GridExportOptions.FILE_FORMAT.XLS,
              filePrefix: 'collection-types',
              fileContent: GridExportOptions.FILE_CONTENT.TYPES
            }}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.COLLECTION_TYPE
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderDataSetTypes() {
    if (this.shouldRender(objectTypes.DATA_SET_TYPE, this.state.dataSetTypes)) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <EntityTypesGrid
            id={ids.DATA_SET_TYPES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.DATA_SET_TYPE] = controller)
            }
            kind={openbis.EntityKind.DATA_SET}
            rows={this.state.dataSetTypes}
            exportable={{
              fileFormat: GridExportOptions.FILE_FORMAT.XLS,
              filePrefix: 'data-set-types',
              fileContent: GridExportOptions.FILE_CONTENT.TYPES
            }}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.DATA_SET_TYPE
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderMaterialTypes() {
    if (
      this.shouldRender(objectTypes.MATERIAL_TYPE, this.state.materialTypes)
    ) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <EntityTypesGrid
            id={ids.MATERIAL_TYPES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.MATERIAL_TYPE] = controller)
            }
            kind={openbis.EntityKind.MATERIAL}
            rows={this.state.materialTypes}
            exportable={{
              fileFormat: GridExportOptions.FILE_FORMAT.TSV,
              filePrefix: 'material-types'
            }}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.MATERIAL_TYPE
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderVocabularyTypes() {
    if (
      this.shouldRender(objectTypes.VOCABULARY_TYPE, this.state.vocabularyTypes)
    ) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <VocabularyTypesGrid
            id={ids.VOCABULARY_TYPES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.VOCABULARY_TYPE] = controller)
            }
            rows={this.state.vocabularyTypes}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.VOCABULARY_TYPE
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  renderPropertyTypes() {
    if (
      this.shouldRender(objectTypes.PROPERTY_TYPE, this.state.propertyTypes)
    ) {
      const { classes } = this.props
      return (
        <div className={classes.grid}>
          <PropertyTypesGrid
            id={ids.PROPERTY_TYPES_GRID_ID}
            controllerRef={controller =>
              (this.gridControllers[objectTypes.PROPERTY_TYPE] = controller)
            }
            rows={this.state.propertyTypes}
            onSelectedRowChange={this.handleSelectedRowChange(
              objectTypes.PROPERTY_TYPE
            )}
          />
        </div>
      )
    } else {
      return null
    }
  }

  shouldRender(objectType, types) {
    return this.props.objectType === objectType || (types && types.length > 0)
  }
}

export default withStyles(styles)(TypeSearch)
