using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.ViewModels;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;

namespace DigitalSensor.Services;


public interface IMonitoringService
{
    SensorInfo SensorInfo { get; set; }
    SensorData SensorData { get; set; }

    event Action<Models.SensorInfo> SensorInfoReceived;
    event Action<Models.SensorData> SensorDataReceived;

    event Action ErrSignal;

    event Action<int> SensorTypeReceived;
    event Action<float> SensorValueReceived;
    event Action<float> SensorMvReceived;
    event Action<float> SensorTemperatureReceived;
    event Action<int> CalibStatusReceived;

    Task InitSensor();
    Task StartMonitoring();
    Task StopMonitoring();

}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;

    private bool _isOpen = false;
    private bool _isSensorInfo = false;
    private bool _isSensorType = false;
    private bool _isRunning = false;

    private CalibrationStatus CalStatus = CalibrationStatus.NoSensorCalibration;

    public SensorInfo SensorInfo { get; set; } = new();
    public SensorData SensorData { get; set; } = new();

    public bool CalibMode { get; set; } = false;

    public event Action ErrSignal;
    public event Action<Models.SensorInfo> SensorInfoReceived;
    public event Action<Models.SensorData> SensorDataReceived;

    public event Action<int> SensorTypeReceived;
    public event Action<float> SensorValueReceived;
    public event Action<float> SensorMvReceived;
    public event Action<float> SensorTemperatureReceived;
    public event Action<int> CalibStatusReceived;


    public MonitoringService(ISensorService dataService)
    {
        _sensorService = dataService;
    }

    public async Task InitSensor()
    {
        int slaveId = await _sensorService.InitSensor();

        if (slaveId > 0)
            await UpdateSlaveID(slaveId);
        else
            throw new Exception("Failed at InitSensor.");
    }

    public async Task StartMonitoring()
    {
        _isRunning = true;
        while (_isRunning)
        {
            try
            {
                if (!_isSensorType)
                {
                    int type= await GetSensorType();

                    if (type > 0)
                        _isSensorType = true;
                }

                if(CalibMode== true)
                {
                    await RunCalibration();
                }
                else
                {
                    await GetSensorValue();
                    await GetSensorMv();
                    await GetSensorTemperature();
                }
            }
            catch (Exception ex)
            {
                ErrSignal?.Invoke();
                await Task.Delay(1000); // 1초 대기
           
                Debug.WriteLine($"Error Monitoring: {ex.Message}");
            }
        }
    }

    public async Task StopMonitoring()
    {
        // 초기화
        _isOpen = false;
        _isSensorInfo = false;
        _isSensorType = false;
        _isRunning = false;


        SensorInfo = new SensorInfo()
        {
            Type = SensorType.None
        };
        SensorInfoReceived?.Invoke(SensorInfo);

        SensorData = new SensorData
        {
            Value = 0,
            Mv = 0,
            Temperature = 0
        };
        SensorDataReceived?.Invoke(SensorData);
    }


    private async Task<int> GetSensorType()
    {
        int type = await _sensorService.GetTypeAsync();

        if (type >0)
        {
            SensorInfo = new SensorInfo()
            {
                Type = (SensorType)type,
            };

            SensorTypeReceived?.Invoke(type);
        }
        return type;
    }

    private async Task GetSensorValue()
    {
        float value = await _sensorService.GetValueAsync();
        SensorData = new SensorData
        {
            Value = value,
            Mv = SensorData.Mv,
            Temperature = SensorData.Temperature
        };


        Debug.WriteLine($"SensorValue: {value}");
        SensorValueReceived?.Invoke(value);
    }

    private async Task GetSensorMv()
    {
        float mv = await _sensorService.GetMVAsync();
        SensorData = new SensorData
        {
            Value = SensorData.Value,
            Mv = mv,
            Temperature = SensorData.Temperature
        };

        Debug.WriteLine($"SensorMv: {mv}");
        SensorMvReceived?.Invoke(mv);
    }

    private async Task GetSensorTemperature()
    {
        float temperature = await _sensorService.GetTemperatureAsync();
        SensorData = new SensorData
        {
            Value = SensorData.Value,
            Mv = SensorData.Mv,
            Temperature = temperature
        };

        Debug.WriteLine($"SensorTemperature: {temperature}");
        SensorTemperatureReceived?.Invoke(temperature);
    }

    private async Task UpdateSlaveID(int slaveId)
    {
        SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            vm.SlaveID = slaveId;
        });
    }

    private async Task RunCalibration()
    {
        try
        {
            await GetCalibStatus();

            if (CalStatus == CalibrationStatus.NoSensorCalibration)
            {
                await GetSensorValue();
            }
        }
        catch (Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기

            Debug.WriteLine($"Error Calibration: {ex.Message}");
        }
    }


    private async Task GetCalibStatus()
    {
        int status = await _sensorService.GetCalibStatusAsync();
        CalibStatusReceived?.Invoke(status);

        CalStatus = (CalibrationStatus)status;
    }


    private async Task GetSensorInfo()
    {
        if (!_isSensorInfo)
        {
            SensorInfo info = await _sensorService.GetSensorInfoAsync();
            SensorInfoReceived?.Invoke(info);

            _isSensorInfo = true;
        }
    }

    private async Task GetSensorData()
    {
        SensorData data = await _sensorService.GetSensorDataAsync();

        Debug.WriteLine($"SensorData: {data.Value}, {data.Mv}, {data.Temperature}");
        SensorDataReceived?.Invoke(data);
    }

}
