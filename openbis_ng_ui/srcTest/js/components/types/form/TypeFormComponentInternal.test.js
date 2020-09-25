import TypeFormComponentTest from '@srcTest/js/components/types/form/TypeFormComponentTest.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new TypeFormComponentTest()
  common.beforeEach()
})

describe(TypeFormComponentTest.SUITE, () => {
  test('internal', testInternal)
})

async function testInternal() {
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(true, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.SYSTEM_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.TEST_USER_DTO)
  await doTestInternal(true, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
  await doTestInternal(false, fixture.TEST_USER_DTO, fixture.SYSTEM_USER_DTO)
}

async function doTestInternal(
  propertyTypeInternal,
  propertyTypeRegistrator,
  propertyAssignmentRegistrator
) {
  const isSystemInternalPropertyType =
    propertyTypeInternal &&
    propertyTypeRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const isSystemInternalPropertyAssignment =
    propertyTypeInternal &&
    propertyAssignmentRegistrator.userId === fixture.SYSTEM_USER_DTO.userId

  const propertyType = new openbis.PropertyType()
  propertyType.setCode('TEST_PROPERTY')
  propertyType.setManagedInternally(propertyTypeInternal)
  propertyType.setRegistrator(propertyTypeRegistrator)
  propertyType.setDataType(openbis.DataType.VARCHAR)

  const propertyAssignment = new openbis.PropertyAssignment()
  propertyAssignment.setPropertyType(propertyType)
  propertyAssignment.setPlugin(fixture.TEST_PLUGIN_DTO)
  propertyAssignment.setRegistrator(propertyAssignmentRegistrator)

  const type = new openbis.SampleType()
  type.setCode('TEST_TYPE')
  type.setPropertyAssignments([propertyAssignment])

  common.facade.loadType.mockReturnValue(Promise.resolve(type))
  common.facade.loadDynamicPlugins.mockReturnValue(
    Promise.resolve([propertyAssignment.plugin])
  )

  const form = await common.mount({
    id: type.getCode(),
    type: objectTypes.OBJECT_TYPE
  })

  form.getButtons().getEdit().click()
  await form.update()

  form.expectJSON({
    parameters: {
      type: {
        title: 'Type',
        code: {
          value: type.getCode(),
          enabled: false
        },
        description: {
          value: type.getDescription(),
          enabled: true
        }
      }
    }
  })

  form.getPreview().getSections()[0].getProperties()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      property: {
        title: 'Property',
        code: {
          value: propertyType.getCode(),
          enabled: false
        },
        dataType: {
          value: propertyType.getDataType(),
          enabled: !isSystemInternalPropertyType
        },
        label: {
          value: propertyType.getLabel(),
          enabled: !isSystemInternalPropertyType
        },
        description: {
          value: propertyType.getDescription(),
          enabled: !isSystemInternalPropertyType
        },
        plugin: {
          value: propertyAssignment.plugin.getName(),
          enabled: !isSystemInternalPropertyAssignment
        },
        mandatory: {
          value: propertyAssignment.isMandatory(),
          enabled: !isSystemInternalPropertyAssignment
        },
        visible: {
          value: propertyAssignment.isShowInEditView(),
          enabled: !isSystemInternalPropertyAssignment
        }
      }
    },
    buttons: {
      remove: {
        enabled: !isSystemInternalPropertyAssignment
      }
    }
  })

  form.getPreview().getSections()[0].click()
  await form.update()

  form.expectJSON({
    parameters: {
      section: {
        title: 'Section',
        name: {
          value: propertyAssignment.getSection(),
          enabled: true
        }
      }
    },
    buttons: {
      remove: {
        enabled: !isSystemInternalPropertyAssignment
      }
    }
  })
}
