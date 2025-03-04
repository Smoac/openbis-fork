import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import withStyles from '@mui/styles/withStyles';
import Loading from '@src/js/components/common/loading/Loading.jsx'
import Table from '@mui/material/Table'
import TableHead from '@mui/material/TableHead'
import TableBody from '@mui/material/TableBody'
import Header from '@src/js/components/common/form/Header.jsx'
import GridController from '@src/js/components/common/grid/GridController.js'
import GridFilters from '@src/js/components/common/grid/GridFilters.jsx'
import GridHeaders from '@src/js/components/common/grid/GridHeaders.jsx'
import GridSelectionInfo from '@src/js/components/common/grid/GridSelectionInfo.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'
import GridRowFullWidth from '@src/js/components/common/grid/GridRowFullWidth.jsx'
import GridExports from '@src/js/components/common/grid/GridExports.jsx'
import GridExportLoading from '@src/js/components/common/grid/GridExportLoading.jsx'
import GridExportWarnings from '@src/js/components/common/grid/GridExportWarnings.jsx'
import GridExportError from '@src/js/components/common/grid/GridExportError.jsx'
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
  pagingAndConfigsAndExportsContent: {
    display: 'flex'
  }
})

class Grid extends React.PureComponent {
  static defaultProps = {
    id: 'grid'
  }

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

    const { id, classes, showHeaders } = this.props
    const { loading, rows } = this.state
    const doShowHeaders = typeof showHeaders === 'boolean' ? showHeaders : true

    return (
      <div
        id={id}
        onClick={this.handleClickContainer}
        className={classes.container}
      >
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
                  {doShowHeaders && this.renderHeaders()}
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
            {this.renderExportState()}
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
    const { multiselectable, classes, showPaging, showConfigs } = this.props
    const doShowPaging = typeof showPaging === 'boolean' ? showPaging : true
    const doShowConfigs = typeof showConfigs === 'boolean' ? showConfigs : true

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridRowFullWidth
        multiselectable={multiselectable}
        columns={visibleColumns}
        styles={{
          content: classes.pagingAndConfigsAndExportsContent
        }}
      >
        {doShowPaging && this.renderPaging()}
        {doShowConfigs && this.renderConfigs()}
        {this.renderExports()}
      </GridRowFullWidth>
    )
  }

  renderPaging() {
    const { id, showRowsPerPage } = this.props
    const { page, pageSize, totalCount } = this.state

    return (
      <GridPaging
        id={id}
        count={totalCount}
        page={page}
        pageSize={pageSize}
        showRowsPerPage={showRowsPerPage}
        onPageChange={this.controller.handlePageChange}
        onPageSizeChange={this.controller.handlePageSizeChange}
      />
    )
  }

  renderConfigs() {
    const { id, filterModes } = this.props
    const { loading, filterMode, columnsVisibility } = this.state

    const allColumns = this.controller.getAllColumns()

    return (
      <React.Fragment>
        <GridColumnsConfig
          id={id}
          columns={allColumns}
          columnsVisibility={columnsVisibility}
          loading={loading}
          onVisibleChange={this.controller.handleColumnVisibleChange}
          onOrderChange={this.controller.handleColumnOrderChange}
        />
        <GridFiltersConfig
          id={id}
          filterModes={filterModes}
          filterMode={filterMode}
          loading={loading}
          onFilterModeChange={this.controller.handleFilterModeChange}
        />
      </React.Fragment>
    )
  }

  renderExports() {
    const { id, multiselectable } = this.props
    const { rows, multiselectedRows, exportOptions } = this.state

    const exportable = this.controller.getExportable()

    if (!exportable) {
      return null
    }

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridExports
        id={id}
        disabled={rows.length === 0}
        exportable={exportable}
        exportOptions={exportOptions}
        multiselectable={multiselectable}
        multiselectedRows={multiselectedRows}
        visibleColumns={visibleColumns}
        onExport={this.controller.handleExport}
        onExportOptionsChange={this.controller.handleExportOptionsChange}
      />
    )
  }

  renderExportState() {
    const { exportState } = this.state

    if (!exportState) {
      return null
    }

    return (
      <React.Fragment>
        <GridExportLoading loading={!!exportState.loading} />
        <GridExportError
          open={!_.isEmpty(exportState.error)}
          error={exportState.error}
          onClose={this.controller.handleExportCancel}
        />
        <GridExportWarnings
          open={!_.isEmpty(exportState.warnings)}
          warnings={exportState.warnings}
          onDownload={() =>
            this.controller.handleExportDownload(
              exportState.fileName,
              exportState.fileUrl
            )
          }
          onCancel={this.controller.handleExportCancel}
        />
      </React.Fragment>
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
    const { id, filterModes, multiselectable } = this.props
    const { filterMode, filters, globalFilter } = this.state

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridFilters
        id={id}
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
    const { selectable, multiselectable, onRowClick, onRowDoubleClick } = this.props
    const { selectedRow, multiselectedRows, heights } = this.state

    const visibleColumns = this.controller.getVisibleColumns()

    return (
      <GridRow
        key={row.id}
        columns={visibleColumns}
        row={row}
        heights={heights[row.id]}
        clickable={!!onRowClick}
        doubleClickable={!!onRowDoubleClick}
        selectable={selectable}
        selected={selectedRow ? selectedRow.id === row.id : false}
        multiselectable={multiselectable}
        multiselected={multiselectedRows && multiselectedRows[row.id]}
        onClick={this.controller.handleRowClick}
        onDoubleClick={this.controller.handleRowDoubleClick}
        onSelect={this.controller.handleRowSelect}
        onMultiselect={this.controller.handleRowMultiselect}
        onMeasured={this.controller.handleMeasured}
      />
    )
  }
}

export default withStyles(styles)(Grid)
