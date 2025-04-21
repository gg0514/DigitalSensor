using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;
using DigitalSensor.Extensions;
using DigitalSensor.ViewModels;
using System.Diagnostics;
using Newtonsoft.Json.Linq;
using Avalonia.Platform;
using Avalonia;
using System.IO;

namespace DigitalSensor.Services;


public class ModbusService 
{
    public event Action<ModbusHandler>? ModbusHandlerAttached;
    public event Action<ModbusHandler>? ModbusHandlerDetached;

    // 생성자에서 초기화
    private readonly IUsbService _usbService;
    private readonly NotificationService _notificationService;

    // 전파이벤트에서 초기화 
    private IModbusSerialMaster? _modbusMaster = default;
    private ModbusHandler? _modbusHandler = default;                    // ModbusHandler 인스턴스 생성

    public SerialConn SerialConn { get; set; } = new();                 // 기본값 부여 


    public ModbusService(IUsbService usbService)
    {
        _usbService = usbService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();

        // USB Device 구독 등록
        _usbService.UsbPermissionGranted += OnUSBPermissionGranted;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    private void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        CloseModbus();

        ModbusHandlerDetached?.Invoke(null);
    }

    private async void OnUSBPermissionGranted(UsbDeviceInfo deviceInfo)
    {
        try
        {
            await OpenModbus(deviceInfo);
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error: {ex.Message}");
        }
    }

    public async Task<bool> OpenModbus(UsbDeviceInfo deviceInfo)
    {
        _modbusMaster= OpenModbus(deviceInfo.DeviceId);

        if (IsOpen())
        {
            _modbusHandler= new ModbusHandler(_modbusMaster, deviceInfo);
            await _modbusHandler.LoadSlaveId();

            // 상위로 이벤트 전파 
            ModbusHandlerAttached?.Invoke(_modbusHandler);

            return true;
        }
        else
        {
            _notificationService.ShowMessage("Modbus Device Open Failed", "");
        }

        return false;
    }

    public void CloseModbus()
    {
        if (IsOpen())
        {
            _modbusHandler = null;

            _usbService.Close();
            _modbusMaster?.Dispose();
            _modbusMaster = null;
        }
    }

    public IModbusSerialMaster OpenModbus(int deviceId)
    {
        if (OpenDevice(deviceId))
        {
            // Desktop, Android 공용
            var adapter = new UsbSerialAdapter(_usbService);
            return ModbusSerialMaster.CreateRtu(adapter);
        }
        else
        {
            _notificationService.ShowMessage("USB Device Open Failed", "");
        }

        return null;
    }

    // Desktop, Android 공용
    // Desktop인 경우, ComPort 숫자
    public bool OpenDevice(int deviceId)
    {
        int baudRate = int.Parse(SerialConn.BaudRate);
        byte dataBits = byte.Parse(SerialConn.DataBits);
        byte stopBits = byte.Parse(SerialConn.StopBits);
        byte parity = byte.Parse(SerialConn.Parity); // 0 = None, 1 = Odd, 2 = Even

        return _usbService.Open(deviceId, baudRate, dataBits, stopBits, parity);
    }

    public bool IsOpen()
    {
        return _modbusMaster != null;
    }
}

