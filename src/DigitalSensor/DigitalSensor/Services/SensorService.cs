using DigitalSensor.Modbus;
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
    public SensorService(NotificationService notificationService, IUsbService usbService)
    {
        _notificationService = notificationService;
        _modbusService = new ModbusService(usbService);

        // 구독 등록
        _modbusService.UsbDeviceAttached += OnUSBDeviceAttached;
        _modbusService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    private void OnUSBDeviceAttached(UsbDeviceInfo deviceInfo)
    {
        // Handle USB device attached event
        Console.WriteLine($"[SensorService] USB device attached: {deviceInfo.DeviceId}");

        _notificationService.ShowMessage("USB Device Attached", $"Device ID: {deviceInfo.DeviceId}");
    }
    private void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        _notificationService.ShowMessage("USB Device Detached", "");

        //// Handle USB device attached event
        //Console.WriteLine($"[SensorService] USB device detached: {deviceInfo.DeviceId}");

        //_notificationService.ShowMessage("USB Device Detached", $"Device ID: {deviceInfo.DeviceId}");
    }


    public Task<SensorInfo> GetSensorInfoAsync()
    {
        var data = new SensorInfo
        {
            Type = SensorType.None,
            Serial = "1234567890ABCDEF" // 예시로 고정된 시리얼 번호
        };

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