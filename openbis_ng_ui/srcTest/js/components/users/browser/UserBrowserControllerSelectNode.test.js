import UserBrowserControllerTest from '@srcTest/js/components/users/browser/UserBrowserControllerTest.js'
import openbis from '@srcTest/js/services/openbis.js'
import fixture from '@srcTest/js/common/fixture.js'

let common = null

beforeEach(() => {
  common = new UserBrowserControllerTest()
  common.beforeEach()
})

describe(UserBrowserControllerTest.SUITE, () => {
  test('select node', testSelectNode)
  test('select another node', testSelectAnotherNode)
  test('select virtual node', testSelectVirtualNode)
})

async function testSelectNode() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([
    fixture.TEST_USER_GROUP_DTO,
    fixture.ANOTHER_USER_GROUP_DTO,
    fixture.ALL_USERS_GROUP_DTO
  ])

  await common.controller.load()

  common.controller.nodeSelect('users/' + fixture.TEST_USER_DTO.userId)

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Users',
      expanded: false,
      selected: false,
      children: [
        {
          text: fixture.ANOTHER_USER_DTO.userId,
          expanded: false,
          selected: false
        },
        {
          text: fixture.TEST_USER_DTO.userId,
          expanded: false,
          selected: true
        }
      ]
    },
    {
      text: 'Groups',
      expanded: false,
      selected: false,
      children: [
        {
          text: fixture.ALL_USERS_GROUP_DTO.code,
          expanded: false,
          selected: false
        },
        {
          text: fixture.ANOTHER_USER_GROUP_DTO.code,
          expanded: false,
          selected: false
        },
        {
          text: fixture.TEST_USER_GROUP_DTO.code,
          expanded: false,
          selected: false
        }
      ]
    }
  ])
  common.expectOpenUserAction(fixture.TEST_USER_DTO.userId)

  common.controller.nodeSelect('groups/' + fixture.ANOTHER_USER_GROUP_DTO.code)

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Users',
      expanded: false,
      selected: false,
      children: [
        {
          text: fixture.ANOTHER_USER_DTO.userId,
          expanded: false,
          selected: false
        },
        {
          text: fixture.TEST_USER_DTO.userId,
          expanded: false,
          selected: false
        }
      ]
    },
    {
      text: 'Groups',
      expanded: false,
      selected: false,
      children: [
        {
          text: fixture.ALL_USERS_GROUP_DTO.code,
          expanded: false,
          selected: false
        },
        {
          text: fixture.ANOTHER_USER_GROUP_DTO.code,
          expanded: false,
          selected: true
        },
        {
          text: fixture.TEST_USER_GROUP_DTO.code,
          expanded: false,
          selected: false
        }
      ]
    }
  ])
  common.expectOpenGroupAction(fixture.ANOTHER_USER_GROUP_DTO.code)
}

async function testSelectAnotherNode() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([])

  await common.controller.load()
  common.controller.nodeSelect('users/' + fixture.TEST_USER_DTO.userId)
  common.controller.nodeSelect('users/' + fixture.ANOTHER_USER_DTO.userId)

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Users',
      expanded: false,
      selected: false,
      children: [
        {
          text: fixture.ANOTHER_USER_DTO.userId,
          expanded: false,
          selected: true
        },
        {
          text: fixture.TEST_USER_DTO.userId,
          expanded: false,
          selected: false
        }
      ]
    },
    {
      text: 'Groups',
      expanded: false,
      selected: false,
      children: []
    }
  ])

  common.expectOpenUserAction(fixture.TEST_USER_DTO.userId)
  common.expectOpenUserAction(fixture.ANOTHER_USER_DTO.userId)
}

async function testSelectVirtualNode() {
  openbis.mockSearchPersons([fixture.TEST_USER_DTO, fixture.ANOTHER_USER_DTO])
  openbis.mockSearchGroups([])

  await common.controller.load()
  common.controller.nodeSelect('users')

  expect(common.controller.getNodes()).toMatchObject([
    {
      text: 'Users',
      expanded: false,
      selected: true
    },
    {
      text: 'Groups',
      expanded: false,
      selected: false
    }
  ])

  common.expectOpenUsersOverviewAction()
}
