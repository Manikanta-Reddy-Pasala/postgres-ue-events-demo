import React from 'react';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';
import Dashboard from './Dashboard';

const theme = createTheme({
  palette: {
    mode: 'light',
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Dashboard />
    </ThemeProvider>
  );
}

export default App;
