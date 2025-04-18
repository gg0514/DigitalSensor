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

        // Modbus Device 구독 등록
        _modbusService.ModbusDeviceAttached += OnModbusDeviceAttached;
        _modbusService.ModbusDeviceDetached += OnModbusDeviceDetached;
    }

    private async void OnModbusDeviceAttached(ModbusDeviceInfo modbusInfo)
    {
        int slaveID = modbusInfo.SlaveId;
        //_notificationService.ShowMessage("Slave ID", $"{slaveID}");

        _notificationService.ShowMessage("USB Device Attached", $"{slaveID}:{modbusInfo.ProductName}");
    }

    private void OnModbusDeviceDetached(ModbusDeviceInfo modbusInfo)
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