// AvaloniaUsbSerial.Android 프로젝트: SerialService.cs
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Android.App;
using Android.Content;
using Android.Hardware.Usb;
using Android.Runtime;
using AvaloniaUsbSerial.Services;
using Hoho.Android.UsbSerial.Driver;
using Hoho.Android.UsbSerial.Extensions;

namespace AvaloniaUsbSerial.Android
{
    public class SerialService : ISerialService
    {
        private const string ACTION_USB_PERMISSION = "com.avaloniaUsbSerial.USB_PERMISSION";
        
        private UsbManager _usbManager;
        private UsbSerialPort _port;
        private UsbDeviceConnection _connection;
        private CancellationTokenSource _cancellationTokenSource;
        private bool _isConnected;
        
        public event EventHandler<byte[]> DataReceived;
        public event EventHandler<bool> ConnectionStatusChanged;

        public bool IsConnected => _isConnected;

        public SerialService()
        {
            _usbManager = (UsbManager)Application.Context.GetSystemService(Context.UsbService);
        }

        public async Task<List<SerialDeviceInfo>> GetAvailableDevicesAsync()
        {
            var devices = new List<SerialDeviceInfo>();
            
            // USB 장치 목록 가져오기
            var availableDrivers = UsbSerialProber.GetDefaultProber().FindAllDrivers(_usbManager);
            
            foreach (var driver in availableDrivers)
            {
                var device = driver.Device;
                var deviceInfo = new SerialDeviceInfo
                {
                    DeviceId = device.DeviceId.ToString(),
                    DisplayName = $"{device.ManufacturerName} {device.ProductName}",
                    Description = $"VID: {device.VendorId}, PID: {device.ProductId}"
                };
                
                devices.Add(deviceInfo);
            }
            
            return devices;
        }

        public async Task<bool> ConnectAsync(string deviceId)
        {
            try
            {
                // 장치 ID로 USB 드라이버 찾기
                var availableDrivers = UsbSerialProber.GetDefaultProber().FindAllDrivers(_usbManager);
                UsbSerialDriver driver = null;
                
                foreach (var d in availableDrivers)
                {
                    if (d.Device.DeviceId.ToString() == deviceId)
                    {
                        driver = d;
                        break;
                    }
                }
                
                if (driver == null)
                {
                    return false;
                }
                
                // USB 장치 연결
                var device = driver.Device;
                
                // 권한 요청
                if (!_usbManager.HasPermission(device))
                {
                    var permissionIntent = PendingIntent.GetBroadcast(
                        Application.Context, 
                        0, 
                        new Intent(ACTION_USB_PERMISSION), 
                        PendingIntentFlags.Mutable);
                    
                    _usbManager.RequestPermission(device, permissionIntent);
                    
                    // 권한 요청 후 사용자 응답 대기 (실제 구현에서는 BroadcastReceiver로 처리)
                    await Task.Delay(1000);
                    
                    if (!_usbManager.HasPermission(device))
                    {
                        return false;
                    }
                }
                
                // 연결 설정
                _connection = _usbManager.OpenDevice(device);
                if (_connection == null)
                {
                    return false;
                }
                
                // 첫 번째 포트 사용 (대부분의 장치는 1개의 포트만 가짐)
                _port = driver.Ports[0];
                _port.Open(_connection);
                _port.SetParameters(115200, 8, UsbSerialStopBits.One, UsbSerialParity.None);
                
                // 데이터 수신 시작
                _cancellationTokenSource = new CancellationTokenSource();
                _ = ReadDataAsync(_cancellationTokenSource.Token);
                
                _isConnected = true;
                ConnectionStatusChanged?.Invoke(this, _isConnected);
                
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"연결 오류: {ex.Message}");
                await DisconnectAsync();
                return false;
            }
        }

        public async Task DisconnectAsync()
        {
            try
            {
                _cancellationTokenSource?.Cancel();
                
                if (_port != null)
                {
                    _port.Close();
                    _port = null;
                }
                
                _connection?.Close();
                _connection = null;
                
                _isConnected = false;
                ConnectionStatusChanged?.Invoke(this, _isConnected);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"연결 해제 오류: {ex.Message}");
            }
        }

        public async Task<bool> SendDataAsync(byte[] data)
        {
            if (!_isConnected || _port == null)
            {
                return false;
            }
            
            try
            {
                await Task.Run(() => _port.Write(data, 1000));
                return true;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"데이터 전송 오류: {ex.Message}");
                return false;
            }
        }

        public async Task<byte[]> ReceiveDataAsync(int timeout = 1000)
        {
            if (!_isConnected || _port == null)
            {
                return new byte[0];
            }
            
            try
            {
                var buffer = new byte[4096];
                int bytesRead = await Task.Run(() => _port.Read(buffer, timeout));
                
                if (bytesRead > 0)
                {
                    var result = new byte[bytesRead];
                    Array.Copy(buffer, result, bytesRead);
                    return result;
                }
                
                return new byte[0];
            }
            catch (Exception ex)
            {
                Console.WriteLine($"데이터 수신 오류: {ex.Message}");
                return new byte[0];
            }
        }

        private async Task ReadDataAsync(CancellationToken cancellationToken)
        {
            byte[] buffer = new byte[4096];
            
            while (!cancellationToken.IsCancellationRequested && _isConnected)
            {
                try
                {
                    int bytesRead = await Task.Run(() => _port.Read(buffer, 100), cancellationToken);
                    
                    if (bytesRead > 0)
                    {
                        var data = new byte[bytesRead];
                        Array.Copy(buffer, data, bytesRead);
                        
                        DataReceived?.Invoke(this, data);
                    }
                    
                    await Task.Delay(10, cancellationToken);
                }
                catch (OperationCanceledException)
                {
                    break;
                }
                catch (Exception ex)
                {
                    Console.WriteLine($"데이터 수신 스레드 오류: {ex.Message}");
                    await Task.Delay(500, cancellationToken);
                }
            }
        }
    }
}
