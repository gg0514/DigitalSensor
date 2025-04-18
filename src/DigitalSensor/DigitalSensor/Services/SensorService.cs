using DigitalSensor.Extensions;
using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DigitalSensor.Services;

public interface ISensorService
{
    Task<SensorInfo> GetSensorInfoAsync();
    Task<SensorData> GetSensorDataAsync();
}


public class SensorService : ISensorService
{
    private readonly NotificationService    _notificationService;
    private readonly ModbusService          _modbusService;


    private readonly Random _random = new();

    // for Design
    public SensorService()
    {
    }

    // for Runtime
    public SensorService(ModbusService modbusService)
    {
        _modbusService = modbusService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();

        // 구독 등록
        _modbusService.UsbDeviceAttached += OnUSBDeviceAttached;
        _modbusService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    private async void OnUSBDeviceAttached(UsbDeviceInfo deviceInfo)
    {
        // Handle USB device attached event
        //_notificationService.ShowMessage("USB Device Attached", $"{deviceInfo.ProductName}");
    }

    private void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        _notificationService.ShowMessage("USB Device Detached", "");
        //_notificationService.ShowMessage("USB Device Detached", $"Device ID: {deviceInfo.DeviceId}");
    }


    public Task<SensorInfo> GetSensorInfoAsync()
    {
        var data = new SensorInfo
        {
            Type = SensorType.None,
            Serial = "1234567890ABCDEF" // 예시로 고정된 시리얼 번호
        };


        if(_modbusService.IsOpen())
        {

        }

        return Task.FromResult(data);
    }


    public Task<SensorData> GetSensorDataAsync()
    {
        var data = new SensorData
        {
            Timestamp   = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
            Value       = (float)_random.NextDouble(),
            Mv          = (float)_random.NextDouble(),
            Temperature = (float)_random.NextDouble()
        };

        return Task.FromResult(data);
    }

}