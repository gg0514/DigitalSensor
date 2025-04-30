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
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace DigitalSensor.Modbus;


public interface IModbusService
{
    event Action TxSignal;
    event Action RxSignal;

    byte SlaveId { get; set; }

    Task<bool> Open(int deviceId);
    Task<bool> Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);
    Task Close();

    Task<bool> Initialize();
    Task TestConnection();
    Task<ushort[]> ReadSlaveId();

    Task<string> ReadHoldingRegisters(byte slaveId, ushort startAddress, ushort numRegisters);

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

    private readonly IUsbService _usbService;
    private readonly HomeViewModel _viewModel;

    private IModbusSerialMaster? _modbusMaster;
    private JObject _modbusMap;

    public byte SlaveId { get; set; } = 1; // 기본값 부여


    public ModbusService(IUsbService usbService)
    {
        _usbService = usbService;
    }

    public async Task<bool> Open(int deviceId)
    {
        int buadRate = 9600;
        int dataBits = 8;
        int stopBits = 1;
        int parity = 0;

        return await Open(deviceId, buadRate, (byte)dataBits, (byte)stopBits, (byte)parity);

        // 이런식으로 쓰지 말것! 교착상태 발생함
        //return Open(deviceId, buadRate, (byte)dataBits, (byte)stopBits, (byte)parity).Result;
    }

    public async Task<bool> Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
    {
        ////USB 연결 열기
        //bool isUsbOpened = _usbService.Open(deviceId, 9600, 8, 1, 0);
        //if (!isUsbOpened)
        //{
        //    throw new InvalidOperationException("USB 연결을 열 수 없습니다.");
        //}

        //// Modbus 마스터 초기화
        //var usbStream = new UsbSerialAdapter(_usbService);
        //_modbusMaster = ModbusSerialMaster.CreateRtu(usbStream);

        //_modbusMap = await Task.Run(() =>
        //{
        //    return JsonLoader.Load_modbusMap("Assets/modbus_config.json");
        //});

        //return true;

        _modbusMap = await Task.Run(() =>
        {
            return JsonLoader.Load_modbusMap("Assets/modbus_config.json");
        });

        return await Task.Run(() =>
        {
            // USB 연결 열기
            bool isUsbOpened = _usbService.Open(deviceId, 9600, 8, 1, 0);
            if (!isUsbOpened)
            {
                throw new InvalidOperationException("USB 연결을 열 수 없습니다.");
            }

            // Modbus 마스터 초기화
            var usbStream = new UsbSerialAdapter(_usbService, TxSignal, RxSignal);
            _modbusMaster = ModbusSerialMaster.CreateRtu(usbStream);

            return true;
        });
    }


    public async Task Close()
    {
        if (_modbusMaster != null)
        {
            // USB 연결 비동기 닫기
            await Task.Run(() => _usbService.Close());

            // Modbus 마스터 비동기 해제
            await Task.Run(() => _modbusMaster.Dispose());
            _modbusMaster = null;
        }
    }

    public async Task<bool> Initialize()
    {
        if (_modbusMaster == null)
        {
            Debug.WriteLine("Modbus master is not initialized.");
            return false;
        }

        try
        {
            SlaveId = (byte)(await ReadSlaveId())[0];
            return true;
        }
        catch(Exception ex)
        {
            Debug.WriteLine($"Initialize Error: {ex.Message}");
            return false;
        }
    }


    // 현재 동작하지 않음.
    public async Task TestConnection()
    {
        await _modbusMaster?.ReadHoldingRegistersAsync(250, 20, 1);
    }

    
    public async Task<string> ReadHoldingRegisters(byte slaveId, ushort startAddress, ushort numRegisters)
    {
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
        //byte slaveId = 250;
        byte slaveId = 1;

        ushort startAddress = (ushort)_modbusMap["SLAVE_ID"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SLAVE_ID"]["dataLength"]; ;


        Debug.WriteLine($"ReadSlaveId : _modbusMaster={_modbusMaster}");

        return await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

    }

    // 센서 데이터 통합
    public async Task<SensorData> ReadSensorData()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_VALUE"]["address"];
        ushort numRegisters = 6; 
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);

        return ConvertToSensorData(registers);
    }


    // 센서 데이터
    public async Task<float> ReadSensorValue()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_VALUE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_VALUE"]["dataLength"]; 
        ushort[] registers = await _modbusMaster?.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);


        return ConvertToFloat(registers);
    }


    // 수온
    public async Task<float> ReadTempValue()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["TEMP_VALUE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["TEMP_VALUE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

        return ConvertToFloat(registers);
    }

    // MV
    public async Task<float> ReadSensorMV()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_MV"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_MV"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

        return ConvertToFloat(registers);
    }

    // 센서 타입
    public async Task<ushort> ReadSensorType()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_TYPE"]["address"];
        

        return (await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, 1))[0];
    }

    // 센서 시리얼
    public async Task<string> ReadSensorSerial()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_SERIAL"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_SERIAL"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

        return ConvertToHexString(registers);
    }

    // 센서 팩터
    public async Task<float> ReadSensorFactor()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_FACTOR"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_FACTOR"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

        return ConvertToFloat(registers);
    }

    // 센서 오프셋
    public async Task<float> ReadSensorOffset()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_OFFSET"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SENSOR_OFFSET"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

        return ConvertToFloat(registers);
    }

    // CALIB_1P_SAMPLE
    public async Task<float> ReadCalib1pSample()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_1P_SAMPLE"]["address"];
        ushort numRegisters = (ushort)_modbusMap["CALIB_1P_SAMPLE"]["dataLength"];
        ushort[] registers = await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
        

        return ConvertToFloat(registers);
    }

    // CALIB_STATUS
    public async Task<ushort> ReadCalibStatus()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_STATUS"]["address"];
        

        return (await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, 1))[0];
    }



    // SlaveId
    public async Task WriteSlaveId(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SLAVE_ID"]["address"];
        ushort numRegisters = (ushort)_modbusMap["SLAVE_ID"]["dataLength"]; ;

        await _modbusMaster?.WriteSingleRegisterAsync(slaveId, startAddress, value);
        

    }

    // 센서 팩터
    public async Task WriteSensorFactor(float value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_FACTOR"]["address"];
        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);
        

    }

    // 센서 오프셋
    public async Task WriteSensorOffset(float value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["SENSOR_OFFSET"]["address"];
        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);
        

    }

    // CALIB_1P_SAMPLE
    public async Task WriteCalib1pSample(float value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_1P_SAMPLE"]["address"];
        ushort[] registers = ConvertToRegisters(value);
        await _modbusMaster.WriteMultipleRegistersAsync(slaveId, startAddress, registers);
        

    }

    // CALIB_2P_BUFFER
    public async Task WriteCalib2pBuffer(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_2P_BUFFER"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);
        
    }

    // CALIB_ZERO
    public async Task WriteCalibZero(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_ZERO"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);
        

    }

    // CALIB_ABORT
    public async Task WriteCalibAbort(ushort value)
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)_modbusMap["CALIB_ABORT"]["address"];
        await _modbusMaster.WriteSingleRegisterAsync(slaveId, startAddress, value);
       
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

    private ushort[] ConvertToRegisters(float value)
    {
        byte[] bytes = BitConverter.GetBytes(value);
        return new ushort[]
        {
           (ushort)(bytes[2] << 8 | bytes[3]),
           (ushort)(bytes[0] << 8 | bytes[1])
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
        bool recovered = _usbService.TryRecover(() => {
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


public class JsonLoader
{
    public static JObject Load_modbusMap(string jsonFilePath)
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
