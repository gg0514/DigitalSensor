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
using System.Linq;
using HarfBuzzSharp;
using System.Diagnostics;

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

    public void Dispose()
    {
        _usbService.Close();
    }

    public async Task<ushort[]> ReadHoldingRegistersAsync(byte slaveId, ushort startAddress, ushort numberOfPoints)
    {
        byte functionCode = 0x03;
        byte[] frame = new byte[8];
        frame[0] = slaveId;
        frame[1] = functionCode;
        frame[2] = (byte)(startAddress >> 8);
        frame[3] = (byte)(startAddress & 0xFF);
        frame[4] = (byte)(numberOfPoints >> 8);
        frame[5] = (byte)(numberOfPoints & 0xFF);
        ushort crc = Crc16(frame, 6);
        frame[6] = (byte)(crc & 0xFF);
        frame[7] = (byte)(crc >> 8);


        string hex_req = BitConverter.ToString(frame, 0, 8).Replace("-", " ");
        Debug.WriteLine($"MODBUS Write (0:8): {hex_req}");

        _TxSignal?.Invoke();
        await _usbService.WriteAsync(frame);

        _RxSignal?.Invoke();
        //byte[] response = await _usbService.ReadAsync();

        // 예상 응답: SlaveID + Function + ByteCount + Data... + CRC_L + CRC_H
        int expectedLength = 5 + numberOfPoints * 2;
        byte[] response = await _usbService.ReadAsync(expectedLength, TimeSpan.FromMilliseconds(500));

        string hex_resp = BitConverter.ToString(response, 0, response.Length).Replace("-", " ");
        Debug.WriteLine($"MODBUS Read (0:{response.Length}): {hex_resp}");

        if (!ValidateCrc(response))
            throw new Exception("CRC mismatch");


        // ByteCount 읽기
        int byteCount = response[2];
        if (byteCount != numberOfPoints * 2)
            throw new Exception($"Unexpected byte count: {byteCount}");

        // 데이터 파싱
        ushort[] registers = new ushort[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++)
        {
            int dataIndex = 3 + i * 2;
            registers[i] = (ushort)((response[dataIndex] << 8) | response[dataIndex + 1]);
        }

        return registers;
    }

    public async Task WriteSingleRegisterAsync(byte slaveId, ushort address, ushort value)
    {
        byte functionCode = 0x06;
        byte[] frame = new byte[8];
        frame[0] = slaveId;
        frame[1] = functionCode;
        frame[2] = (byte)(address >> 8);
        frame[3] = (byte)(address & 0xFF);
        frame[4] = (byte)(value >> 8);
        frame[5] = (byte)(value & 0xFF);
        ushort crc = Crc16(frame, 6);
        frame[6] = (byte)(crc & 0xFF);
        frame[7] = (byte)(crc >> 8);

        _TxSignal?.Invoke();

        string hex_req = BitConverter.ToString(frame, 0, 8).Replace("-", " ");
        Debug.WriteLine($"MODBUS Write (0:8): {hex_req}");

        await _usbService.WriteAsync(frame);

        _RxSignal?.Invoke();

        //byte[] response = await _usbService.ReadAsync();

        // 응답은 요청과 동일한 구조 (에코됨)
        int expectedLength = 8;
        byte[] response = await _usbService.ReadAsync(expectedLength, TimeSpan.FromMilliseconds(500));

        string hex_resp = BitConverter.ToString(response, 0, response.Length).Replace("-", " ");
        Debug.WriteLine($"MODBUS Read (0:{response.Length}): {hex_resp}");


        if (!ValidateCrc(response))
            throw new Exception("CRC mismatch");

        // 요청의 앞부분과 응답이 일치하는지 확인
        if (!(response[0] == slaveId && response[1] == functionCode &&
              response[2] == frame[2] && response[3] == frame[3] &&
              response[4] == frame[4] && response[5] == frame[5]))
        {
            // SecretKey 250이 아닌 경우에만 예외 발생
            if (slaveId != 250) 
                throw new Exception("Mismatch response");
        }
    }

    public async Task WriteMultipleRegistersAsync(byte slaveId, ushort startAddress, ushort[] values)
    {
        if (values == null || values.Length == 0)
            throw new ArgumentException("Values cannot be null or empty");

        byte functionCode = 0x10;
        int registerCount = values.Length;
        byte byteCount = (byte)(registerCount * 2);

        byte[] frame = new byte[9 + byteCount]; // header(7) + data(N*2) + CRC(2)
        frame[0] = slaveId;
        frame[1] = functionCode;
        frame[2] = (byte)(startAddress >> 8);
        frame[3] = (byte)(startAddress & 0xFF);
        frame[4] = (byte)(registerCount >> 8);
        frame[5] = (byte)(registerCount & 0xFF);
        frame[6] = byteCount;

        for (int i = 0; i < registerCount; i++)
        {
            frame[7 + i * 2] = (byte)(values[i] >> 8);
            frame[8 + i * 2] = (byte)(values[i] & 0xFF);
        }

        ushort crc = Crc16(frame, frame.Length - 2);
        frame[^2] = (byte)(crc & 0xFF);
        frame[^1] = (byte)(crc >> 8);

        _TxSignal?.Invoke();

        string hex_req = BitConverter.ToString(frame, 0, 8).Replace("-", " ");
        Debug.WriteLine($"MODBUS Write (0:8): {hex_req}");

        await _usbService.WriteAsync(frame);

   
        _RxSignal?.Invoke();
        //byte[] response = await _usbService.ReadAsync();

        // 응답: SlaveId + Func + StartAddr + Quantity + CRC = 8 bytes
        int expectedLength = 8;
        byte[] response = await _usbService.ReadAsync(expectedLength, TimeSpan.FromMilliseconds(500));

        string hex_resp = BitConverter.ToString(response, 0, response.Length).Replace("-", " ");
        Debug.WriteLine($"MODBUS Read (0:{response.Length}): {hex_resp}");

        if (!ValidateCrc(response))
            throw new Exception("CRC mismatch");

        // 요청의 앞부분과 응답이 일치하는지 확인
        if (!(response[0] == slaveId && response[1] == functionCode &&
              response[2] == frame[2] && response[3] == frame[3] &&
              response[4] == frame[4] && response[5] == frame[5]))
        {
            // SecretKey 250이 아닌 경우에만 예외 발생
            if (slaveId != 250)
                throw new Exception("Mismatch response");
        }
    }


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

