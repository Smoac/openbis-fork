import TypeFormControllerTest from '@srcTest/js/components/types/form/TypeFormControllerTest.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new TypeFormControllerTest()
  common.beforeEach()
  common.init({
    id: 'TEST_OBJECT_ID',
    type: objectTypes.OBJECT_TYPE
  })
})

afterEach(() => {
  common.afterEach()
})

describe(TypeFormControllerTest.SUITE, () => {
  test('save add local property', testSaveAddLocalProperty)
  test('save add global property', testSaveAddGlobalProperty)
  test(
    'save update local property assignment',
    testSaveUpdateLocalPropertyAssignment
  )
  test(
    'save update global property assignment',
    testSaveUpdateGlobalPropertyAssignment
  )
  test(
    'save update local property type if possible',
    testSaveUpdateLocalPropertyTypeIfPossible
  )
  test(
    'save update global property type if possible',
    testSaveUpdateGlobalPropertyTypeIfPossible
  )
  test('save delete local property', testSaveDeleteLocalProperty)
  test('save delete global property', testSaveDeleteGlobalProperty)
  test(
    'save delete local property last assignment',
    testSaveDeleteLocalPropertyLastAssignment
  )
  test(
    'save delete global property last assignment',
    testSaveDeleteGlobalPropertyLastAssignment
  )
})

async function testSaveAddLocalProperty() {
  await doTestAddProperty('local')
}

async function testSaveAddGlobalProperty() {
  await doTestAddProperty('global')
}

async function testSaveUpdateLocalPropertyAssignment() {
  await doTestUpdatePropertyAssignment(
    SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
    LOCAL_PROPERTY_TYPE
  )
}

async function testSaveUpdateGlobalPropertyAssignment() {
  await doTestUpdatePropertyAssignment(
    SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
    GLOBAL_PROPERTY_TYPE
  )
}

async function testSaveUpdateLocalPropertyTypeIfPossible() {
  await doTestUpdatePropertyTypeIfPossible(
    SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
    LOCAL_PROPERTY_TYPE,
    LOCAL_PROPERTY_ASSIGNMENT
  )
}

async function testSaveUpdateGlobalPropertyTypeIfPossible() {
  await doTestUpdatePropertyTypeIfPossible(
    SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
    GLOBAL_PROPERTY_TYPE,
    GLOBAL_PROPERTY_ASSIGNMENT
  )
}

async function testSaveDeleteLocalProperty() {
  await doTestDeleteProperty(
    SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
    LOCAL_PROPERTY_TYPE
  )
}

async function testSaveDeleteGlobalProperty() {
  await doTestDeleteProperty(
    SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
    GLOBAL_PROPERTY_TYPE
  )
}

async function testSaveDeleteLocalPropertyLastAssignment() {
  await doTestDeletePropertyLastAssignment(
    SAMPLE_TYPE_WITH_LOCAL_PROPERTY,
    LOCAL_PROPERTY_TYPE
  )
}

async function testSaveDeleteGlobalPropertyLastAssignment() {
  await doTestDeletePropertyLastAssignment(
    SAMPLE_TYPE_WITH_GLOBAL_PROPERTY,
    GLOBAL_PROPERTY_TYPE
  )
}

async function doTestAddProperty(scope) {
  const SAMPLE_TYPE = new openbis.SampleType()
  SAMPLE_TYPE.setCode('TEST_TYPE')
  SAMPLE_TYPE.setGeneratedCodePrefix('TEST_PREFIX')

  common.facade.loadType.mockReturnValue(Promise.resolve(SAMPLE_TYPE))
  common.facade.loadGlobalPropertyTypes.mockReturnValue(Promise.resolve([]))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleAddSection()
  common.controller.handleAddProperty()

  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'scope',
    value: scope
  })
  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'code',
    value: 'NEW_CODE'
  })
  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'dataType',
    value: 'VARCHAR'
  })
  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'label',
    value: 'NEW_LABEL'
  })
  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'description',
    value: 'NEW_DESCRIPTION'
  })

  await common.controller.handleSave()

  const propertyTypeCode = scope === 'local' ? 'TEST_TYPE.NEW_CODE' : 'NEW_CODE'

  expectExecuteOperations([
    createPropertyTypeOperation({
      code: propertyTypeCode,
      dataType: openbis.DataType.VARCHAR,
      label: 'NEW_LABEL'
    }),
    setPropertyAssignmentOperation(
      SAMPLE_TYPE.getCode(),
      propertyTypeCode,
      false
    )
  ])
}

async function doTestUpdatePropertyAssignment(type, propertyType) {
  common.facade.loadType.mockReturnValue(Promise.resolve(type))
  common.facade.loadTypeUsages.mockReturnValue(Promise.resolve(0))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'mandatory',
    value: true
  })

  await common.controller.handleSave()

  expectExecuteOperations([
    setPropertyAssignmentOperation(type.getCode(), propertyType.getCode(), true)
  ])
}

async function doTestUpdatePropertyTypeIfPossible(
  type,
  propertyType,
  propertyAssignment
) {
  common.facade.loadType.mockReturnValue(Promise.resolve(type))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0',
    field: 'label',
    value: 'Updated label'
  })

  await common.controller.handleSave()

  expectExecuteOperations([
    updatePropertyTypeOperation(propertyType.getCode(), 'Updated label'),
    setPropertyAssignmentOperation(
      type.getCode(),
      propertyType.getCode(),
      propertyAssignment.isMandatory()
    )
  ])
}

async function doTestDeleteProperty(type, propertyType) {
  common.facade.loadType.mockReturnValue(Promise.resolve(type))
  common.facade.loadPropertyUsages.mockReturnValue(Promise.resolve({}))
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0'
  })
  common.controller.handleRemove()
  common.controller.handleRemoveConfirm()

  await common.controller.handleSave()

  expectExecuteOperations([
    deletePropertyAssignmentOperation(type.getCode(), propertyType.getCode()),
    setPropertyAssignmentOperation(type.getCode())
  ])
}

async function doTestDeletePropertyLastAssignment(type, propertyType) {
  common.facade.loadType.mockReturnValue(Promise.resolve(type))
  common.facade.loadPropertyUsages.mockReturnValue(Promise.resolve({}))
  common.facade.loadAssignments.mockReturnValue(
    Promise.resolve({
      [propertyType.getCode()]: 1
    })
  )
  common.facade.executeOperations.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  common.controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0'
  })
  common.controller.handleRemove()
  common.controller.handleRemoveConfirm()

  await common.controller.handleSave()

  expectExecuteOperations([
    deletePropertyAssignmentOperation(type.getCode(), propertyType.getCode()),
    deletePropertyTypeOperation(propertyType.getCode()),
    setPropertyAssignmentOperation(type.getCode())
  ])
}

function createPropertyTypeOperation({
  code: propertyTypeCode,
  dataType: propertyDataType,
  vocabulary: propertyTypeVocabulary,
  label: propertyTypeLabel
}) {
  const creation = new openbis.PropertyTypeCreation()
  creation.setCode(propertyTypeCode)
  creation.setDataType(propertyDataType)
  if (propertyTypeVocabulary) {
    creation.setVocabularyId(
      new openbis.VocabularyPermId(propertyTypeVocabulary)
    )
  }
  creation.setLabel(propertyTypeLabel)
  return new openbis.CreatePropertyTypesOperation([creation])
}

function updatePropertyTypeOperation(propertyTypeCode, propertyTypeLabel) {
  const update = new openbis.PropertyTypeUpdate()
  update.setTypeId(new openbis.PropertyTypePermId(propertyTypeCode))
  update.setLabel(propertyTypeLabel)
  return new openbis.UpdatePropertyTypesOperation([update])
}

function deletePropertyTypeOperation(propertyTypeCode) {
  const id = new openbis.PropertyTypePermId(propertyTypeCode)
  const options = new openbis.PropertyTypeDeletionOptions()
  options.setReason('deleted via ng_ui')
  return new openbis.DeletePropertyTypesOperation([id], options)
}

function setPropertyAssignmentOperation(
  typeCode,
  propertyCode,
  propertyMandatory
) {
  const assignments = []
  if (propertyCode) {
    let assignment = new openbis.PropertyAssignmentCreation()
    assignment.setPropertyTypeId(new openbis.PropertyTypePermId(propertyCode))
    assignment.setMandatory(propertyMandatory)
    assignments.push(assignment)
  }

  const update = new openbis.SampleTypeUpdate()
  update.setTypeId(
    new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
  )
  update.getPropertyAssignments().set(assignments)

  return new openbis.UpdateSampleTypesOperation([update])
}

function deletePropertyAssignmentOperation(typeCode, propertyCode) {
  const assignmentId = new openbis.PropertyAssignmentPermId(
    new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE),
    new openbis.PropertyTypePermId(propertyCode)
  )

  const update = new openbis.SampleTypeUpdate()
  update.setTypeId(
    new openbis.EntityTypePermId(typeCode, openbis.EntityKind.SAMPLE)
  )
  update.getPropertyAssignments().remove([assignmentId])
  update.getPropertyAssignments().setForceRemovingAssignments(true)

  return new openbis.UpdateSampleTypesOperation([update])
}

function expectExecuteOperations(expectedOperations) {
  expect(common.facade.executeOperations).toHaveBeenCalledTimes(1)
  const actualOperations = common.facade.executeOperations.mock.calls[0][0]
  expect(actualOperations.length).toEqual(expectedOperations.length)
  actualOperations.forEach((actualOperation, index) => {
    expect(actualOperation).toMatchObject(expectedOperations[index])
  })
}

const LOCAL_PROPERTY_TYPE = new openbis.PropertyType()
LOCAL_PROPERTY_TYPE.setCode('TEST_TYPE.TEST_PROPERTY_TYPE')
LOCAL_PROPERTY_TYPE.setLabel('TEST_LABEL')
LOCAL_PROPERTY_TYPE.setDescription('TEST_DESCRIPTION')
LOCAL_PROPERTY_TYPE.setDataType(openbis.DataType.INTEGER)

const LOCAL_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
LOCAL_PROPERTY_ASSIGNMENT.setPropertyType(LOCAL_PROPERTY_TYPE)

const SAMPLE_TYPE_WITH_LOCAL_PROPERTY = new openbis.SampleType()
SAMPLE_TYPE_WITH_LOCAL_PROPERTY.setCode('TEST_TYPE')
SAMPLE_TYPE_WITH_LOCAL_PROPERTY.setGeneratedCodePrefix('TEST_PREFIX')
SAMPLE_TYPE_WITH_LOCAL_PROPERTY.setPropertyAssignments([
  LOCAL_PROPERTY_ASSIGNMENT
])

const GLOBAL_PROPERTY_TYPE = new openbis.PropertyType()
GLOBAL_PROPERTY_TYPE.setCode('TEST_PROPERTY_TYPE')
GLOBAL_PROPERTY_TYPE.setLabel('TEST_LABEL')
GLOBAL_PROPERTY_TYPE.setDescription('TEST_DESCRIPTION')
GLOBAL_PROPERTY_TYPE.setDataType(openbis.DataType.INTEGER)

const GLOBAL_PROPERTY_ASSIGNMENT = new openbis.PropertyAssignment()
GLOBAL_PROPERTY_ASSIGNMENT.setPropertyType(GLOBAL_PROPERTY_TYPE)

const SAMPLE_TYPE_WITH_GLOBAL_PROPERTY = new openbis.SampleType()
SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.setCode('TEST_TYPE')
SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.setGeneratedCodePrefix('TEST_PREFIX')
SAMPLE_TYPE_WITH_GLOBAL_PROPERTY.setPropertyAssignments([
  GLOBAL_PROPERTY_ASSIGNMENT
])
