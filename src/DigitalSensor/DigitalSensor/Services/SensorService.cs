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

    private ModbusHandler _modbusHandler= default;


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
        _modbusService.ModbusHandlerAttached += OnModbusHandlerAttached;
        _modbusService.ModbusHandlerDetached += OnModbusHandlerDetached;
    }

    private async void OnModbusHandlerAttached(ModbusHandler handler)
    {
        _modbusHandler = handler;

        int slaveID = handler.SlaveId;
        string productName = handler.GetProductName();

        _notificationService.ShowMessage("ModbusHandler Attached", $"{slaveID}:{productName}");
    }

    private void OnModbusHandlerDetached(ModbusHandler modbusInfo)
    {
        _notificationService.ShowMessage("ModbusHandler Detached", "");
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