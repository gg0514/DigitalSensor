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
using static System.Runtime.InteropServices.JavaScript.JSType;
using System.Diagnostics;


namespace DigitalSensor.Services;


public interface IModbusService
{
    event Action<ModbusDeviceInfo> ModbusDeviceAttached;
    event Action<ModbusDeviceInfo> ModbusDeviceDetached;


    // SLAVE ID
    ushort ReadChgSlaveId();
    void WriteChgSlaveId(ushort value);

    // 센서 데이터
    float ReadSensorValue();                    // 40001
    float ReadTempValue();                      // 40003
    float ReadSensorMV();                       // 40005

    // 센서 정보
    ushort ReadSensorType();
    ushort[] ReadSensorSerial(); // 길이: 3 (UINT16)

    // SENSOR FACTOR
    float ReadSensorFactor();
    void WriteSensorFactor(float value);

    // SENSOR OFFSET
    float ReadSensorOffset();
    void WriteSensorOffset(float value);

    // 1P CALIBRATION
    float ReadCalib1pSample();
    void WriteCalib1pSample(float value);

    // 2P CALIBRATION
    void WriteCalib2pBuffer(ushort value);
    void WriteCalibZero(ushort value);

    // CALIBRATION STATUS
    void WriteCalibAbort(ushort value);
    ushort ReadCalibStatus();
}



public class ModbusService //: IModbusService
{
    // 이벤트 버블링
    public event Action<ModbusDeviceInfo>? ModbusDeviceAttached;
    public event Action<ModbusDeviceInfo>? ModbusDeviceDetached;

    // 생성자에서 초기화
    private readonly IUsbService _usbService;
    private readonly NotificationService _notificationService;

    // 전파이벤트에서 초기화 
    private UsbDeviceInfo? _usbDeviceInfo = default; 
    private IModbusSerialMaster? _modbusMaster = default;

    public SerialConn SerialConn { get; set; } = new();                 // 기본값 부여 


    public ModbusService(IUsbService usbService)
    {
        _usbService = usbService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();

        // USB Device 구독 등록
        _usbService.UsbDeviceAttached += OnUSBDeviceAttached;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    private void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        ModbusDeviceDetached?.Invoke(null);
    }

    private async void OnUSBDeviceAttached(UsbDeviceInfo deviceInfo)
    {
        _usbDeviceInfo = deviceInfo;

        try
        {
            await OpenModbus();
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error: {ex.Message}");
        }
    }

    private async Task OpenModbus()
    {
        _modbusMaster= OpenModbus(_usbDeviceInfo.DeviceId);

        if (IsOpen())
        {
            ushort[] result = await ReadSlaveID();
            int slaveID = result[0];  // 배열에서 필요한 값 꺼내기


            // 상위로 이벤트 전파 
            ModbusDeviceAttached?.Invoke(new ModbusDeviceInfo
            {
                DeviceId= _usbDeviceInfo.DeviceId,
                ProductName = _usbDeviceInfo.ProductName,
                SlaveId = slaveID,
            });
        }
        else
        {
            _notificationService.ShowMessage("Modbus Device Open Failed", "");
        }
    }


    public IModbusSerialMaster OpenModbus(int deviceId)
    {
        if (OpenDevice(deviceId))
        {
            var adapter = new UsbSerialAdapter(_usbService);
            return ModbusSerialMaster.CreateRtu(adapter);
        }
        else
        {
            _notificationService.ShowMessage("USB Device Open Failed", "");
        }

        return null;
    }



    public bool IsOpen()
    {
        return _modbusMaster != null;
    }

    public bool OpenDevice(int deviceId)
    {
        int baudRate = int.Parse(SerialConn.BaudRate);
        byte dataBits = byte.Parse(SerialConn.DataBits);
        byte stopBits = byte.Parse(SerialConn.StopBits);
        byte parity = byte.Parse(SerialConn.Parity); // 0 = None, 1 = Odd, 2 = Even

        return _usbService.Open(deviceId, baudRate, dataBits, stopBits, parity);
    }



    public async Task<ushort[]> ReadSlaveID()
    {
        byte slaveId = 250;
        ushort startAddress = 20;
        ushort numRegisters = 1;

        return await Task.Run(() => _modbusMaster.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
    }


    ////*********************************
    //// UsbSerialAdapter 버전 

    //public async Task<ushort[]> ReadUsbSerialAdapter(byte slaveId, ushort startAddress, ushort numRegisters)
    //{
    //    if (!_usbService.IsConnection())
    //    {
    //        throw new InvalidOperationException("USB service is not opened.");
    //    }

    //    var adapter = new UsbSerialAdapter(_usbService);

    //    // create modbus master
    //    IModbusSerialMaster master = ModbusSerialMaster.CreateRtu(adapter);

    //    return await Task.Run(() => master.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
    //}

    ////**********************************
    //// SerialPort 버전 

    //public async Task<ushort[]> ReadHoldingRegistersAsync(string portName, byte slaveId, ushort startAddress, ushort numRegisters)
    //{
    //    using (SerialPort port = new SerialPort(portName))
    //    {
    //        // configure serial port
    //        port.BaudRate = 9600;
    //        port.DataBits = 8;
    //        port.Parity = Parity.None;
    //        port.StopBits = StopBits.One;
    //        port.Open();

    //        var adapter = new SerialPortAdapter(port);

    //        // create modbus master
    //        IModbusSerialMaster master = ModbusSerialMaster.CreateRtu(adapter);

    //        return await Task.Run(() => master.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
    //    }
    //}
}

