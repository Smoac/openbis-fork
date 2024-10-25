import React from 'react'
import {AdapterDateFns} from '@mui/x-date-pickers/AdapterDateFnsV3'
import {LocalizationProvider} from '@mui/x-date-pickers/LocalizationProvider';

class DatePickerProvider extends React.Component {
  render() {
    return (
      <LocalizationProvider dateAdapter={AdapterDateFns}>
        {this.props.children}
      </LocalizationProvider>
    )
  }
}

export default DatePickerProvider
