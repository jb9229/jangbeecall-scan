import React, { useEffect, useState } from 'react';
import { checkBLScanServiceRunning, switchBLCallScan } from './action';

import { Button } from 'react-native';
import styled from 'styled-components/native';

const Container = styled.View`
    flex: 1;
    align-items: center;
    justify-content: center;
`;

const IncommingCallScanContainer:React.FC = (): React.ReactElement =>
{
  const [running, setRunning] = useState<boolean | undefined>(false);
  // component life cycle
  useEffect(() => {
    const result = checkBLScanServiceRunning();

    setRunning(result);
  }, [])

  // actions
  const onSwitchBLCallScan = () => {
    switchBLCallScan(!!running); 
  }

  // rendering ui
  return (
    <Container>
      <Button title="수신전화 피해사례 조사" onPress={onSwitchBLCallScan }/>
    </Container>
  );
};

export default IncommingCallScanContainer;