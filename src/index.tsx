import * as Updates from 'expo-updates';

import { ActivityIndicator, Colors } from 'react-native-paper';
import { DefaultTheme, Provider as PaperProvider } from 'react-native-paper';
import React, { useEffect, useState } from "react";

import IncommingCallScan from './container/incomming-call-scan';

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
  const [loadding, setLoading] = useState(true);
  useEffect(() => {
    (async() => {
      try {
        const update = await Updates.checkForUpdateAsync();
        if (update.isAvailable) {
          await Updates.fetchUpdateAsync();
          // ... notify user of update ...
          await Updates.reloadAsync();
        }
        setLoading(false);
      } catch (e) {
        // handle or log error
        alert(`error: ${e?.message}`);
        setLoading(false);
      }
    })();
  }, [])

  if (loadding) { return (<ActivityIndicator animating={true} color={Colors.red800} />) }
  return (
    <PaperProvider theme={theme}>
      <IncommingCallScan />
    </PaperProvider>
  );
}

export default App;