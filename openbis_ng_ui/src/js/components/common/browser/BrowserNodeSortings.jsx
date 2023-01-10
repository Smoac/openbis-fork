import _ from 'lodash'
import React from 'react'
import autoBind from 'auto-bind'
import { withStyles } from '@material-ui/core/styles'
import Tooltip from '@src/js/components/common/form/Tooltip.jsx'
import IconButton from '@material-ui/core/IconButton'
import CloseIcon from '@material-ui/icons/Close'
import Sort from '@material-ui/icons/Sort'
import Mask from '@src/js/components/common/loading/Mask.jsx'
import Popover from '@material-ui/core/Popover'
import Container from '@src/js/components/common/form/Container.jsx'
import RadioGroupField from '@src/js/components/common/form/RadioGroupField.jsx'
import BrowserTreeController from '@src/js/components/common/browser/BrowserTreeController.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    paddingLeft: theme.spacing(2)
  },
  button: {
    padding: '4px',
    margin: '-4px'
  }
})

class BrowserNodeSortings extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.state = {
      el: null
    }
  }

  handleOpen(event) {
    this.setState({
      el: event.currentTarget
    })
  }

  handleClose() {
    this.setState({
      el: null
    })
  }

  handleChange(event) {
    const { node, onChange } = this.props
    if (onChange) {
      onChange(node.id, event.target.value)
    }
  }

  handleClearCustom() {
    const { node, onClearCustom } = this.props
    if (onClearCustom) {
      onClearCustom(node.id)
    }
  }

  render() {
    logger.log(logger.DEBUG, 'BrowserNodeSortings.render')

    const { node, classes } = this.props

    if (
      !node ||
      !node.canHaveChildren ||
      (_.isEmpty(node.sortings) && _.isEmpty(node.customSorting))
    ) {
      return null
    }

    const { el } = this.state

    return (
      <div
        className={classes.container}
        onClick={event => {
          event.preventDefault()
          event.stopPropagation()
        }}
      >
        <Tooltip title={messages.get(messages.CHANGE_SORTING)}>
          <IconButton
            size='small'
            onClick={this.handleOpen}
            classes={{ root: classes.button }}
          >
            <Sort fontSize='small' />
          </IconButton>
        </Tooltip>
        <Popover
          open={Boolean(el)}
          anchorEl={el}
          onClose={this.handleClose}
          anchorOrigin={{
            vertical: 'bottom',
            horizontal: 'left'
          }}
          transformOrigin={{
            vertical: 'top',
            horizontal: 'left'
          }}
        >
          <Mask visible={node.loading}>
            <Container square={true}>{this.renderSortings()}</Container>
          </Mask>
        </Popover>
      </div>
    )
  }

  renderSortings() {
    const { node } = this.props

    let options = []

    if (!_.isEmpty(node.sortings)) {
      const sortings = Object.entries(node.sortings)
        .map(([id, sorting]) => ({
          value: id,
          label: sorting.label,
          index: sorting.index
        }))
        .sort(sorting => sorting.index)
      options = [...sortings]
    }

    if (!_.isEmpty(node.customSorting)) {
      options = [
        {
          id: BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID,
          value: BrowserTreeController.INTERNAL_CUSTOM_SORTING_ID,
          label: this.renderCustomSorting()
        },
        ...options
      ]
    }

    return (
      <RadioGroupField
        name='sorting'
        value={node.sortingId}
        options={options}
        onChange={this.handleChange}
      />
    )
  }

  renderCustomSorting() {
    return (
      <span>
        {messages.get(messages.CUSTOM_SORTING)}
        <CloseIcon fontSize='small' onClick={this.handleClearCustom} />
      </span>
    )
  }
}

export default withStyles(styles)(BrowserNodeSortings)
