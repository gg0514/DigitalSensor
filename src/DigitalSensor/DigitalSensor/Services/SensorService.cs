using DigitalSensor.Extensions;
using DigitalSensor.Modbus;
using DigitalSensor.Models;
using DigitalSensor.ViewModels;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;

namespace DigitalSensor.Services;

public interface ISensorService
{
    event Action SensorAttached;
    event Action SensorDetached;

    bool IsOpen();
    Task<bool> Open();
    Task Close();

    Task<int> InitSensor();
    Task<SensorInfo> GetSensorInfoAsync();
    Task<SensorData> GetSensorDataAsync();
    Task<int> GetCalibStatusAsync();

    Task<int> GetTypeAsync();
    Task<float> GetValueAsync();
    Task<float> GetMVAsync();
    Task<float> GetTemperatureAsync();

    Task SetCalibAbortAsync();
    Task SetCalibZeroAsync();
    Task SetCalib1PSampleAsync();
    Task SetCalib2PBufferAsync(int order);
}


public class SensorService : ISensorService
{
    public event Action SensorAttached;
    public event Action SensorDetached;

    private readonly IUsbService    _usbService;
    private readonly IModbusService  _modbusService;
    private readonly NotificationService _notificationService;

    private int _deviceId = 0;
    private bool _isOpen = false;

    // for Design
    public SensorService()
    {
    }

    // for Runtime
    public SensorService(IUsbService usbService, IModbusService modbusService, NotificationService notificationService)
    {
        // 이벤트구독용
        _usbService = usbService;
        _modbusService = modbusService;
        //_notificationService = App.GlobalHost.GetService<NotificationService>();
        _notificationService = notificationService;

        // USB Device 구독 등록
        _usbService.UsbPermissionGranted += OnUSBPermissionGranted;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    public bool IsOpen()
    {
        return _isOpen;
    }

    private async void OnUSBPermissionGranted(UsbDeviceInfo deviceInfo)
    {
        SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

        try
        {
            _deviceId = deviceInfo.DeviceId;
            _isOpen = await Open();

            if (_isOpen)
            {
                SensorAttached?.Invoke();

                _notificationService.ShowMessage("정보", $"Device {_deviceId} opened successfully.");
                Debug.WriteLine($"Device {_deviceId}:{deviceInfo.ProductName} opened successfully");

                await UiDispatcherHelper.RunOnUiThreadAsync( async () =>
                {
                    vm.UsbDevice = deviceInfo;
                });
            }
        }
        catch(Exception ex)
        {
            Debug.WriteLine($"Error opening device: {ex.Message}");
            _notificationService.ShowMessage("정보", $"Error opening device: {ex.Message}");
            return;
        }

        // 센서 진단
        //callHealthCheck();
    }



    private async void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        await _modbusService.Close();
        _notificationService.ShowMessage("정보", $"Device closed.");

        SensorDetached?.Invoke();       
    }

    public async Task<bool> Open()
    {
        try
        {
            await _modbusService.Open(_deviceId);
            return true;
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error opening device: {ex.Message}");
            return false;
        }
    }
    public async Task Close()
    {
        try
        {
            await _modbusService.Close();
            _isOpen = false;
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error closing device: {ex.Message}");
        }
    }


    public async Task<int> InitSensor()
    {
        int slaveId = -1;

        for (int i = 0; i < 5; i++)
        {
            bool bOK = await _modbusService.Initialize();

            if (bOK)
            {
                slaveId = _modbusService.SlaveId;
                break;
            }

            await Task.Delay(1000);
        }

        return slaveId;
    }

    public async Task<SensorInfo> GetSensorInfoAsync()
    {
        int type = await _modbusService.ReadSensorType();
        string serial = await _modbusService.ReadSensorSerial();

        var data = new SensorInfo
        {
            Type = (SensorType)type,
            Serial = serial // 예시로 고정된 시리얼 번호
        };

        return data;
    }


    public async Task<SensorData> GetSensorDataAsync()
    {
        SensorData data = await _modbusService.ReadSensorData();

        return new SensorData
        {
            Timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
            Value = data.Value,
            Mv = data.Mv,
            Temperature = data.Temperature
        };
    }


    public async Task<int> GetTypeAsync()
    {
        int type = await _modbusService.ReadSensorType();
        return type;
    }

    public async Task<float> GetValueAsync()
    {
        float value = await _modbusService.ReadSensorValue();
        return value;
    }

    public async Task<float> GetMVAsync()
    {
        float mv = await _modbusService.ReadSensorMV();
        return mv;
    }

    public async Task<float> GetTemperatureAsync()
    {
        float temperature = await _modbusService.ReadTempValue();
        return temperature;
    }

    public async Task<int> GetCalibStatusAsync()
    {
        int status = await _modbusService.ReadCalibStatus();
        return status;
    }

    public async Task SetCalibZeroAsync()
    {
        if (!_isOpen)
            return;

        await _modbusService.WriteCalibZero(0);
    }

    public async Task SetCalibAbortAsync()
    {
        if (!_isOpen)
            return;

        await _modbusService.WriteCalibAbort(0);
    }
    
    public async Task SetCalib1PSampleAsync()
    {
        if (!_isOpen)
            return;

        await _modbusService.WriteCalib1pSample(0);
    }

    public async Task SetCalib2PBufferAsync(int order)
    {
        if (!_isOpen)
            return;

        await _modbusService.WriteCalib2pBuffer((ushort)order);
    }
}