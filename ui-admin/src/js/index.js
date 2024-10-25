import 'regenerator-runtime/runtime'
import React from 'react'
import {createRoot} from 'react-dom/client'
import ErrorBoundary from '@src/js/components/common/error/ErrorBoundary.jsx'
import DatePickerProvider from '@src/js/components/common/date/DatePickerProvider.jsx'
import ThemeProviderDefault from '@src/js/components/common/theme/ThemeProviderDefault.jsx'
import { StyledEngineProvider } from "@mui/material/styles";

const render = () => {
  let App = require('./components/App.jsx').default

  const container = document.getElementById('app');
  const root = createRoot(container);
  root.render(
    <StyledEngineProvider injectFirst>
      <ThemeProviderDefault>
        <ErrorBoundary>
          <DatePickerProvider>
            <App />
          </DatePickerProvider>
        </ErrorBoundary>
      </ThemeProviderDefault>
    </StyledEngineProvider>
  )
}

/* eslint-disable no-undef */
if (module.hot) {
  module.hot.accept('./components/App.jsx', () => setTimeout(render))
}

render()
