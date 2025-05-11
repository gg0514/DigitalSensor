using Avalonia.Platform;
using DigitalSensor.Models;
using DigitalSensor.ViewModels;
using FluentIcons.Common.Internals;
using HarfBuzzSharp;
using Modbus.Device;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using DigitalSensor.Utils;
using DigitalSensor.USB;
using Microsoft.Win32;

namespace DigitalSensor.Services;


public interface IModbusService
{
    event Action TxSignal;
    event Action RxSignal;

    Task<bool> Open(int deviceId);
    Task<bool> Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);
    Task Close();

    Task TestConnection();
    Task<int> VerifyID();

    Task<string> ReadHoldingRegisters(byte slaveId, ushort startAddress, ushort numRegisters);

    Task<ushort[]> ReadSlaveId();
    Task<SensorData> ReadSensorData();
    Task<float> ReadSensorValue();
    Task<float> ReadTempValue();
    Task<float> ReadSensorMV();

    Task<ushort> ReadSensorType();
    Task<string> ReadSensorSerial();
    Task<float> ReadSensorFactor();
    Task<float> ReadSensorOffset();

    Task<float> ReadCalib1pSample();
    Task<ushort> ReadCalibStatus();

    Task WriteSlaveId(ushort value);
    Task WriteSensorFactor(float value);
    Task WriteSensorOffset(float value);
    Task WriteCalib1pSample(float value);
    Task WriteCalib2pBuffer(ushort value);
    Task WriteCalibZero(ushort value);
    Task WriteCalibAbort(ushort value);
}


public class ModbusService : IModbusService
{
    public event Action TxSignal;
    public event Action RxSignal;

    // Source
    private IUsbService _usbService;
    private SerialConn _serialConn;

    private JObject _modbusMap;
    private byte _slaveId = 1;

    //private IModbusSerialMaster? _modbusMaster1;
    private ModbusRtuService _modbusMaster;

    
    public ModbusService(IUsbService usbService, AppSettings settings)
    {
        _usbService = usbService;
        _serialConn = settings.SerialConn;
    }

    public async Task<bool> Open(int deviceId)
    {

        int buadRate = int.Parse(_serialConn.BaudRate);
        int dataBits = int.Parse(_serialConn.DataBits);
        int stopBits = int.Parse(_serialConn.StopBits);    
        int parity = int.Parse(_serialConn.Parity);    

        return await Open(deviceId, buadRate, (byte)dataBits, (byte)stopBits, (byte)parity);

        // 이런식으로 쓰지 말것! 교착상태 발생함
        //return Open(deviceId, buadRate, (byte)dataBits, (byte)stopBits, (byte)parity).Result;
    }

    public async Task<bool> Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
    {
        _modbusMap = await Task.Run(() =>
        {
            Debug.WriteLine($"MODBUS - Loding Modbus Map File (modbus_config.json)");
            return JsonLoader.Load_modbusMap("modbus_config.json");
        });

        return await Task.Run(() =>
        {
            // USB 연결 열기
            bool isUsbOpened = _usbService.Open(deviceId, 9600, 8, 1, 0);
            if (!isUsbOpened)
            {
                throw new InvalidOperationException("USB 연결을 열 수 없습니다.");
            }

            Debug.WriteLine($"MODBUS - Device (ID:{deviceId}) Open Success!!");

            // Modbus 마스터 초기화
            //var usbStream = new UsbSerialAdapter(_usbService, TxSignal, RxSignal);
            //_modbusMaster = ModbusSerialMaster.CreateRtu(usbStream);

            _modbusMaster = new ModbusRtuService(_usbService, TxSignal, RxSignal);

            return true;
        });
    }


    public async Task Close()
    {
        if (_modbusMaster != null)
        {
            // USB 연결 비동기 닫기
            Debug.WriteLine($"MODBUS - Close Device ...");
            await Task.Run(() => _usbService.Close());

            // Modbus 마스터 비동기 해제
            await Task.Run(() => _modbusMaster.Dispose());
            _modbusMaster = null;
        }
    }

    public async Task<int> VerifyID()
    {
        if(!_usbService.IsConnection())
        {
            Debug.WriteLine("MODBUS - USB is not connected");
            return -1;
        }

        try
        {
            _slaveId = (byte)(await ReadSlaveId())[0];
            Debug.WriteLine($"MODBUS - Verify ID: {_slaveId}");

            return _slaveId;
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"MODBUS - Verify Identification Error: {ex.Message}");
            return -1;
        }
    }


    public async Task TestConnection()
    {
        if (!_usbService.IsConnection())
            return;

        await _modbusMaster?.ReadHoldingRegistersAsync(250, 25, 1);
    }


    public async Task<string> ReadHoldingRegisters(byte slaveId, ushort startAddress, ushort numRegisters)
    {
        if (!_usbService.IsConnection())
            return null;

        //byte slaveId = 250;
        //ushort startAddress = 20;
        //ushort numRegisters = 1;
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);


        string hexString = string.Join(" ", registers.Select(v => v.ToString("X4")));

        return $"ReadHoldingRegisters: {hexString}";
    }


    // SLAVE ID
    public async Task<ushort[]> ReadSlaveId()
    {
        if (!_usbService.IsConnection())
            return null;

        // 만능키ID
        byte SecretId = 250;

        ushort startAddress = (ushort)_modbusMap["SLAVE_ID"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SLAVE_ID"]["dataLength"]; ;
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(SecretId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSlaveId: {SecretId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return registers;
    }

    // 센서 데이터 통합
    public async Task<SensorData> ReadSensorData()
    {
        if (!_usbService.IsConnection())
            return null;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_VALUE"]["address"];
        ushort numRegisters = 6;
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorData: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToSensorData(registers);
    }


    // 센서 데이터
    public async Task<float> ReadSensorValue()
    {
        if (!_usbService.IsConnection())
            return -1;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_VALUE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_VALUE"]["dataLength"];
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorValue: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToFloat(registers);
    }


    // 수온
    public async Task<float> ReadTempValue()
    {
        if (!_usbService.IsConnection())
            return -1;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["TEMP_VALUE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["TEMP_VALUE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadTempValue: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToFloat(registers);
    }

    // MV
    public async Task<float> ReadSensorMV()
    {
        if (!_usbService.IsConnection())
            return -1;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_MV"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_MV"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorMV: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToFloat(registers);
    }

    // 센서 타입
    public async Task<ushort> ReadSensorType()
    {
        if (!_usbService.IsConnection())
            return 0;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_TYPE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_TYPE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorType: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return registers[0];
    }

    // 센서 시리얼
    public async Task<string> ReadSensorSerial()
    {
        if (!_usbService.IsConnection())
            return null;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_SERIAL"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_SERIAL"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorSerial: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToHexString(registers);
    }

    // 센서 팩터
    public async Task<float> ReadSensorFactor()
    {
        if (!_usbService.IsConnection())
            return -1;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_FACTOR"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_FACTOR"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorFactor: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToFloat(registers);
    }

    // 센서 오프셋
    public async Task<float> ReadSensorOffset()
    {
        if (!_usbService.IsConnection())
            return -1;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_OFFSET"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_OFFSET"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadSensorOffset: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToFloat(registers);
    }

    // CALIB_1P_SAMPLE
    public async Task<float> ReadCalib1pSample()
    {
        if (!_usbService.IsConnection())
            return -1;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_1P_SAMPLE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["CALIB_1P_SAMPLE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadCalib1pSample: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return ConvertToFloat(registers);
    }

    // CALIB_STATUS
    public async Task<ushort> ReadCalibStatus()
    {
        if (!_usbService.IsConnection())
            return 0;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_STATUS"]["address"];
        ushort numRegisters = (ushort)_modbusMap["CALIB_STATUS"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        string result = string.Join(" ", registers.Select(v => v.ToString("X4")));

        Debug.WriteLine($"MODBUS - ReadCalibStatus: {slaveId}, {startAddress}, {numRegisters}, Result= 0x{result}");

        return registers[0];
    }



    // SlaveId
    public async Task WriteSlaveId(ushort value)
    {
        if (!_usbService.IsConnection())
            return;

        // 만능키ID
        byte SecretId = 250;
        byte slaveId = SecretId;

        ushort startAddress = (ushort)_modbusMap["SLAVE_ID"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SLAVE_ID"]["dataLength"]; ;

        await _modbusMaster?.WriteSingleRegisterAsync(slaveId, startAddress, value);

        Debug.WriteLine($"MODBUS - WriteSlaveId: {slaveId},{startAddress},{value}");
    }

    // 센서 팩터
    public async Task WriteSensorFactor(float value)
    {
        if (!_usbService.IsConnection())
            return;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_FACTOR"]["address"];

        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);

        Debug.WriteLine($"MODBUS - WriteSensorFactor: {slaveId},{startAddress},{value}");
    }

    // 센서 오프셋
    public async Task WriteSensorOffset(float value)
    {
        if (!_usbService.IsConnection())
            return;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_OFFSET"]["address"];
        ushort[] registers = ConvertToRegisters(value);

        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);

        Debug.WriteLine($"MODBUS - WriteSensorOffset: {slaveId},{startAddress},{value}");
    }

    // CALIB_1P_SAMPLE
    public async Task WriteCalib1pSample(float value)
    {
        if (!_usbService.IsConnection())
            return;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_1P_SAMPLE"]["address"];
        ushort[] registers = ConvertToRegisters(value);

        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);

        Debug.WriteLine($"MODBUS - WriteCalib1pSample: {slaveId},{startAddress},{value}");
    }

    // CALIB_2P_BUFFER
    public async Task WriteCalib2pBuffer(ushort value)
    {
        if (!_usbService.IsConnection())
            return;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_2P_BUFFER"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);

        Debug.WriteLine($"MODBUS - WriteCalib2pBuffer: {slaveId},{startAddress},{value}");
    }

    // CALIB_ZERO
    public async Task WriteCalibZero(ushort value)
    {
        if (!_usbService.IsConnection())
            return;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_ZERO"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);

        Debug.WriteLine($"MODBUS - WriteCalibZero: {slaveId},{startAddress},{value}");
    }

    // CALIB_ABORT
    public async Task WriteCalibAbort(ushort value)
    {
        if (!_usbService.IsConnection())
            return;

        byte slaveId = _slaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_ABORT"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);

        Debug.WriteLine($"MODBUS - WriteCalibAbort: {slaveId},{startAddress},{value}");
    }


    private SensorData ConvertToSensorData(ushort[] registers)
    {
        if (registers == null || registers.Length < 6)
        {
            throw new ArgumentException("Invalid register data. Expected at least 6 registers.");
        }

        // SensorData 객체 생성
        var sensorData = new SensorData();

        // 각 float 값은 2개의 레지스터(32비트)로 구성됨
        // registers[0]과 registers[1] -> Value
        sensorData.Value = ConvertRegistersToFloat(registers[0], registers[1]);

        // registers[2]와 registers[3] -> Temperature
        sensorData.Temperature = ConvertRegistersToFloat(registers[2], registers[3]);

        // registers[4]와 registers[5] -> Mv
        sensorData.Mv = ConvertRegistersToFloat(registers[4], registers[5]);

        return sensorData;
    }

    private float ConvertRegistersToFloat(ushort high, ushort low)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(low & 0xFF);
        bytes[1] = (byte)(low >> 8);
        bytes[2] = (byte)(high & 0xFF);
        bytes[3] = (byte)(high >> 8);
        return BitConverter.ToSingle(bytes, 0);
    }
    private float ConvertToFloat(ushort[] registers)
    {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)(registers[1] & 0xFF);
        bytes[1] = (byte)(registers[1] >> 8);
        bytes[2] = (byte)(registers[0] & 0xFF);
        bytes[3] = (byte)(registers[0] >> 8);
        return BitConverter.ToSingle(bytes, 0);
    }

    private string ConvertToHexString(ushort[] registers)
    {
        //ushort[] name = new ushort[3];
        //name[0] = 0x2590;
        //name[1] = 0xfaae;
        //name[2] = 0x0000;

        //string hexString = string.Concat(name.Select(x => x.ToString("X4")));
        // 출력: 2590FAAE0000

        string hexString = string.Concat(registers.Select(x => x.ToString("X4")));
        return hexString;
    }


    // float	1.0f
    // IEEE 754 (Hex)	0x3F800000
    // ushort[] (Little-Endian)	{ 0x0000, 0x3F80 }

    // Big-Endian	0x3F80 0x0000
    // ushort[] = { 0x3F80, 0x0000 }
    
    private ushort[] ConvertToRegisters(float value)
    {
        byte[] bytes = BitConverter.GetBytes(value); // [0x00, 0x00, 0x80, 0x3F]
        return new ushort[]
        {
        (ushort)(bytes[3] << 8 | bytes[2]), // 0x3F80
        (ushort)(bytes[1] << 8 | bytes[0])  // 0x0000
        };
    }



    public async Task SensorHealthCheck()
    {
        for (int i = 0; i < 3; i++)
        {
            int type = await ReadSensorType();                // 0x06
            string serial = await ReadSensorSerial();         // 0x08

            float value = await ReadSensorValue();            // 0x00
            float temperature = await ReadTempValue();        // 0x02
            float mv = await ReadSensorMV();                  // 0x04

            float factor = await ReadSensorFactor();          // 0x0B
            float offset = await ReadSensorOffset();          // 0x0D
            float sample = await ReadCalib1pSample();         // 0x0F
            ushort calib = await ReadCalibStatus();           // 0x07
        }
    }

    public bool RecoverConnection()
    {
        bool recovered = _usbService.TryRecover(() =>
        {
            try
            {
                TestConnection();
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

