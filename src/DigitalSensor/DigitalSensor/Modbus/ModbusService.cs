using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;
using DigitalSensor.Modbus;
using DigitalSensor.Extensions;
using DigitalSensor.ViewModels;
using System.Diagnostics;
using System.Threading;
using Newtonsoft.Json.Linq;
using Avalonia.Platform;
using Avalonia;
using System.IO;
using HarfBuzzSharp;

namespace DigitalSensor.Services;



//*************************************************
// MODBUS RTU 의 안정적인 서비스 제공 목적    
//*************************************************

public class ModbusService 
{
    public event Action<ModbusHandler>? ModbusHandlerAttached;
    public event Action<ModbusHandler>? ModbusHandlerDetached;

    // 생성자에서 초기화
    private readonly IUsbService _usbService;
    private readonly NotificationService _notificationService;

    private IModbusSerialMaster? _modbusRTU = default;
    private UsbDeviceInfo _usbDeviceInfo = default;
    private ModbusHandler _modbusHandler = default;                    

    public SerialConn SerialConn { get; set; } = new();                 // 기본값 부여 


    public ModbusService(IUsbService usbService)
    {
        // 1) source
        _usbService = usbService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();

        // USB Device 구독 등록
        _usbService.UsbPermissionGranted += OnUSBPermissionGranted;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    public async void ResetModbusCommunication()
    {
        CloseModbus();

        // 2) intermediate 
        _modbusRTU = CreateModbusRTU(_usbDeviceInfo.DeviceId);
        Debug.WriteLine("****** CreateModbusRTU()");

        // 3) target
        _modbusHandler = new ModbusHandler(_modbusRTU, _usbDeviceInfo);
        Debug.WriteLine("****** ModbusHandler()");


        _modbusHandler.TestConnection();
        Debug.WriteLine("****** MODBUS Handler TEST OK!!");

        // 새로운 Handler가 생성됨을 알림.
        await _modbusHandler.LoadSlaveId();
        ModbusHandlerAttached?.Invoke(_modbusHandler);

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
        CloseModbus();

        ModbusHandlerDetached?.Invoke(null);
    }


    public IModbusSerialMaster CreateModbusRTU(int deviceId)
    {
        if (OpenUSBDevice(deviceId))
        {
            if(_usbService.IsConnection())
            {
                // Desktop, Android 공용
                var adapter = new UsbSerialAdapter(_usbService);
                return ModbusSerialMaster.CreateRtu(adapter);
            }
        }
        else
        {
            _notificationService.ShowMessage("Modbus RTU Create Failed", "");
        }

        return null;
    }

    // Desktop, Android 공용
    // Desktop인 경우, ComPort 숫자
    public bool OpenUSBDevice(int deviceId)
    {
        int baudRate = int.Parse(SerialConn.BaudRate);
        byte dataBits = byte.Parse(SerialConn.DataBits);
        byte stopBits = byte.Parse(SerialConn.StopBits);
        byte parity = byte.Parse(SerialConn.Parity); // 0 = None, 1 = Odd, 2 = Even

        bool bOpen=_usbService.Open(deviceId, baudRate, dataBits, stopBits, parity);
        return bOpen;
    }



    public void CloseModbus()
    {
        if (_modbusRTU != null)
        {
            _modbusHandler = null;

            _usbService.Close();
            _modbusRTU?.Dispose();
            _modbusRTU = null;

            Debug.WriteLine("****** CloseModbus()");
        }
    }


    private async void callHealthCheck()
    {
        try
        {
            await SensorHealthCheck();
        }
        catch (Exception ex)
        {
            bool recovered = RecoverConnection();

            if (!recovered)
            {
                Debug.WriteLine("모든 복구 시도 실패, ModbusSerialMaster 재생성 필요");

                //Thread.Sleep(3000);
                //ResetModbusCommunication();
            }
        }
    }



    public async Task SensorHealthCheck()
    {
        for (int i = 0; i < 3; i++)
        {
            //byte slaveId = (byte)(await _modbusHandler.ReadSlaveId())[0];    // 0x14
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

    public bool RecoverConnection()
    {
        bool recovered = _usbService.TryRecover(() => {
            try
            {
                _modbusHandler.TestConnection();
                return true;
            }
            catch
            {
                return false;
            }
        });

        return recovered;
    }


}

