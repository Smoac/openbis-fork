import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Table from '@material-ui/core/Table'
import TableHead from '@material-ui/core/TableHead'
import TableBody from '@material-ui/core/TableBody'
import Header from '@src/js/components/common/form/Header.jsx'
import GridController from '@src/js/components/common/grid/GridController.js'
import GridFilters from '@src/js/components/common/grid/GridFilters.jsx'
import GridHeaders from '@src/js/components/common/grid/GridHeaders.jsx'
import GridSelectionInfo from '@src/js/components/common/grid/GridSelectionInfo.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'
import GridRowFullWidth from '@src/js/components/common/grid/GridRowFullWidth.jsx'
import GridExports from '@src/js/components/common/grid/GridExports.jsx'
import GridPaging from '@src/js/components/common/grid/GridPaging.jsx'
import GridColumnsConfig from '@src/js/components/common/grid/GridColumnsConfig.jsx'
import GridFiltersConfig from '@src/js/components/common/grid/GridFiltersConfig.jsx'
import ComponentContext from '@src/js/components/common/ComponentContext.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    minWidth: '800px',
    height: '100%'
  },
  loadingContainer: {
    flex: '1 1 auto'
  },
  loading: {
    display: 'inline-block'
  },
  tableContainer: {
    display: 'inline-block',
    minWidth: '100%',
    height: '100%'
  },
  table: {
    borderCollapse: 'unset'
  },
  tableHead: {
    position: 'sticky',
    top: 0,
    zIndex: '200',
    backgroundColor: theme.palette.background.paper
  },
  titleCell: {
    border: 0
  },
  titleContent: {
    paddingLeft: theme.spacing(2)
  },
  title: {
    paddingTop: theme.spacing(1),
    paddingBottom: 0
  },
  pagingAndConfigsAndExportsCell: {},
  pagingAndConfigsAndExportsContent: {
    display: 'flex'
  }
})

class Grid extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)

    this.state = {}

    if (this.props.controller) {
      this.controller = this.props.controller
    } else {
      this.controller = new GridController()
    }

    this.controller.init(new ComponentContext(this))

    if (this.props.controllerRef) {
      this.props.controllerRef(this.controller)
    }
  }

  componentDidMount() {
    this.controller.load()
  }

  handleClickContainer() {
    this.controller.handleRowSelect(null)
  }

  handleClickTable(event) {
    event.stopPropagation()
  }

  render() {
    logger.log(logger.DEBUG, 'Grid.render')

    if (!this.state.loaded) {
      return <Loading loading={true}></Loading>
    }

    const { classes } = this.props
    const { loading, rows } = this.state

    return (
      <div onClick={this.handleClickContainer} className={classes.container}>
        <div className={classes.loadingContainer}>
          <Loading loading={loading} styles={{ root: classes.loading }}>
            <div className={classes.tableContainer}>
              <Table
                classes={{ root: classes.table }}
                onClick={this.handleClickTable}
              >
                <TableHead classes={{ root: classes.tableHead }}>
                  {this.renderTitle()}
                  {this.renderPagingAndConfigsAndExports()}
                  {this.renderHeaders()}
                  {this.renderFilters()}
                  {this.renderSelectionInfo()}
                </TableHead>
                <TableBody>
                  {rows.map(row => {
                    return this.renderRow(row)
                  })}
                </TableBody>
              </Table>
            </div>
          </Loading>
        </div>
      </div>
    )
  }

  renderTitle() {
    const { header, multiselectable, classes } = this.props

    if (header === null || header === undefined) {
      return null
    }

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridRowFullWidth
        multiselectable={multiselectable}
        columns={visibleColumns}
        styles={{ cell: classes.titleCell, content: classes.titleContent }}
      >
        <div onClick={this.handleClickContainer}>
          <Header styles={{ root: classes.title }}>{header}</Header>
        </div>
      </GridRowFullWidth>
    )
  }

  renderPagingAndConfigsAndExports() {
    const { multiselectable, classes } = this.props

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridRowFullWidth
        multiselectable={multiselectable}
        columns={visibleColumns}
        styles={{
          cell: classes.pagingAndConfigsAndExportsCell,
          content: classes.pagingAndConfigsAndExportsContent
        }}
      >
        {this.renderPaging()}
        {this.renderConfigs()}
        {this.renderExports()}
      </GridRowFullWidth>
    )
  }

  renderPaging() {
    const { page, pageSize, totalCount } = this.state

    return (
      <GridPaging
        count={totalCount}
        page={page}
        pageSize={pageSize}
        onPageChange={this.controller.handlePageChange}
        onPageSizeChange={this.controller.handlePageSizeChange}
      />
    )
  }

  renderConfigs() {
    const { filterModes } = this.props
    const { loading, filterMode, columnsVisibility } = this.state

    const allColumns = this.controller.getAllColumns()

    return (
      <React.Fragment>
        <GridColumnsConfig
          columns={allColumns}
          columnsVisibility={columnsVisibility}
          loading={loading}
          onVisibleChange={this.controller.handleColumnVisibleChange}
          onOrderChange={this.controller.handleColumnOrderChange}
        />
        <GridFiltersConfig
          filterModes={filterModes}
          filterMode={filterMode}
          loading={loading}
          onFilterModeChange={this.controller.handleFilterModeChange}
        />
      </React.Fragment>
    )
  }

  renderExports() {
    const { multiselectable } = this.props
    const { rows, exportOptions } = this.state

    return (
      <GridExports
        disabled={rows.length === 0}
        exportOptions={exportOptions}
        multiselectable={multiselectable}
        onExport={this.controller.handleExport}
        onExportOptionsChange={this.controller.handleExportOptionsChange}
      />
    )
  }

  renderHeaders() {
    const { multiselectable } = this.props
    const { sortings, rows, multiselectedRows } = this.state

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridHeaders
        columns={visibleColumns}
        rows={rows}
        sortings={sortings}
        onSortChange={this.controller.handleSortChange}
        onMultiselectAllRowsChange={
          this.controller.handleMultiselectAllRowsChange
        }
        multiselectable={multiselectable}
        multiselectedRows={multiselectedRows}
      />
    )
  }

  renderFilters() {
    const { filterModes, multiselectable } = this.props
    const { filterMode, filters, globalFilter } = this.state

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridFilters
        columns={visibleColumns}
        filterModes={filterModes}
        filterMode={filterMode}
        filters={filters}
        onFilterChange={this.controller.handleFilterChange}
        onFilterModeChange={this.controller.handleFilterModeChange}
        globalFilter={globalFilter}
        onGlobalFilterChange={this.controller.handleGlobalFilterChange}
        multiselectable={multiselectable}
      />
    )
  }

  renderSelectionInfo() {
    const { multiselectable, actions } = this.props
    const { rows, multiselectedRows } = this.state

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridSelectionInfo
        columns={visibleColumns}
        rows={rows}
        actions={actions}
        onExecuteAction={this.controller.handleExecuteAction}
        onMultiselectionClear={this.controller.handleMultiselectionClear}
        multiselectable={multiselectable}
        multiselectedRows={multiselectedRows}
      />
    )
  }

  renderRow(row) {
    const { selectable, multiselectable, onRowClick } = this.props
    const { selectedRow, multiselectedRows } = this.state

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridRow
        key={row.id}
        columns={visibleColumns}
        row={row}
        clickable={onRowClick ? true : false}
        selectable={selectable}
        selected={selectedRow ? selectedRow.id === row.id : false}
        multiselectable={multiselectable}
        multiselected={multiselectedRows && multiselectedRows[row.id]}
        onClick={this.controller.handleRowClick}
        onSelect={this.controller.handleRowSelect}
        onMultiselect={this.controller.handleRowMultiselect}
      />
    )
  }
}

export default withStyles(styles)(Grid)
