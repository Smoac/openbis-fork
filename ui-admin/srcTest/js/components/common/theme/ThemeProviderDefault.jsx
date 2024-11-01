import React from 'react'
import { createTheme, ThemeProvider } from '@mui/material/styles'
import { config } from '@src/js/components/common/theme/ThemeProviderDefault.jsx'

const theme = createTheme({
  ...config,
  props: {
    MuiCollapse: {
      timeout: 0
    }
  },
  transitions: {
    create: () => 'none'
  }
})

export default class ThemeProviderDefault extends React.Component {
  render() {
    return (
      <ThemeProvider theme={theme}>{this.props.children}</ThemeProvider>
    )
  }
}
