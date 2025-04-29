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
    event Action<Models.SensorInfo> SensorInfoReceived;
    event Action<Models.SensorData> SensorDataReceived;

    event Action ErrSignal;

    event Action<int> SensorTypeReceived;
    event Action<float> SensorValueReceived;
    event Action<float> SensorMvReceived;
    event Action<float> SensorTemperatureReceived;

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

    public event Action ErrSignal;
    public event Action<Models.SensorInfo> SensorInfoReceived;
    public event Action<Models.SensorData> SensorDataReceived;

    public event Action<int> SensorTypeReceived;
    public event Action<float> SensorValueReceived;
    public event Action<float> SensorMvReceived;
    public event Action<float> SensorTemperatureReceived;


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
                await GetSensorType();
                await GetSensorValue();
                await GetSensorMv();
                await GetSensorTemperature();

                // 현재는 지원하지 않음
                //await GetSensorData();

                //await Task.Delay(1000); // 1초 대기
            }
            catch (Exception ex)
            {
                ErrSignal?.Invoke();

                Debug.WriteLine($"Error: {ex.Message}");
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


        SensorInfo info = new SensorInfo()
        {
            Type = SensorType.None
        };
        SensorInfoReceived?.Invoke(info);

        SensorData data = new SensorData
        {
            Value = 0,
            Mv = 0,
            Temperature = 0
        };
        SensorDataReceived?.Invoke(data);
    }


    private async Task GetSensorType()
    {
        if (!_isSensorType)
        {
            int type = await _sensorService.GetTypeAsync();
            SensorTypeReceived?.Invoke(type);

            _isSensorType = true;
        }
    }

    private async Task GetSensorValue()
    {
        float value = await _sensorService.GetValueAsync();
        Debug.WriteLine($"SensorValue: {value}");
        SensorValueReceived?.Invoke(value);
    }

    private async Task GetSensorMv()
    {
        float mv = await _sensorService.GetMVAsync();
        Debug.WriteLine($"SensorMv: {mv}");
        SensorMvReceived?.Invoke(mv);
    }

    private async Task GetSensorTemperature()
    {
        float temperature = await _sensorService.GetTemperatureAsync();
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
