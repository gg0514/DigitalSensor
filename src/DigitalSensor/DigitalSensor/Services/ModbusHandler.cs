using Avalonia.Platform;
using DigitalSensor.Models;
using Modbus.Device;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace DigitalSensor.Services;


public class ModbusHandler
{
    private readonly IModbusSerialMaster? _modbusMaster;
    private readonly UsbDeviceInfo? _usbDeviceInfo;
    private readonly JObject modbusMap;

    public byte SlaveId { get; set; } = 1; // 기본값 부여


    public ModbusHandler(IModbusSerialMaster modbusMaster, UsbDeviceInfo deviceInfo)
    {
        _modbusMaster = modbusMaster;
        _usbDeviceInfo = deviceInfo;

        modbusMap = JsonLoader.LoadModbusMap("Assets/modbus_config.json");
    }

    public async Task<int> LoadSlaveId()
    {
        SlaveId = (byte)(await ReadSlaveId())[0];

        return SlaveId;
    }

    public string GetProductName()
    {
        return _usbDeviceInfo?.ProductName ?? string.Empty;
    }

    public async Task TestConnection()

    {
        int id= (await _modbusMaster?.ReadHoldingRegistersAsync(250, 20, 1))[0];
    }

    // SLAVE ID
    public async Task<ushort[]> ReadSlaveId()
    {
        byte slaveId = 250;

        ushort startAddress = (ushort)modbusMap["SLAVE_ID"]["address"];
        ushort numRegisters = (ushort)modbusMap["SLAVE_ID"]["dataLength"]; ;

        return await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
    }

    // 센서 데이터
    public async Task<float> ReadSensorValue()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_VALUE"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_VALUE"]["dataLength"]; ;
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToFloat(registers);
    }


    // 수온
    public async Task<float> ReadTempValue()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["TEMP_VALUE"]["address"];
        ushort numRegisters = (ushort)modbusMap["TEMP_VALUE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToFloat(registers);
    }

    // MV
    public async Task<float> ReadSensorMV()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_MV"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_MV"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToFloat(registers);
    }

    // 센서 타입
    public async Task<ushort> ReadSensorType()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_TYPE"]["address"];
        return (await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, 1))[0];
    }

    // 센서 시리얼
    public async Task<string> ReadSensorSerial()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_SERIAL"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_SERIAL"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToHexString(registers);
    }

    // 센서 팩터
    public async Task<float> ReadSensorFactor()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_FACTOR"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_FACTOR"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToFloat(registers);
    }

    // 센서 오프셋
    public async Task<float> ReadSensorOffset()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_OFFSET"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_OFFSET"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToFloat(registers);
    }

    // CALIB_1P_SAMPLE
    public async Task<float> ReadCalib1pSample()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["CALIB_1P_SAMPLE"]["address"];
        ushort numRegisters = (ushort)modbusMap["CALIB_1P_SAMPLE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        return ConvertToFloat(registers);
    }

    // CALIB_STATUS
    public async Task<ushort> ReadCalibStatus()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["CALIB_STATUS"]["address"];
        return (await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, 1))[0];
    }



    // SlaveId
    public async Task WriteSlaveId(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SLAVE_ID"]["address"];
        ushort numRegisters = (ushort)modbusMap["SLAVE_ID"]["dataLength"]; ;

        await _modbusMaster?.WriteSingleRegisterAsync(slaveId, startAddress, value);
    }

    // 센서 팩터
    public async Task WriteSensorFactor(float value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_FACTOR"]["address"];
        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);
    }

    // 센서 오프셋
    public async Task WriteSensorOffset(float value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_OFFSET"]["address"];
        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);
    }

    // CALIB_1P_SAMPLE
    public async Task WriteCalib1pSample(float value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["CALIB_1P_SAMPLE"]["address"];
        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);
    }

    // CALIB_2P_BUFFER
    public async Task WriteCalib2pBuffer(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["CALIB_2P_BUFFER"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);
    }

    // CALIB_ZERO
    public async Task WriteCalibZero(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["CALIB_ZERO"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);
    }

    // CALIB_ABORT
    public async Task WriteCalibAbort(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["CALIB_ABORT"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);
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

    private ushort[] ConvertToRegisters(float value)
    {
        byte[] bytes = BitConverter.GetBytes(value);
        return new ushort[]
        {
           (ushort)((bytes[2] << 8) | bytes[3]),
           (ushort)((bytes[0] << 8) | bytes[1])
        };
    }
}


public class JsonLoader
{
    public static JObject LoadModbusMap(string jsonFilePath)
    {
        // 더이상 사용하지 않음 - AssetLoader 가져오기
        // var assetLoader = AvaloniaLocator.Current.GetService<IAssetLoader>();
        // if (assetLoader == null)
        //    throw new InvalidOperationException("AssetLoader not found.");

        // 리소스 스트림 열기
        //using var stream = AssetLoader.Open(new Uri("avares://MyApp/Assets/data.json"));
        using var stream = AssetLoader.Open(new Uri($"avares://DigitalSensor/{jsonFilePath}"));
        using var reader = new StreamReader(stream);

        // JSON 문자열 읽기
        var jsonString = reader.ReadToEnd();
        return JObject.Parse(jsonString);
    }
}
