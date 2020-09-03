import openbis from '@src/js/services/openbis.js'

export default class UserFormFacade {
  async loadUser(userId) {
    const id = new openbis.PersonPermId(userId)
    const fo = new openbis.PersonFetchOptions()
    fo.withSpace()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject()
    return openbis.getPersons([id], fo).then(map => {
      return map[userId]
    })
  }

  async loadUserGroups(userId) {
    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withUsers()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject()
    return openbis.searchAuthorizationGroups(criteria, fo).then(result => {
      return result.getObjects().filter(group => {
        return group.getUsers().some(user => {
          return user.userId === userId
        })
      })
    })
  }

  async loadGroups() {
    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    const fo = new openbis.AuthorizationGroupFetchOptions()
    fo.withRoleAssignments().withSpace()
    fo.withRoleAssignments().withProject()
    return openbis.searchAuthorizationGroups(criteria, fo).then(result => {
      return result.getObjects()
    })
  }

  async loadSpaces() {
    const criteria = new openbis.SpaceSearchCriteria()
    const fo = new openbis.SpaceFetchOptions()
    return openbis.searchSpaces(criteria, fo).then(result => {
      return result.getObjects()
    })
  }

  async executeOperations(operations, options) {
    return openbis.executeOperations(operations, options)
  }
}
