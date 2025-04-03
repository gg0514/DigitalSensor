using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;


namespace DigitalSensor.Services;

public class UsbSerialAdapter : IStreamResource
{
    private readonly IUsbService _usbService;


    public UsbSerialAdapter(IUsbService usbService)
    {
        _usbService = usbService;
    }

    public int InfiniteTimeout { get; } = -1;    

    public int ReadTimeout { get; set; } = 1000;
    public int WriteTimeout { get; set; } = 1000;

    /// <summary>
    ///     Purges the receive buffer.
    /// </summary>
    public void DiscardInBuffer() 
    { 
    }

    public void Dispose()
    {
        // IDisposable 패턴에 따라 자원 해제
        // SerialInputOutputManager은 직접 해제하지 않음 (외부에서 관리)
        //_usbService = null;
    }

    public int Read(byte[] buffer, int offset, int count)
    {
        return _usbService.Read(buffer, offset, count);
    }
    public void Write(byte[] buffer, int offset, int count)
    {
        _usbService.Write(buffer, offset, count);
    }
}


public class ModbusService
{
    private readonly IUsbService _usbService;

    public ModbusService(IUsbService usbService)
    {
        _usbService = usbService;
    }

    public async Task<ushort[]> ReadUsbSerialAdapter(int deviceId, byte slaveId, ushort startAddress, ushort numRegisters)
    {
        int baudRate = 9600;
        byte dataBits = 8;
        byte stopBits = 1;
        byte parity = 0; // 0 = None, 1 = Odd, 2 = Even

        _usbService.Open(deviceId, baudRate, dataBits, stopBits, parity);

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

