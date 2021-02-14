import { DefaultTheme, Provider as PaperProvider } from 'react-native-paper';

import IncommingCallScan from './container/incomming-call-scan';
import React from "react";

const theme = {
  ...DefaultTheme,
  roundness: 2,
  colors: {
    ...DefaultTheme.colors,
    primary: '#428ec0',
    accent: '#f1c40f',
  },
};

const App: React.FC = () => {
  return (
    <PaperProvider theme={theme}>
      <IncommingCallScan />
    </PaperProvider>
  );
}

export default App;