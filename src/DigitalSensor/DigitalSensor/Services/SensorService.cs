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
    // 이벤트 버블링
    event Action? SensorAttached;
    event Action? SensorDetached;

    Task<SensorInfo> GetSensorInfoAsync();
    Task<SensorData> GetSensorDataAsync();
}


public class SensorService : ISensorService
{
    public event Action? SensorAttached;
    public event Action? SensorDetached;


    private readonly IUsbService    _usbService;
    private readonly ModbusService  _modbusService;
    private readonly NotificationService _notificationService;


    // for Design
    public SensorService()
    {
    }

    // for Runtime
    public SensorService(IUsbService usbService, ModbusService modbusService)
    {
        // 이벤트구독용
        _usbService = usbService;
        _modbusService = modbusService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();

        // USB Device 구독 등록
        _usbService.UsbPermissionGranted += OnUSBPermissionGranted;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    private async void OnUSBPermissionGranted(UsbDeviceInfo deviceInfo)
    {
        SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

        try
        {
            int deviceId = deviceInfo.DeviceId;
            bool isOpen = await Open(deviceId);

            if (isOpen)
            {
                _notificationService.ShowMessage("정보", $"Device {deviceId} opened successfully.");

                int slaveId = await GetSlaveID();

                await UiDispatcherHelper.RunOnUiThreadAsync( async() =>
                {
                    vm.SlaveID = slaveId;
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

    private async Task<bool> Open(int deviceId)
    {
        try
        {
            await _modbusService.Open(deviceId);
            return true;
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error opening device: {ex.Message}");
            return false;
        }
    }

    private async Task<int> GetSlaveID()
    {
        ushort[] values = await _modbusService.ReadSlaveId();
        return  values[0];
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

        return await Task.FromResult(data);
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

        return await Task.FromResult(data);
    }
}