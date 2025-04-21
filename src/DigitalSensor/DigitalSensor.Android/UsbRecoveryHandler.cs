using System;
using System.Collections.Generic;
using System.Linq;
using System.Diagnostics;
using System.Threading.Tasks;
using Android.Hardware.Usb;
using System.Threading;
using Android.Content.Res;


namespace DigitalSensor.Android
{
    /// <summary>
    /// USB 디바이스 통신 복구를 담당하는 클래스
    /// </summary>
    public class UsbRecoveryHandler
    {
        private UsbDeviceConnection _usbConnection;
        private UsbEndpoint _endpointRead;
        private UsbEndpoint _endpointWrite;
        private UsbInterface _usbInterface;
        private int _timeout = 5000;

        // 복구 시도 카운터
        private int _recoveryAttempts = 0;
        private const int MaxRecoveryAttempts = 3;

        // 복구 단계별 대기 시간 (밀리초)
        private const int ShortDelay = 50;
        private const int MediumDelay = 100;
        private const int LongDelay = 250;




        public UsbRecoveryHandler(UsbDeviceConnection connection, UsbEndpoint endpointRead,
                             UsbEndpoint endpointWrite, UsbInterface usbInterface, int timeout = 5000)
        {
            _usbConnection = connection ?? throw new ArgumentNullException(nameof(connection));
            _endpointRead = endpointRead ?? throw new ArgumentNullException(nameof(endpointRead));
            _endpointWrite = endpointWrite ?? throw new ArgumentNullException(nameof(endpointWrite));
            _usbInterface = usbInterface;
            _timeout = timeout;
            _recoveryAttempts = 0;
        }

        /// <summary>
        /// USB 통신 오류 복구 시도
        /// </summary>
        /// <returns>복구 성공 여부</returns>
        public bool TryRecover(Func<bool> communicationTest)
        {
            if (_usbConnection == null)
                throw new InvalidOperationException("USB 연결이 구성되지 않았습니다. Configure 메서드를 먼저 호출하세요.");

            if (communicationTest == null)
                throw new ArgumentNullException(nameof(communicationTest));

            _recoveryAttempts++;
            if (_recoveryAttempts > MaxRecoveryAttempts)
            {
                Debug.WriteLine($"최대 복구 시도 횟수 초과: {MaxRecoveryAttempts}회");
                return false;
            }

            Debug.WriteLine($"USB 복구 시도 {_recoveryAttempts} 시작");

            // 각 단계별 복구 시도
            if (TryResetEndpoint() && DelayAndTest(communicationTest, ShortDelay))
            {
                Debug.WriteLine("엔드포인트 리셋으로 복구 성공");
                return true;
            }

            if (TrySendClearHaltCommand() && DelayAndTest(communicationTest, ShortDelay))
            {
                Debug.WriteLine("CLEAR_HALT 명령으로 복구 성공");
                return true;
            }

            if (TryResetInterface() && DelayAndTest(communicationTest, MediumDelay))
            {
                Debug.WriteLine("인터페이스 리셋으로 복구 성공");
                return true;
            }

            if (TrySendDeviceResetCommand() && DelayAndTest(communicationTest, LongDelay))
            {
                Debug.WriteLine("디바이스 리셋 명령으로 복구 성공");
                return true;
            }

            Debug.WriteLine("모든 복구 방법 실패");
            return false;
        }

        /// <summary>
        /// 엔드포인트 리셋 시도
        /// </summary>
        public bool TryResetEndpoint()
        {
            try
            {
               Debug.WriteLine("엔드포인트 리셋 시도 중...");

                // 읽기 엔드포인트 리셋
                bool readReset = TryClearHaltFeature(_endpointRead);

                // 쓰기 엔드포인트 리셋
                bool writeReset = TryClearHaltFeature(_endpointWrite);

                return readReset && writeReset;
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"엔드포인트 리셋 중 오류 발생 : {ex}");
                return false;
            }
        }

        /// <summary>
        /// CLEAR_HALT 명령 전송
        /// </summary>
        public bool TrySendClearHaltCommand()
        {
            try
            {
               Debug.WriteLine("CLEAR_HALT 컨트롤 명령 전송 중...");

                const int UsbFeatureEndpointHalt = 0;
                const int UsbTypeStandard = 0x00;
                const int UsbRecipientEndpoint = 0x02;
                const int UsbReqClearFeature = 0x01;

                // 읽기 엔드포인트에 컨트롤 명령 전송
                int resultRead = _usbConnection.ControlTransfer(
                    (UsbAddressing)(UsbTypeStandard | UsbRecipientEndpoint),
                    UsbReqClearFeature,
                    UsbFeatureEndpointHalt,
                    (int)_endpointRead.Address,
                    null, 0, _timeout);

                // 쓰기 엔드포인트에 컨트롤 명령 전송
                int resultWrite = _usbConnection.ControlTransfer(
                    (UsbAddressing)(UsbTypeStandard | UsbRecipientEndpoint),
                    UsbReqClearFeature,
                    UsbFeatureEndpointHalt,
                    (int)_endpointWrite.Address,
                    null, 0, _timeout);

                bool success = resultRead >= 0 && resultWrite >= 0;
                Debug.WriteLine($"CLEAR_HALT 명령 결과: {(success ? "성공" : "실패")}");

                return success;
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"CLEAR_HALT 명령 전송 중 오류 발생: {ex}");
                return false;
            }
        }

        /// <summary>
        /// 인터페이스 리셋 시도
        /// </summary>
        public bool TryResetInterface()
        {
            try
            {
                if (_usbInterface == null)
                {
                    Debug.WriteLine("USB 인터페이스가 null이므로 인터페이스 리셋을 건너뜁니다.");
                    return false;
                }

               Debug.WriteLine("USB 인터페이스 리셋 시도 중...");

                // 인터페이스 해제 후 재설정
                _usbConnection.ReleaseInterface(_usbInterface);
                Thread.Sleep(MediumDelay);
                bool claimed = _usbConnection.ClaimInterface(_usbInterface, true);

               Debug.WriteLine($"인터페이스 리셋 결과: {(claimed ? "성공" : "실패")}");
                return claimed;
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"인터페이스 리셋 중 오류 발생: {ex}");
                return false;
            }
        }

        /// <summary>
        /// 디바이스 리셋 명령 전송 시도
        /// </summary>
        public bool TrySendDeviceResetCommand()
        {
            try
            {
               Debug.WriteLine("디바이스 리셋 명령 전송 시도 중...");

                // USB 디바이스 리셋 컨트롤 전송
                // 주의: 모든 장치가 이 명령을 지원하지는 않음
                const int UsbTypeStandard = 0x00;
                const int UsbRecipientDevice = 0x00;
                const int UsbReqSetFeature = 0x03;
                const int UsbDeviceReset = 0; // 디바이스별로 다를 수 있음

                int result = _usbConnection.ControlTransfer(
                    (UsbAddressing)(UsbTypeStandard | UsbRecipientDevice),
                    UsbReqSetFeature,
                    UsbDeviceReset,
                    0,
                    null, 0, _timeout);

                bool success = result >= 0;
                Debug.WriteLine($"디바이스 리셋 명령 결과:{(success ? "성공" : "실패")}");

                return success;
            }
            catch (Exception ex)
            {
                Debug.WriteLine("디바이스 리셋 명령 전송 중 오류 발생");
                return false;
            }
        }

        /// <summary>
        /// 특정 엔드포인트의 HALT 상태 해제 시도
        /// </summary>
        private bool TryClearHaltFeature(UsbEndpoint endpoint)
        {
            if (endpoint == null)
                return false;

            const int UsbFeatureEndpointHalt = 0;
            const int UsbTypeStandard = 0x00;
            const int UsbRecipientEndpoint = 0x02;
            const int UsbReqClearFeature = 0x01;

            int result = _usbConnection.ControlTransfer(
                (UsbAddressing)(UsbTypeStandard | UsbRecipientEndpoint),
                UsbReqClearFeature,
                UsbFeatureEndpointHalt,
                (int)endpoint.Address,
                null, 0, _timeout);

            return result >= 0;
        }

        /// <summary>
        /// 지연 후 통신 테스트 수행
        /// </summary>
        private bool DelayAndTest(Func<bool> communicationTest, int delayMs)
        {
            Thread.Sleep(delayMs);
            try
            {
                return communicationTest();
            }
            catch (Exception ex)
            {
               Debug.WriteLine($"통신 테스트 실패: {ex}");
                return false;
            }
        }

        /// <summary>
        /// 복구 시도 카운터 리셋
        /// </summary>
        public void ResetAttemptCounter()
        {
            _recoveryAttempts = 0;
        }
    }
}
