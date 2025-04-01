using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.Data;
using Modbus.Device;
using Modbus.Utility;
using Modbus.Serial;

namespace DigitalSensor.Services
{
    public class ModbusService
    {
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
}
