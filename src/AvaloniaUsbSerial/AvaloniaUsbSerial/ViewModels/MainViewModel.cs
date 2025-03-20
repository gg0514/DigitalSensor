// AvaloniaUsbSerial 프로젝트: MainViewModel.cs
using System;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using System.Windows.Input;
using AvaloniaUsbSerial.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace AvaloniaUsbSerial.ViewModels
{
    public partial class MainViewModel : ViewModelBase
    {
        private readonly ISerialService _serialService;
        private SerialDeviceInfo        _selectedDevice;
        private string                  _statusMessage;
        private bool                    _isConnected;
        private string                  _receivedData;
        private string                  _dataToSend;

        public MainViewModel(ISerialService serialService)
        {
            _serialService = serialService;
            Devices = new ObservableCollection<SerialDeviceInfo>();

            // 커맨드 초기화
            RefreshDevicesCommand = new AsyncRelayCommand(RefreshDevicesAsync);
            ConnectCommand = new AsyncRelayCommand(ConnectAsync, CanConnect);
            DisconnectCommand = new AsyncRelayCommand(DisconnectAsync, () => IsConnected);
            SendDataCommand = new AsyncRelayCommand(SendDataAsync, () => IsConnected);

            // 이벤트 핸들러 등록
            _serialService.DataReceived += SerialService_DataReceived;
            _serialService.ConnectionStatusChanged += SerialService_ConnectionStatusChanged;
        }

        // 장치 목록
        public ObservableCollection<SerialDeviceInfo> Devices { get; }

        // 선택된 장치
        public SerialDeviceInfo SelectedDevice
        {
            get => _selectedDevice;
            set => SetProperty(ref _selectedDevice, value);
        }

        // 상태 메시지
        public string StatusMessage
        {
            get => _statusMessage;
            set => SetProperty(ref _statusMessage, value);
        }

        // 연결 상태
        public bool IsConnected
        {
            get => _isConnected;
            set => SetProperty(ref _isConnected, value);
        }

        // 수신된 데이터
        public string ReceivedData
        {
            get => _receivedData;
            set => SetProperty(ref _receivedData, value);
        }

        // 전송할 데이터
        public string DataToSend
        {
            get => _dataToSend;
            set => SetProperty(ref _dataToSend, value);
        }

        // 커맨드
        public IAsyncRelayCommand RefreshDevicesCommand { get; }
        public IAsyncRelayCommand ConnectCommand { get; }
        public IAsyncRelayCommand DisconnectCommand { get; }
        public IAsyncRelayCommand SendDataCommand { get; }

        // 장치 목록 새로고침
        private async Task RefreshDevicesAsync()
        {
            try
            {
                StatusMessage = "장치 목록을 가져오는 중...";
                Devices.Clear();

                var devices = await _serialService.GetAvailableDevicesAsync();
                foreach (var device in devices)
                {
                    Devices.Add(device);
                }

                StatusMessage = $"{Devices.Count}개의 장치를 찾았습니다.";
            }
            catch (Exception ex)
            {
                StatusMessage = $"장치 목록 가져오기 실패: {ex.Message}";
            }
        }

        // 연결 가능 여부 확인
        private bool CanConnect()
        {
            return SelectedDevice != null && !IsConnected;
        }

        // 장치 연결
        private async Task ConnectAsync()
        {
            if (SelectedDevice == null)
            {
                StatusMessage = "장치를 선택해주세요.";
                return;
            }

            try
            {
                StatusMessage = $"{SelectedDevice.DisplayName}에 연결 중...";
                bool result = await _serialService.ConnectAsync(SelectedDevice.DeviceId);

                if (result)
                {
                    IsConnected = true;
                    StatusMessage = $"{SelectedDevice.DisplayName}에 연결되었습니다.";
                }
                else
                {
                    StatusMessage = "연결에 실패했습니다.";
                }
            }
            catch (Exception ex)
            {
                StatusMessage = $"연결 오류: {ex.Message}";
            }
        }

        // 장치 연결 해제
        private async Task DisconnectAsync()
        {
            try
            {
                await _serialService.DisconnectAsync();
                IsConnected = false;
                StatusMessage = "연결이 해제되었습니다.";
            }
            catch (Exception ex)
            {
                StatusMessage = $"연결 해제 오류: {ex.Message}";
            }
        }

        // 데이터 전송
        private async Task SendDataAsync()
        {
            if (string.IsNullOrEmpty(DataToSend))
            {
                StatusMessage = "전송할 데이터를 입력해주세요.";
                return;
            }

            try
            {
                byte[] data = System.Text.Encoding.UTF8.GetBytes(DataToSend);
                bool result = await _serialService.SendDataAsync(data);

                if (result)
                {
                    StatusMessage = $"{data.Length} 바이트 전송 완료";
                    DataToSend = string.Empty;
                }
                else
                {
                    StatusMessage = "데이터 전송 실패";
                }
            }
            catch (Exception ex)
            {
                StatusMessage = $"전송 오류: {ex.Message}";
            }
        }

        // 데이터 수신 이벤트 핸들러
        private void SerialService_DataReceived(object sender, byte[] data)
        {
            string receivedText = System.Text.Encoding.UTF8.GetString(data);
            ReceivedData += receivedText;
        }

        // 연결 상태 변경 이벤트 핸들러
        private void SerialService_ConnectionStatusChanged(object sender, bool isConnected)
        {
            IsConnected = isConnected;
            StatusMessage = isConnected ? "장치가 연결되었습니다." : "장치 연결이 해제되었습니다.";
        }
    }
}