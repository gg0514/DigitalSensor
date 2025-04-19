using DigitalSensor.Extensions;
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

        // 센서 진단
        await SensorHealthCheck();

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
        await Task.Delay(50);

        string serial = await _modbusHandler.ReadSensorSerial();
        await Task.Delay(50);

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
        await Task.Delay(50); 

        float mv        = await _modbusHandler.ReadSensorMV();
        await Task.Delay(50);

        float temperature = await _modbusHandler.ReadTempValue();
        await Task.Delay(50);

        var data = new SensorData
        {
            Timestamp   = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
            Value       = value,
            Mv          = mv,
            Temperature = temperature
        };

        return await Task.FromResult(data);
    }


    public async Task SensorHealthCheck()
    {
        for(int i=0; i < 30; i++)
        {
            byte slaveId = (byte)(await _modbusHandler.ReadSlaveId())[0];    // 0x14
            int type = await _modbusHandler.ReadSensorType();                // 0x06
            string serial = await _modbusHandler.ReadSensorSerial();         // 0x08

            float value = await _modbusHandler.ReadSensorValue();            // 0x00
            float temperature = await _modbusHandler.ReadTempValue();        // 0x02
            float mv = await _modbusHandler.ReadSensorMV();                  // 0x04

            float factor = await _modbusHandler.ReadSensorFactor();          // 0x0B
            float offset = await _modbusHandler.ReadSensorOffset();          // 0x0D
            float sample = await _modbusHandler.ReadCalib1pSample();         // 0x0F
            ushort calib = await _modbusHandler.ReadCalibStatus();           // 0x07
        }
    }
}