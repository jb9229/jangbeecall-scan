import * as React from 'react';

import { Platform, StyleProp, Switch, TextStyle, ViewStyle } from 'react-native';

import styled from 'styled-components/native';

interface StyleSwitchStyleProps{
  spaceBetween?: boolean;
}
const Container = styled.View<StyleSwitchStyleProps>`
  flex-direction: row;
  justify-content: ${(props): string => props.spaceBetween ? 'space-between' : 'flex-start'};
  align-items: center;
`;
const LabelWrap = styled.View`
  margin-right: 11;
`;
const Label = styled.Text``;
const SwitchWrap = styled.View``;
const StyleSwitch = styled(Switch).attrs((props) => ({
  trackColor: { true: props.theme.ColorPrimary },
  height: 10
}))`
  width: 50;
  height: 31;
`;

interface Props {
  label?: string;
  value: boolean;
  wrapperStyle?: StyleProp<ViewStyle>;
  labelStyle?: StyleProp<TextStyle>;
  onValueChange: (value: boolean) => void;
}

const SwitchComponent: React.FC<Props> = (props) => {
  return (
    <Container>
      {!!props.label &&
        <LabelWrap>
          <Label>{props.label}</Label>
        </LabelWrap>
      }
      <SwitchWrap>
        <StyleSwitch
          value={props.value}
          style={{ transform: [{ scaleX: Platform.OS === 'ios' ? 0.9 : 1.7 }, { scaleY: Platform.OS === 'ios' ? 0.9 : 1.7 }] }}
          onValueChange={(value: boolean): void =>
          {
            props.onValueChange(value);
          }}
          thumbColor={'white'}
        />
      </SwitchWrap>
    </Container>
  );
};

export default SwitchComponent;
