export default class RoleControllerRemove {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  execute(roleId) {
    const { roles } = this.context.getState()

    const roleIndex = roles.findIndex(role => role.id === roleId)

    const newRoles = Array.from(roles)
    newRoles.splice(roleIndex, 1)

    this.context.setState(state => ({
      ...state,
      roles: newRoles,
      selection: null
    }))

    this.controller.changed(true)
  }
}
