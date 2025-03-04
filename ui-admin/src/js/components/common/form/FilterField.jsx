import React from 'react'
import withStyles from '@mui/styles/withStyles';
import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import CircularProgress from '@mui/material/CircularProgress'
import IconButton from '@mui/material/IconButton'
import FilterIcon from '@mui/icons-material/FilterList'
import CloseIcon from '@mui/icons-material/Close'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  field: {
    width: '100%'
  },
  input: {
    fontSize: theme.typography.body2.fontSize,
    height: '26px'
  },
  underline: {
    '&:before': {
      borderBottomColor: theme.palette.border.primary
    }
  },
  adornment: {
    margin: '8px',
    marginLeft: '10px',
    minHeight: '33px' //keeps the height same as the tabs
  },
  adornmentButton: {
    padding: '4px'
  },
  adornmentSpacer: {
    width: '64px'
  }
})

class FilterField extends React.Component {
  constructor(props) {
    super(props)
    this.filterRef = React.createRef()
    this.handleFilterChange = this.handleFilterChange.bind(this)
    this.handleFilterClear = this.handleFilterClear.bind(this)
  }

  handleFilterChange(event) {
    this.props.filterChange(event.target.value)
  }

  handleFilterClear(event) {
    event.preventDefault()
    this.props.filterClear()
    this.filterRef.current.focus()
  }

  render() {
    logger.log(logger.DEBUG, 'FilterField.render')

    const classes = this.props.classes

    return (<TextField
        className={classes.field}
        placeholder={messages.get(messages.FILTER)}
        value={this.props.filter}
        variant='standard'
        onChange={this.handleFilterChange}
        margin='none'
        slotProps={{
          input: {
            inputRef: this.filterRef,
            startAdornment: this.renderFilterIcon(),
            endAdornment: this.renderEndAdornment(),
            classes: {
              input: classes.input,
              underline: classes.underline
            }
          }
        }}
      />
    );
  }

  renderFilterIcon() {
    const { loading, classes } = this.props

    const icon = loading ? (
      <CircularProgress size={20} />
    ) : (
      <FilterIcon fontSize='small' />
    )

    return (
      <InputAdornment
        position='start'
        classes={{
          root: classes.adornment
        }}
      >
        {icon}
      </InputAdornment>
    )
  }

  renderEndAdornment() {
    const { endAdornments } = this.props
    return (
      <React.Fragment>
        {this.renderFilterClearIcon()}
        {endAdornments}
      </React.Fragment>
    )
  }

  renderFilterClearIcon() {
    const classes = this.props.classes

    if (this.props.filter) {
      return (
        (<InputAdornment
          position='end'
          classes={{
            root: classes.adornment
          }}
        >
          <Tooltip title={messages.get(messages.CLEAR_FILTER)}>
            <IconButton
              onClick={this.handleFilterClear}
              classes={{ root: classes.adornmentButton }}
              size="large">
              <CloseIcon fontSize='small' />
            </IconButton>
          </Tooltip>
        </InputAdornment>)
      );
    } else {
      return (
        <React.Fragment>
          <div className={classes.adornmentSpacer}></div>
        </React.Fragment>
      )
    }
  }
}

export default withStyles(styles)(FilterField)
