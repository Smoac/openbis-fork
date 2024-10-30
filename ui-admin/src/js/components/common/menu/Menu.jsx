import _ from 'lodash'
import autoBind from 'auto-bind'
import React from 'react'
import AppBar from '@mui/material/AppBar'
import Toolbar from '@mui/material/Toolbar'
import Tabs from '@mui/material/Tabs'
import Tab from '@mui/material/Tab'
import TextField from '@mui/material/TextField'
import InputAdornment from '@mui/material/InputAdornment'
import SearchIcon from '@mui/icons-material/Search'
import CloseIcon from '@mui/icons-material/Close'
import LogoutIcon from '@mui/icons-material/PowerSettingsNew'
import { alpha } from '@mui/material/styles';
import withStyles from '@mui/styles/withStyles';
import AppController from '@src/js/components/AppController.js'
import Button from '@src/js/components/common/form/Button.jsx'
import pages from '@src/js/common/consts/pages.js'
import messages from '@src/js/common/messages.js'
import logger from '@src/js/common/logger.js'
import Typography from "@mui/material/Typography";

const styles = theme => ({
  appBar: {
    position: 'relative',
    zIndex: 400
  },
  toolBar: {
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2)
  },
  tabs: {
    flexGrow: 1,
    textColor: theme.palette.background.secondary
  },
  search: {
    color: theme.palette.background.paper,
    backgroundColor: alpha(theme.palette.background.paper, 0.15),
    '&:hover': {
      backgroundColor: alpha(theme.palette.background.paper, 0.25)
    },
    borderRadius: theme.shape.borderRadius,
    paddingLeft: theme.spacing(1),
    paddingRight: theme.spacing(1),
    marginRight: theme.spacing(1),
    transition: theme.transitions.create('width'),
    width: '200px',
    '&:focus-within': {
      width: '300px'
    },
    fontSize: '14px'
  },
  searchIcon: {
    paddingLeft: theme.spacing(1) / 2,
    paddingRight: theme.spacing(1),
    cursor: 'default'
  },
  searchClear: {
    cursor: 'pointer'
  },
  userInfo: {
    margin: '0px 10px 0px 5px'
  }
})

class Menu extends React.PureComponent {
  constructor(props) {
    super(props)
    autoBind(this)
    this.searchRef = React.createRef()
  }

  handlePageChange(event, value) {
    AppController.getInstance().pageChange(value)
  }

  handleSearchChange(event) {
    AppController.getInstance().searchChange(event.target.value)
  }

  handleSearchKeyPress(event) {
    if (event.key === 'Enter') {
      AppController.getInstance().search(
        this.props.currentPage,
        this.props.searchText
      )
    }
  }

  handleSearchClear(event) {
    event.preventDefault()
    AppController.getInstance().searchChange('')
    this.searchRef.current.focus()
  }

  handleLogout() {
    AppController.getInstance().logout()
  }

  render() {
    logger.log(logger.DEBUG, 'Menu.render')

    const { classes, searchText, userName } = this.props
    return (
      (<AppBar position='static' classes={{ root: classes.appBar }}>
        <Toolbar variant='dense' classes={{ root: classes.toolBar }}>
          <Tabs
            value={this.props.currentPage}
            onChange={this.handlePageChange}
            classes={{ root: classes.tabs }}
            textColor='inherit'
            indicatorColor='secondary'
          >
            <Tab
              value={pages.DATABASE}
              label={messages.get(messages.DATABASE)}
            />
            <Tab value={pages.TYPES} label={messages.get(messages.TYPES)} />
            <Tab value={pages.USERS} label={messages.get(messages.USERS)} />
            <Tab value={pages.TOOLS} label={messages.get(messages.TOOLS)} />
          </Tabs>
          <TextField
            placeholder={messages.get(messages.SEARCH)}
            value={searchText || ''}
            onChange={this.handleSearchChange}
            onKeyPress={this.handleSearchKeyPress}
            variant='standard'
            slotProps={{
              input: {
                inputRef: this.searchRef,
                disableUnderline: true,
                startAdornment: this.renderSearchIcon(),
                endAdornment: this.renderSearchClearIcon(),
                classes: {
                  root: classes.search
                }
              }
            }}
          />
          <Typography variant="body1" classes={{ root: classes.userInfo }}>
            {userName}
          </Typography>
          <Button
            label={<LogoutIcon fontSize='small' />}
            type='final'
            onClick={this.handleLogout}
          />
        </Toolbar>
      </AppBar>)
    );
  }

  renderSearchIcon() {
    const { classes } = this.props
    return (
      <InputAdornment position='start'>
        <SearchIcon classes={{ root: classes.searchIcon }} fontSize='small' />
      </InputAdornment>
    )
  }

  renderSearchClearIcon() {
    const { classes, searchText } = this.props
    if (searchText) {
      return (
        <InputAdornment position='end'>
          <CloseIcon
            classes={{ root: classes.searchClear }}
            onMouseDown={this.handleSearchClear}
            fontSize='small'
          />
        </InputAdornment>
      )
    } else {
      return <React.Fragment></React.Fragment>
    }
  }
}

export default _.flow(
  withStyles(styles),
  AppController.getInstance().withState(() => ({
    currentPage: AppController.getInstance().getCurrentPage(),
    searchText: AppController.getInstance().getSearch()
  }))
)(Menu)
