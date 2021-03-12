import _ from 'lodash'
import RoleSelectionType from '@src/js/components/users/form/common/RoleSelectionType.js'
import FormUtil from '@src/js/components/common/form/FormUtil.js'

export default class RoleControllerAdd {
  constructor(controller) {
    this.controller = controller
    this.context = controller.context
  }

  async execute() {
    let { roles } = this.context.getState()

    const newRole = {
      id: _.uniqueId('role-'),
      inheritedFrom: FormUtil.createField({
        value: false
      }),
      level: FormUtil.createField({}),
      space: FormUtil.createField({
        visible: false
      }),
      project: FormUtil.createField({
        visible: false
      }),
      role: FormUtil.createField({
        visible: false
      }),
      original: null
    }

    const newRoles = Array.from(roles)
    newRoles.push(newRole)

    await this.context.setState(state => ({
      ...state,
      roles: newRoles,
      selection: {
        type: RoleSelectionType.ROLE,
        params: {
          id: newRole.id,
          part: 'level'
        }
      }
    }))

    await this.controller.changed(true)

    if (this.controller.rolesGridController) {
      await this.controller.rolesGridController.showSelectedRow()
    }
  }
}
