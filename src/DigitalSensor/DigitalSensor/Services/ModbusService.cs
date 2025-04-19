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
using Newtonsoft.Json.Linq;
using Avalonia.Platform;
using Avalonia;
using System.IO;

namespace DigitalSensor.Services;



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

    public async Task LoadSlaveId()
    {
        SlaveId = (byte)(await ReadSlaveId())[0];
    }

    public string GetProductName()
    {
        return _usbDeviceInfo?.ProductName ?? string.Empty;
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
        ushort startAddress = (ushort)modbusMap["SENSOR_DATA"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_DATA"]["dataLength"]; ;
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
    public async Task<ushort[]> ReadSensorSerial()
    {
        byte slaveId = SlaveId;
        ushort startAddress = (ushort)modbusMap["SENSOR_SERIAL"]["address"];
        ushort numRegisters = (ushort)modbusMap["SENSOR_SERIAL"]["dataLength"];
        return await _modbusMaster.ReadHoldingRegistersAsync(slaveId, startAddress, numRegisters);
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



public class ModbusService 
{
    // 이벤트 버블링
    //public event Action<ModbusDeviceInfo>? ModbusDeviceAttached;
    //public event Action<ModbusDeviceInfo>? ModbusDeviceDetached;

    public event Action<ModbusHandler>? ModbusHandlerAttached;
    public event Action<ModbusHandler>? ModbusHandlerDetached;

    // 생성자에서 초기화
    private readonly IUsbService _usbService;
    private readonly NotificationService _notificationService;

    // 전파이벤트에서 초기화 
    private IModbusSerialMaster? _modbusMaster = default;
    private ModbusHandler? _modbusHandler = default;                    // ModbusHandler 인스턴스 생성

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
        CloseModbus();

        ModbusHandlerDetached?.Invoke(null);
    }

    private async void OnUSBDeviceAttached(UsbDeviceInfo deviceInfo)
    {
        try
        {
            await OpenModbus(deviceInfo);
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error: {ex.Message}");
        }
    }

    private async Task OpenModbus(UsbDeviceInfo deviceInfo)
    {
        _modbusMaster= OpenModbus(deviceInfo.DeviceId);

        if (IsOpen())
        {
            _modbusHandler= new ModbusHandler(_modbusMaster, deviceInfo);
            await _modbusHandler.LoadSlaveId();

            // 상위로 이벤트 전파 
            ModbusHandlerAttached?.Invoke(_modbusHandler);



            //ushort[] result = await ReadSlaveId();
            //int slaveID = result[0];  // 배열에서 필요한 값 꺼내기

            //// 상위로 이벤트 전파 
            //ModbusDeviceAttached?.Invoke(new ModbusDeviceInfo
            //{
            //    DeviceId= _usbDeviceInfo.DeviceId,
            //    ProductName = _usbDeviceInfo.ProductName,
            //    SlaveId = slaveID,
            //});
        }
        else
        {
            _notificationService.ShowMessage("Modbus Device Open Failed", "");
        }
    }

    public void CloseModbus()
    {
        if (IsOpen())
        {
            _modbusHandler = null;

            _usbService.Close();
            _modbusMaster?.Dispose();
            _modbusMaster = null;
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



    public async Task<ushort[]> ReadSlaveId()
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

