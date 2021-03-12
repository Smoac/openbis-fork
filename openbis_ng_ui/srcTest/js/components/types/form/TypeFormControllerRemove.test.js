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
  test('remove section confirmed', async () => {
    await testRemoveSectionConfirmed(0)
    await testRemoveSectionConfirmed(10)
  })
  test('remove section cancelled', async () => {
    await testRemoveSectionCancelled(0)
    await testRemoveSectionCancelled(10)
  })
  test('remove property confirmed', async () => {
    await testRemovePropertyConfirmed(0)
    await testRemovePropertyConfirmed(10)
  })
  test('remove property cancelled', async () => {
    await testRemovePropertyCancelled(0)
    await testRemovePropertyCancelled(10)
  })
})

async function testRemoveSectionConfirmed(usages) {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadPropertyUsages.mockReturnValue(
    Promise.resolve({
      [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: usages
    })
  )

  await common.controller.load()
  common.controller.handleSelectionChange(TypeFormSelectionType.SECTION, {
    id: 'section-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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

  await common.controller.handleRemove()

  expect(common.context.getState()).toMatchObject({
    removeSectionDialogOpen: true,
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0'],
        usages: usages
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })

  common.controller.handleRemoveConfirm()

  expect(common.context.getState()).toMatchObject({
    removeSectionDialogOpen: false,
    selection: null,
    properties: [
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })
}

async function testRemoveSectionCancelled(usages) {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadPropertyUsages.mockReturnValue(
    Promise.resolve({
      [fixture.TEST_PROPERTY_TYPE_1_DTO.getCode()]: usages
    })
  )

  await common.controller.load()
  common.controller.handleSelectionChange(TypeFormSelectionType.SECTION, {
    id: 'section-0'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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

  await common.controller.handleRemove()

  expect(common.context.getState()).toMatchObject({
    removeSectionDialogOpen: true,
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0'],
        usages: usages
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })

  common.controller.handleRemoveCancel()

  expect(common.context.getState()).toMatchObject({
    removeSectionDialogOpen: false,
    selection: {
      type: TypeFormSelectionType.SECTION,
      params: {
        id: 'section-0'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0'],
        usages: usages
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-1', 'property-2']
      }
    ]
  })
}

async function testRemovePropertyConfirmed(usages) {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadPropertyUsages.mockReturnValue(
    Promise.resolve({
      [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: usages
    })
  )

  await common.controller.load()
  common.controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-1'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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

  await common.controller.handleRemove()

  expect(common.context.getState()).toMatchObject({
    removePropertyDialogOpen: true,
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        usages: usages
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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

  common.controller.handleRemoveConfirm()

  expect(common.context.getState()).toMatchObject({
    removePropertyDialogOpen: false,
    selection: null,
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
    sections: [
      {
        id: 'section-0',
        name: { value: 'TEST_SECTION_1' },
        properties: ['property-0']
      },
      {
        id: 'section-1',
        name: { value: 'TEST_SECTION_2' },
        properties: ['property-2']
      }
    ]
  })
}

async function testRemovePropertyCancelled(usages) {
  common.facade.loadType.mockReturnValue(
    Promise.resolve(fixture.TEST_SAMPLE_TYPE_DTO)
  )
  common.facade.loadPropertyUsages.mockReturnValue(
    Promise.resolve({
      [fixture.TEST_PROPERTY_TYPE_2_DTO.getCode()]: usages
    })
  )

  await common.controller.load()
  common.controller.handleSelectionChange(TypeFormSelectionType.PROPERTY, {
    id: 'property-1'
  })

  expect(common.context.getState()).toMatchObject({
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() }
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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

  await common.controller.handleRemove()

  expect(common.context.getState()).toMatchObject({
    removePropertyDialogOpen: true,
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        usages: usages
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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

  common.controller.handleRemoveCancel()

  expect(common.context.getState()).toMatchObject({
    removePropertyDialogOpen: false,
    selection: {
      type: TypeFormSelectionType.PROPERTY,
      params: {
        id: 'property-1'
      }
    },
    properties: [
      {
        id: 'property-0',
        code: { value: fixture.TEST_PROPERTY_TYPE_1_DTO.getCode() }
      },
      {
        id: 'property-1',
        code: { value: fixture.TEST_PROPERTY_TYPE_2_DTO.getCode() },
        usages: usages
      },
      {
        id: 'property-2',
        code: { value: fixture.TEST_PROPERTY_TYPE_3_DTO.getCode() }
      }
    ],
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
}
