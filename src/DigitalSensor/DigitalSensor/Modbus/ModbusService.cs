using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;


namespace DigitalSensor.Modbus;


public class ModbusService
{
    private readonly IUsbService _usbService;

    public ModbusService(IUsbService usbService)
    {
        _usbService = usbService;
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

    public bool OpenDevice(int deviceId)
    {
        int baudRate = 9600;
        byte dataBits = 8;
        byte stopBits = 1;
        byte parity = 0; // 0 = None, 1 = Odd, 2 = Even

        return _usbService.Open(deviceId, baudRate, dataBits, stopBits, parity);
    }

    public async Task<ushort[]> ReadUsbSerialAdapter(byte slaveId, ushort startAddress, ushort numRegisters)
    {
        if(!_usbService.IsConnection())
        {
            throw new InvalidOperationException("USB service is not opened.");
        }

        var adapter = new UsbSerialAdapter(_usbService);

        // create modbus master
        IModbusSerialMaster master = ModbusSerialMaster.CreateRtu(adapter);

        return await Task.Run(() => master.ReadHoldingRegisters(slaveId, startAddress, numRegisters));
    }

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

