import React from 'react'
import Grid from '@src/js/components/common/grid/Grid.jsx'
import UserGroupLink from '@src/js/components/common/link/UserGroupLink.jsx'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

export default class GroupsGrid extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GroupsGrid.render')

    const {
      id,
      rows,
      selectedRowId,
      onSelectedRowChange,
      controllerRef
    } = this.props

    return (
      <Grid
        id={id}
        controllerRef={controllerRef}
        header={messages.get(messages.GROUPS)}
        columns={[
          {
            name: 'code',
            label: messages.get(messages.CODE),
            sort: 'asc',
            getValue: ({ row }) => row.code.value,
            renderValue: ({ value }) => {
              if (value) {
                return <UserGroupLink groupCode={value} />
              } else {
                return null
              }
            }
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            getValue: ({ row }) => row.description.value,
            renderValue: ({ value, classes }) => {
              if (value) {
                return <span className={classes.wrap}>{value}</span>
              } else {
                return null
              }
            }
          }
        ]}
        rows={rows}
        selectedRowId={selectedRowId}
        onSelectedRowChange={onSelectedRowChange}
      />
    )
  }
}
