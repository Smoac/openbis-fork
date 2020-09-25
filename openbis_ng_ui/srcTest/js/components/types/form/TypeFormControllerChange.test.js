import TypeFormControllerTest from '@srcTest/js/components/types/form/TypeFormControllerTest.js'
import TypeFormSelectionType from '@src/js/components/types/form/TypeFormSelectionType.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import fixture from '@srcTest/js/common/fixture.js'

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
  test('change type', testChangeType)
  test('change section', testChangeSection)
  test('change property', testChangeProperty)
})

async function testChangeType() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadUsages.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    type: {
      description: { value: 'TEST_DESCRIPTION' }
    }
  })

  common.controller.handleChange(TypeFormSelectionType.TYPE, {
    field: 'description',
    value: 'NEW_DESCRIPTION'
  })

  expect(common.context.getState()).toMatchObject({
    type: {
      description: { value: 'NEW_DESCRIPTION' }
    }
  })
}

async function testChangeSection() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadUsages.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })

  common.controller.handleChange(TypeFormSelectionType.SECTION, {
    id: 'section-1',
    field: 'name',
    value: 'TEST_NAME'
  })

  expect(common.context.getState()).toMatchObject({
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_NAME' },
        properties: ['property-1', 'property-2']
      }
    ]
  })
}

async function testChangeProperty() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadUsages.mockReturnValue(Promise.resolve({}))

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        description: { value: null }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ]
  })

  common.controller.handleChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-1',
    field: 'description',
    value: 'TEST_DESCRIPTION'
  })

  expect(common.context.getState()).toMatchObject({
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        description: { value: 'TEST_DESCRIPTION' }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ]
  })
}
