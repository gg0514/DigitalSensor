using DigitalSensor.Extensions;
using DigitalSensor.Modbus;
using DigitalSensor.Models;
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

    private UsbDeviceInfo _usbDeviceInfo = default;
    public SerialConn SerialConn { get; set; } = new();                 // 기본값 부여 


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

    private void OnUSBPermissionGranted(UsbDeviceInfo deviceInfo)
    {
        _usbDeviceInfo = deviceInfo;

        try
        {
            //ResetModbusCommunication();
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error: {ex.Message}");
        }

        // 센서 진단
        //callHealthCheck();
    }

    private void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        //CloseModbus();

        //ModbusHandlerDetached?.Invoke(null);
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