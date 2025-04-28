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
    bool IsOpen();
    Task<bool> Open();
    Task Close();

    Task<int> Initialize();
    Task<SensorInfo> GetSensorInfoAsync();
    Task<SensorData> GetSensorDataAsync();

    Task<int> GetTypeAsync();
    Task<float> GetValueAsync();
    Task<float> GetMVAsync();
    Task<float> GetTemperatureAsync();
}


public class SensorService : ISensorService
{
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


    public async Task<int> Initialize()
    {
        while(true)
        {
            bool bOK = await _modbusService.Initialize();

            if (bOK)
            {
                break;
            }

            await Task.Delay(1000);
        }

        return _modbusService.SlaveId;
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
        float value = await _modbusService.ReadSensorValue();
        float mv = await _modbusService.ReadSensorMV();
        float temperature = await _modbusService.ReadTempValue();

        var data = new SensorData
        {
            Timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
            Value = value,
            Mv = mv,
            Temperature = temperature
        };

        return data;
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
}