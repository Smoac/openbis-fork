import ToolSearchComponent from '@srcTest/js/components/tools/search/ToolSearchComponent.js'
import ToolSearchTestData from '@srcTest/js/components/tools/search/ToolSearchTestData.js'
import objectTypes from '@src/js/common/consts/objectType.js'
import openbis from '@srcTest/js/services/openbis.js'

let common = null

beforeEach(() => {
  common = new ToolSearchComponent()
  common.beforeEach()
})

describe(ToolSearchComponent.SUITE, () => {
  test('load with searchText (results found)', async () =>
    await testLoadWithSearchText(true))
  test('load with searchText (no results)', async () =>
    await testLoadWithSearchText(false))
  test('load with objectType (results found)', async () =>
    await testLoadWithObjectType(true))
  test('load with objectType (no results)', async () =>
    await testLoadWithObjectType(false))
})

async function testLoadWithSearchText(resultsFound) {
  const {
    testPlugin,
    testPlugin2,
    anotherPlugin,
    anotherPlugin2,
    testQuery,
    anotherQuery
  } = ToolSearchTestData

  const dynamicPropertyPlugins = new openbis.SearchResult()
  dynamicPropertyPlugins.setObjects(
    resultsFound ? [testPlugin, testPlugin2, anotherPlugin] : []
  )
  openbis.searchPlugins.mockReturnValueOnce(
    Promise.resolve(dynamicPropertyPlugins)
  )

  const entityValidationPlugins = new openbis.SearchResult()
  entityValidationPlugins.setObjects(
    resultsFound ? [testPlugin2, anotherPlugin, anotherPlugin2] : []
  )
  openbis.searchPlugins.mockReturnValueOnce(
    Promise.resolve(entityValidationPlugins)
  )

  openbis.mockSearchQueries(resultsFound ? [testQuery, anotherQuery] : [])

  const form = await common.mount({ searchText: 'test' })

  if (resultsFound) {
    form.expectJSON({
      messages: [],
      dynamicPropertyPlugins: {
        columns: [
          {
            name: 'name',
            label: 'Name'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'pluginKind',
            label: 'Plugin Kind'
          },
          {
            name: 'entityKind',
            label: 'Entity Kind'
          },
          {
            name: 'registrator',
            label: 'Registrator'
          }
        ],
        rows: [
          {
            values: {
              name: testPlugin.getName(),
              description: testPlugin.getDescription()
            }
          },
          {
            values: {
              name: testPlugin2.getName(),
              description: testPlugin2.getDescription()
            }
          }
        ]
      },
      entityValidationPlugins: {
        columns: [
          {
            name: 'name',
            label: 'Name'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'pluginKind',
            label: 'Plugin Kind'
          },
          {
            name: 'entityKind',
            label: 'Entity Kind'
          },
          {
            name: 'registrator',
            label: 'Registrator'
          }
        ],
        rows: [
          {
            values: {
              name: testPlugin2.getName(),
              description: testPlugin2.getDescription()
            }
          }
        ]
      },
      queries: {
        columns: [
          {
            name: 'name',
            label: 'Name'
          },
          {
            name: 'description',
            label: 'Description'
          },
          {
            name: 'database',
            label: 'Database'
          },
          {
            name: 'queryType',
            label: 'Query Type'
          },
          {
            name: 'entityTypeCodePattern',
            label: 'Entity Type Pattern'
          },
          {
            name: 'publicFlag',
            label: 'Public'
          },
          {
            name: 'registrator',
            label: 'Registrator'
          }
        ],
        rows: [
          {
            values: {
              name: testQuery.getName(),
              description: testQuery.getDescription()
            }
          }
        ]
      }
    })
  } else {
    form.expectJSON({
      messages: [
        {
          text: 'No results found.',
          type: 'info'
        }
      ],
      dynamicPropertyPlugins: null,
      entityValidationPlugins: null,
      queries: null
    })
  }
}

async function testLoadWithObjectType(resultsFound) {
  const { testPlugin, testPlugin2, anotherPlugin } = ToolSearchTestData

  openbis.mockSearchPlugins(
    resultsFound ? [testPlugin, testPlugin2, anotherPlugin] : []
  )

  const form = await common.mount({
    objectType: objectTypes.DYNAMIC_PROPERTY_PLUGIN
  })

  form.expectJSON({
    messages: [],
    dynamicPropertyPlugins: {
      columns: [
        {
          name: 'name',
          label: 'Name'
        },
        {
          name: 'description',
          label: 'Description'
        },
        {
          name: 'pluginKind',
          label: 'Plugin Kind'
        },
        {
          name: 'entityKind',
          label: 'Entity Kind'
        },
        {
          name: 'registrator',
          label: 'Registrator'
        }
      ],
      rows: resultsFound
        ? [
            {
              values: {
                name: anotherPlugin.getName(),
                description: anotherPlugin.getDescription()
              }
            },
            {
              values: {
                name: testPlugin.getName(),
                description: testPlugin.getDescription()
              }
            },
            {
              values: {
                name: testPlugin2.getName(),
                description: testPlugin2.getDescription()
              }
            }
          ]
        : []
    },
    entityValidationPlugins: null,
    queries: null
  })
}
