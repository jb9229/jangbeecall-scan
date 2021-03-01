import React, { useEffect, useState } from 'react';
import { Title, ToggleButton, useTheme } from 'react-native-paper';
import { checkBLScanServiceRunning, switchBLCallScan } from './action';

import styled from 'styled-components/native';

const Container = styled.View`
    flex: 1;
    align-items: center;
    justify-content: center;
`;

const CallScanToggleButton = styled(ToggleButton)`
  width: 250px;
  height: 250px;
`;
const ButtonWrap = styled.View`
  width: 250px;
  height: 250px;
  margin-bottom: 30px;
  border-radius: 125px;
  shadow-offset: 0px 2px;
  shadow-color: #000;
  shadow-opacity: 0.25;
  shadow-radius: 4px;
  elevation: 5;
  background-color: white;
`;

const IncommingCallScanContainer:React.FC = (): React.ReactElement =>
{
  const { colors } = useTheme();
  const [running, setRunning] = useState<boolean>(false);
  // component life cycle
  useEffect(() => {
    checkBLScanServiceRunning(setCallScanCallback);
  }, [])

  // actions
  const setCallScanCallback = (isScaning: boolean) => {
    setRunning(isScaning);
  }
  const onSwitchBLCallScan = () => {
    switchBLCallScan(running); 
    setRunning(!running);
  }

  // rendering ui
  return (
    <Container>
      <ButtonWrap>
        <CallScanToggleButton
          size={120}
          icon="account-search-outline"
          value="account-search-outline"
          status={running ? "checked" : "unchecked"}
          color={running ? 'white' : colors.primary}
          onPress={onSwitchBLCallScan}
          theme={{roundness: 200 }}
          style={{ backgroundColor: running ? colors.primary : colors.disabled }}
        />
      </ButtonWrap>
      <Title>{running? `수신전화 피해사례 알림 켜짐` : `수신전화 피해사례 알림 꺼짐`}</Title>
    </Container>
  );
};

export default IncommingCallScanContainer;