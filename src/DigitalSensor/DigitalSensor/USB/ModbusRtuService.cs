using System;
using System.IO.Ports;
using System.Threading.Tasks;
using Modbus.IO;
using Modbus.Data;
using Modbus.Device;
using Modbus.Serial;
using System.Collections.Generic;
using DigitalSensor.Models;
using System.Threading;

namespace DigitalSensor.USB;

public class ModbusRtuService 
{
    private readonly IUsbService _usbService;
    private Action _TxSignal;
    private Action _RxSignal;

    public ModbusRtuService(IUsbService usbService, Action TxSignal, Action RxSignal)
    {
        _usbService = usbService;
        _TxSignal = TxSignal;
        _RxSignal = RxSignal;
    }

    //public async Task<byte[]> ReadHoldingRegistersAsync(byte slaveId, ushort startAddress, ushort numberOfPoints)
    //{
    //    byte functionCode = 0x03;
    //    byte[] frame = new byte[8];
    //    frame[0] = slaveId;
    //    frame[1] = functionCode;
    //    frame[2] = (byte)(startAddress >> 8);
    //    frame[3] = (byte)(startAddress & 0xFF);
    //    frame[4] = (byte)(numberOfPoints >> 8);
    //    frame[5] = (byte)(numberOfPoints & 0xFF);
    //    ushort crc = Crc16(frame, 6);
    //    frame[6] = (byte)(crc & 0xFF);
    //    frame[7] = (byte)(crc >> 8);

    //    await _usbService.WriteAsync(frame);

    //    // 예상 응답: SlaveID + Function + ByteCount + Data... + CRC_L + CRC_H
    //    int expectedLength = 5 + numberOfPoints * 2;
    //    byte[] response = await _usbService.ReadAsync(expectedLength, TimeSpan.FromMilliseconds(500));

    //    if (!ValidateCrc(response))
    //        throw new Exception("CRC mismatch");

    //    return response;
    //}

    private ushort Crc16(byte[] data, int length)
    {
        ushort crc = 0xFFFF;
        for (int pos = 0; pos < length; pos++)
        {
            crc ^= data[pos];
            for (int i = 0; i < 8; i++)
            {
                bool lsb = (crc & 1) != 0;
                crc >>= 1;
                if (lsb)
                    crc ^= 0xA001;
            }
        }
        return crc;
    }

    private bool ValidateCrc(byte[] data)
    {
        int len = data.Length;
        ushort crc = Crc16(data, len - 2);
        return data[len - 2] == (byte)(crc & 0xFF) && data[len - 1] == (byte)(crc >> 8);
    }
}

