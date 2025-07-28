using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.USB;
using DigitalSensor.ViewModels;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;

namespace DigitalSensor.Services;

public interface ISensorService
{
    event Action<UsbDeviceInfo> SensorAttached;
    event Action SensorDetached;

    Task<bool> Open();
    Task Close();

    Task<int> RetrieveID();
    Task<SensorInfo> GetSensorInfoAsync();
    Task<SensorData> GetSensorDataAsync();
    Task<int> GetCalibStatusAsync();
    Task<int> GetLampOnOffAsync();

    Task<int> GetTypeAsync();
    Task<float> GetValueAsync();
    Task<float> GetMVAsync();
    Task<float> GetTemperatureAsync();

    Task SetCalibAbortAsync();
    Task SetCalibZeroAsync();
    Task SetCalibTempAsync(float v);
    Task SetCalib1PSampleAsync(float v);
    Task SetCalib2PBufferAsync(int order);
    Task SetLampOnOffAsync(int onoff);

    void DiscardInBuffer();
}


public class SensorService : ISensorService
{
    public event Action<UsbDeviceInfo> SensorAttached;
    public event Action SensorDetached;

    private readonly IModbusService _modbusService;
    private readonly IUsbService    _usbService;
    private readonly NotificationService _notificationService;

    private UsbDeviceInfo _usbDeviceInfo;
    public Localize Localize { get; } = new();

    // for Design
    public SensorService()
    {
    }

    // for Runtime
    public SensorService(IUsbService usbService, IModbusService modbusService, NotificationService notificationService)
    {
        // 이벤트구독용
        _usbService = usbService;
        _modbusService = modbusService;
        //_notificationService = App.GlobalHost.GetService<NotificationService>();
        _notificationService = notificationService;

        // USB Device 구독 등록
        _usbService.UsbPermissionGranted += OnUSBPermissionGranted;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;
    }


    private async void OnUSBPermissionGranted(UsbDeviceInfo deviceInfo)
    {
        try
        {
            _usbDeviceInfo= deviceInfo;
            bool isOpen = await Open();

            if (isOpen)
            {
                SensorAttached?.Invoke(deviceInfo);

                _notificationService.ShowMessage(Localize["Information"], $"{deviceInfo.ProductName} - Device opened successfully.");
                Console.WriteLine($"USB - {deviceInfo.ProductName}:{deviceInfo.DeviceId} Device Attached");

                //SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

                //await UiDispatcherHelper.RunOnUiThreadAsync( async () =>
                //{
                //    vm.UsbDevice = deviceInfo;
                //});
            }
        }
        catch(Exception ex)
        {
            Console.WriteLine($"Error opening device: {ex.Message}");
            _notificationService.ShowMessage(Localize["Information"], $"Error opening device: {ex.Message}");
            return;
        }

        // 센서 진단
        //callHealthCheck();
    }



    private async void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        await _modbusService.Close();
        _notificationService.ShowMessage(Localize["Information"], $"Device closed.");

        SensorDetached?.Invoke();       
    }

    public async Task<bool> Open()
    {
        try
        {
            await _modbusService.Open(_usbDeviceInfo.DeviceId);
            return true;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error opening device: {ex.Message}");
            return false;
        }
    }
    public async Task Close()
    {
        try
        {
            await _modbusService.Close();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error closing device: {ex.Message}");
        }
    }


    public async Task<int> RetrieveID()
    {
        for (int i = 0; i < 5; i++)
        {
            int slaveId = await _modbusService.VerifyID();

            if (slaveId > 0)
                return slaveId;

            await Task.Delay(1000);
        }

        return -1;
    }

    public async Task<SensorInfo> GetSensorInfoAsync()
    {
        int type = await _modbusService.ReadSensorType();
        string serial = await _modbusService.ReadSensorSerial();
        float factor = await _modbusService.ReadSensorFactor();
        float offset = await _modbusService.ReadSensorOffset();

        var data = new SensorInfo
        {
            Type = (SensorType)type,
            Serial = serial,
            SensorUnit = string.Empty,
            Factor = factor,
            Offset = offset
        };

        return data;
    }


    public async Task<SensorData> GetSensorDataAsync()
    {
        SensorData data = await _modbusService.ReadSensorData();

        return new SensorData
        {
            Timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
            Value = data.Value,
            Mv = data.Mv,
            Temperature = data.Temperature
        };
    }


    public async Task<int> GetTypeAsync()
    {
        int type = await _modbusService.ReadSensorType();
        return type;
    }

    public async Task<float> GetValueAsync()
    {
        float value = await _modbusService.ReadSensorValue();
        return value;
    }

    public async Task<float> GetMVAsync()
    {
        float mv = await _modbusService.ReadSensorMV();
        return mv;
    }

    public async Task<float> GetTemperatureAsync()
    {
        float temperature = await _modbusService.ReadTempValue();
        return temperature;
    }

    public async Task<int> GetCalibStatusAsync()
    {
        int status = await _modbusService.ReadCalibStatus();
        return status;
    }

    public async Task<int> GetLampOnOffAsync()
    {
        int status = await _modbusService.ReadLampOnOff();
        return status;
    }

    public async Task SetCalibZeroAsync()
    {
        try
        {
            await _modbusService.WriteCalibZero(1);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] calibration zero: {ex.Message}");
            throw;
        }
    }

    public async Task SetCalibAbortAsync()
    {
        try
        {
            await _modbusService.WriteCalibAbort(1);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] calibration abort: {ex.Message}");
            throw;
        }
    }

    public async Task SetCalibTempAsync(float value)
    {
        try
        {
            await _modbusService.WriteCalib1pTemp(value);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] calibration temperature: {ex.Message}");
            throw;
        }
    }

    public async Task SetCalib1PSampleAsync(float value)
    {
        try
        {
            await _modbusService.WriteCalib1pSample(value);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] calibration 1p sample: {ex.Message}");
            throw;
        }
    }

    public async Task SetCalib2PBufferAsync(int order)
    {
        try
        {
            ushort value = order switch
            {
                0 => 0,
                1 => 1,
                _ => throw new ArgumentOutOfRangeException(nameof(order), "Invalid order value")
            };
            await _modbusService.WriteCalib2pBuffer(value);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] calibration 2p buffer: {ex.Message}");

            // 재전송
            throw;
        }
    }

    public async Task SetLampOnOffAsync(int onoff)
    {
        try
        {
            ushort value = onoff switch
            {
                0 => 0,
                1 => 1,
                _ => throw new ArgumentOutOfRangeException(nameof(onoff), "Invalid onoff value")
            };
            await _modbusService.WriteLampOnOff(value);
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] SetLampOnOffAsync: {ex.Message}");

            // 재전송
            throw;
        }
    }


    public void DiscardInBuffer()
    {
        try
        {
            _usbService.DiscardInBuffer();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"[Error] DiscardInBuffer: {ex.Message}");
            throw;
        }
    }
}