using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;


namespace DigitalSensor.Services;


public interface IModbusService
{
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

public class ModbusService
{
    // 이벤트 버블링
    public event Action<UsbDeviceInfo>? UsbDeviceAttached;
    public event Action<UsbDeviceInfo>? UsbDeviceDetached;

    private readonly NotificationService _notificationService;
    private readonly IUsbService _usbService;
    private IModbusSerialMaster _modbusMaster = default;
    private ushort _slaveId = 0;

    public ModbusService(NotificationService notificationService, IUsbService usbService)
    {
        _notificationService = notificationService;
        _usbService = usbService;

        // 구독 등록
        _usbService.UsbDeviceAttached += OnUSBDeviceAttached;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }

    private async void OnUSBDeviceAttached(UsbDeviceInfo deviceInfo)
    {
        UsbDeviceAttached?.Invoke(deviceInfo);

        OpenModbus(deviceInfo.DeviceId);


        if (IsOpen())
        {
            ushort[] result = await ReadSlaveID();
            int slaveID = result[0];  // 배열에서 필요한 값 꺼내기

            _notificationService.ShowMessage("Slave ID", $"{slaveID}");
        }
    }

    private void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        UsbDeviceDetached?.Invoke(deviceInfo);
    }


    public List<int> DetectDevices()
    {
        var devices = _usbService.GetUsbDeviceInfos();

        List<int> deviceIds = new List<int>();

        foreach (var device in devices)
        {
            //if (device.VendorId == 0x0403 && device.ProductId == 0x6001) // FTDI USB Serial Device
            {
                deviceIds.Add(device.DeviceId);
            }
        }

        return deviceIds;
    }

    public bool IsOpen()
    {
        return _modbusMaster != null;
    }

    public IModbusSerialMaster OpenModbus(int deviceId)
    {
        if (_modbusMaster == null)
        {
            if (OpenDevice(deviceId))
            {
                var adapter = new UsbSerialAdapter(_usbService);
                _modbusMaster = ModbusSerialMaster.CreateRtu(adapter);
            }
            else
            {
                _notificationService.ShowMessage("USB Device Open Failed", "");
            }
        }
        return _modbusMaster;
    }


    public bool OpenDevice(int deviceId)
    {
        int baudRate = 9600;
        byte dataBits = 8;
        byte stopBits = 1;
        byte parity = 0; // 0 = None, 1 = Odd, 2 = Even

        return _usbService.Open(deviceId, baudRate, dataBits, stopBits, parity);
    }



    public async Task<ushort[]> ReadSlaveID()
    {
        byte slaveId = 250;
        ushort startAddress = 20;
        ushort numRegisters = 1;

        return await Task.Run(() => _modbusMaster.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
    }


    //*********************************
    // UsbSerialAdapter 버전 

    public async Task<ushort[]> ReadUsbSerialAdapter(byte slaveId, ushort startAddress, ushort numRegisters)
    {
        if (!_usbService.IsConnection())
        {
            throw new InvalidOperationException("USB service is not opened.");
        }

        var adapter = new UsbSerialAdapter(_usbService);

        // create modbus master
        IModbusSerialMaster master = ModbusSerialMaster.CreateRtu(adapter);

        return await Task.Run(() => master.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
    }

    //**********************************
    // SerialPort 버전 

    public async Task<ushort[]> ReadHoldingRegistersAsync(string portName, byte slaveId, ushort startAddress, ushort numRegisters)
    {
        using (SerialPort port = new SerialPort(portName))
        {
            // configure serial port
            port.BaudRate = 9600;
            port.DataBits = 8;
            port.Parity = Parity.None;
            port.StopBits = StopBits.One;
            port.Open();

            var adapter = new SerialPortAdapter(port);

            // create modbus master
            IModbusSerialMaster master = ModbusSerialMaster.CreateRtu(adapter);

            return await Task.Run(() => master.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
        }
    }
}

