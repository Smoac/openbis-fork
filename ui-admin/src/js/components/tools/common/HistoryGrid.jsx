import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import GridWithOpenbis from '@src/js/components/common/grid/GridWithOpenbis.jsx'
import GridExportOptions from '@src/js/components/common/grid/GridExportOptions.js'
import GridFilterOptions from '@src/js/components/common/grid/GridFilterOptions.js'
import GridUtil from '@src/js/components/common/grid/GridUtil.js'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import FormUtil from '@src/js/components/common/form/FormUtil.js'
import EntityType from '@src/js/components/common/dto/EntityType.js'
import HistoryGridContentCell from '@src/js/components/tools/common/HistoryGridContentCell.jsx'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import date from '@src/js/common/date.js'
import ids from '@src/js/common/consts/ids.js'
import logger from '@src/js/common/logger.js'
import format from 'date-fns/format';

class HistoryGrid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
  }

  async load(params) {
    return await this.loadHistory(this.props.eventType, params)
  }

  async loadHistory(eventType, { filters, page, pageSize, sortings }) {
    const criteria = new openbis.EventSearchCriteria()
    criteria.withEventType().thatEquals(eventType)

    Object.keys(filters).forEach(filterName => {
      const filterValue = filters[filterName]
      if (filterName === 'entityType') {
        criteria.withEntityType().thatEquals(filterValue)
      } else if (filterName === 'registrator') {
        criteria.withRegistrator().withUserId().thatContains(filterValue)
      } else if (
        filterName === 'registrationDate' ||
        filterName === 'entityRegistrationDate'
      ) {
        if (filterValue.from && filterValue.from.dateObject) {
          criteria['with' + _.upperFirst(filterName)]()
            .withTimeZone(date.timezone())
            .thatIsLaterThanOrEqualTo(format(filterValue.from.dateObject, 'yyyy-MM-dd HH:mm:ss').toString())
        }
        if (filterValue.to && filterValue.to.dateObject) {
          criteria['with' + _.upperFirst(filterName)]()
            .withTimeZone(date.timezone())
            .thatIsEarlierThanOrEqualTo(format(filterValue.to.dateObject, 'yyyy-MM-dd HH:mm:ss').toString())
        }
      } else {
        criteria['with' + _.upperFirst(filterName)]().thatContains(filterValue)
      }
    })

    const fo = new openbis.EventFetchOptions()
    fo.withRegistrator()
    fo.from(page * pageSize)
    fo.count(pageSize)

    sortings.forEach(sorting => {
      fo.sortBy()[sorting.columnName]()[sorting.sortDirection]()
    })

    const result = await openbis.searchEvents(criteria, fo)

    const rows = result.objects.map(event => ({
      id: _.get(event, 'id.techId'),
      entityType: FormUtil.createField({
        value: _.get(event, 'entityType')
      }),
      entitySpace: FormUtil.createField({
        value: _.get(event, 'entitySpace')
      }),
      entityProject: FormUtil.createField({
        value: _.get(event, 'entityProject')
      }),
      entityRegistrator: FormUtil.createField({
        value: _.get(event, 'entityRegistrator')
      }),
      entityRegistrationDate: FormUtil.createField({
        value: _.get(event, 'entityRegistrationDate')
      }),
      identifier: FormUtil.createField({
        value: _.get(event, 'identifier')
      }),
      description: FormUtil.createField({
        value: _.get(event, 'description')
      }),
      reason: FormUtil.createField({
        value: _.get(event, 'reason')
      }),
      content: FormUtil.createField({
        value: _.get(event, 'content'),
        visible: false
      }),
      registrator: FormUtil.createField({
        value: _.get(event, 'registrator.userId')
      }),
      registrationDate: FormUtil.createField({
        value: _.get(event, 'registrationDate')
      })
    }))

    return {
      rows,
      totalCount: result.totalCount
    }
  }

  render() {
    logger.log(logger.DEBUG, 'HistoryGrid.render')

    const id = this.getId()

    return (
      <GridWithOpenbis
        id={id}
        settingsId={id}
        filterModes={[GridFilterOptions.COLUMN_FILTERS]}
        header={this.getHeader()}
        columns={[
          {
            name: 'entityType',
            label: messages.get(messages.ENTITY_TYPE),
            sortable: false,
            getValue: ({ row }) => row.entityType.value,
            renderValue: ({ value }) => new EntityType(value).getLabel(),
            renderFilter: ({ value, onChange }) => {
              return (
                <SelectField
                  label={messages.get(messages.FILTER)}
                  value={value}
                  emptyOption={{}}
                  options={openbis.EntityType.values.map(entityType => ({
                    label: new EntityType(entityType).getLabel(),
                    value: entityType
                  }))}
                  onChange={onChange}
                  variant='standard'
                />
              )
            }
          },
          {
            name: 'identifier',
            label: messages.get(messages.ENTITY_IDENTIFIER),
            sortable: true,
            getValue: ({ row }) => row.identifier.value
          },
          {
            name: 'entitySpace',
            label: messages.get(messages.ENTITY_SPACE),
            sortable: false,
            getValue: ({ row }) => row.entitySpace.value
          },
          {
            name: 'entityProject',
            label: messages.get(messages.ENTITY_PROJECT),
            sortable: false,
            getValue: ({ row }) => row.entityProject.value
          },
          GridUtil.userColumn({
            name: 'entityRegistrator',
            label: messages.get(messages.ENTITY_REGISTRATOR),
            path: 'entityRegistrator.value',
            sortable: false
          }),
          GridUtil.dateColumn({
            name: 'entityRegistrationDate',
            label: messages.get(messages.ENTITY_REGISTRATION_DATE),
            path: 'entityRegistrationDate.value',
            sortable: false
          }),
          {
            name: 'reason',
            label: messages.get(messages.REASON),
            sortable: false,
            getValue: ({ row }) => row.reason.value
          },
          {
            name: 'description',
            label: messages.get(messages.DESCRIPTION),
            sortable: false,
            getValue: ({ row }) => row.description.value
          },
          {
            name: 'content',
            label: messages.get(messages.CONTENT),
            sortable: false,
            filterable: false,
            getValue: ({ row }) => row.content.value,
            renderValue: ({ value }) => {
              return <HistoryGridContentCell value={value} />
            }
          },
          GridUtil.registratorColumn({
            path: 'registrator.value',
            sortable: false
          }),
          GridUtil.registrationDateColumn({ path: 'registrationDate.value' })
        ]}
        loadRows={this.load}
        sort='registrationDate'
        sortDirection='desc'
        exportable={this.getExportable()}
        selectable={true}
      />
    )
  }

  getId() {
    const { eventType } = this.props

    if (eventType === openbis.EventType.DELETION) {
      return ids.HISTORY_OF_DELETION_GRID_ID
    } else if (eventType === openbis.EventType.FREEZING) {
      return ids.HISTORY_OF_FREEZING_GRID_ID
    }
  }

  getExportable() {
    const { eventType } = this.props

    if (eventType === openbis.EventType.DELETION) {
      return {
        fileFormat: GridExportOptions.FILE_FORMAT.TSV,
        filePrefix: 'deletion-history'
      }
    } else if (eventType === openbis.EventType.FREEZING) {
      return {
        fileFormat: GridExportOptions.FILE_FORMAT.TSV,
        filePrefix: 'freezing-history'
      }
    }
  }

  getHeader() {
    const { eventType } = this.props

    if (eventType === openbis.EventType.DELETION) {
      return messages.get(messages.DELETIONS)
    } else if (eventType === openbis.EventType.FREEZING) {
      return messages.get(messages.FREEZES)
    }
  }
}

export default HistoryGrid
