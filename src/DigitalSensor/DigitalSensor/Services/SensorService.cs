using DigitalSensor.Extensions;
using DigitalSensor.Modbus;
using DigitalSensor.Models;
using System;
using System.Collections.Generic;
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

    private readonly NotificationService    _notificationService;
    private readonly ModbusService          _modbusService;

    private ModbusHandler _modbusHandler= default;

    //private readonly Random _random = new();

    // for Design
    public SensorService()
    {
    }

    // for Runtime
    public SensorService(ModbusService modbusService)
    {
        // 이벤트구독용
        _modbusService = modbusService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();

        // Modbus Handler 구독 등록
        _modbusService.ModbusHandlerAttached += OnModbusHandlerAttached;
        _modbusService.ModbusHandlerDetached += OnModbusHandlerDetached;
    }

    private async void OnModbusHandlerAttached(ModbusHandler handler)
    {
        _modbusHandler = handler;

        int slaveID = handler.SlaveId;
        string productName = handler.GetProductName();

        _notificationService.ShowMessage("ModbusHandler Attached", $"{slaveID}:{productName}");

        // Sensor Attached 통지
        SensorAttached?.Invoke();
    }

    private void OnModbusHandlerDetached(ModbusHandler modbusInfo)
    {
        // Sensor Detached 통지
        SensorDetached?.Invoke();

        _notificationService.ShowMessage("ModbusHandler Detached", "");
        //_notificationService.ShowMessage("USB Device Detached", $"Device ID: {deviceInfo.DeviceId}");
    }


    public async Task<SensorInfo> GetSensorInfoAsync()
    {
        int type = await _modbusHandler.ReadSensorType();
        string serial = await _modbusHandler.ReadSensorSerial();

        var data = new SensorInfo
        {
            Type = (SensorType)type,
            Serial = serial // 예시로 고정된 시리얼 번호
        };

        return await Task.FromResult(data);
    }


    public async Task<SensorData> GetSensorDataAsync()
    {
        float value     = await _modbusHandler.ReadSensorValue();
        float mv        = await _modbusHandler.ReadSensorMV();
        float temperature = await _modbusHandler.ReadTempValue();

        var data = new SensorData
        {
            Timestamp   = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
            Value       = value,
            Mv          = mv,
            Temperature = temperature
        };

        return await Task.FromResult(data);
    }
}