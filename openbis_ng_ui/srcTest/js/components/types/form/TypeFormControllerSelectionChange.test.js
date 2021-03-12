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
  test('select a section', testSelectSection)
  test('select a property', testSelectProperty)
})

async function testSelectSection() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })

  common.controller.handleSelectionChange(TypeFormSelectionType.SECTION, {
    id: 'section-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    }
  })

  common.controller.handleSelectionChange()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })
}

async function testSelectProperty() {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )

  await common.controller.load()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })

  common.controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-0'
      }
    }
  })

  common.controller.handleSelectionChange()

  expect(common.context.getState()).toMatchObject({
    selection: null
  })
}
