import { NativeModules } from 'react-native';

export const checkBLScanServiceRunning = (callback: (isScaning: boolean) => void): void => {
  const { CallDetection } = NativeModules;

  if (!!!CallDetection) { return }

  return CallDetection.isRunningService(
    callback,
    (errorMsg: string) => { alert(`[수신전화 피해사례조회 권한 설정 필요] ${errorMsg}`); return undefined }
  );
};

/**
 * Switching Black List Imcomming Calling Scan
 */
export const switchBLCallScan = (blCallScanSwitch: boolean) => {
  const { CallDetection } = NativeModules;

  if (!!!CallDetection) { alert('네이티브 모듈 연동에 실패 했습니다'); return null }

  if (!blCallScanSwitch) {
    CallDetection.start(
      (startResult: boolean) => startResult,
      (errorMsg: string) => {
        alert(
          `[수신전화 피해사례조회 서비스 시작 실패] 요청에 문제가 발생했습니다, 다시 시작해 주세요: ${errorMsg}`
        );
      }
    );
  } else {
    CallDetection.finish(
      (stopResult: boolean) => stopResult,
      (errorMsg: string) => {
        alert(
          `[수신전화 피해사례조회 서비스 종료 실패] 요청에 문제가 발생했습니다, 다시 종료해 주세요: ${errorMsg}`
        );
      }
    );
  }
};