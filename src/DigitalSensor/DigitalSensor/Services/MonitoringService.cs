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


    void StartMonitoring();
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;

    private bool _isOpen = false;
    private bool _isSlaveID = false;
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

    public async void StartMonitoring()
    {
        _isOpen = _sensorService.IsOpen();

        if (!_isOpen)
        {
            _isOpen = await _sensorService.Open();
        }

        _isRunning = true;
        while (_isRunning)
        {
            try
            {
                await GetSlaveID();
                await GetSensorType();
                await GetSensorValue();
                await GetSensorMv();
                await GetSensorTemperature();
            }
            catch (Exception ex)
            {
                ErrSignal?.Invoke();

                Debug.WriteLine($"Error: {ex.Message}");
            }
        }
    }

    public async void StopMonitoring()
    {
        _isRunning = false;
    }

    private async Task GetSlaveID()
    {
        if (!_isSlaveID)
        {
            int slaveId = await _sensorService.Initialize();
            if (slaveId > 0)
            {
                _isSlaveID = true;

                SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

                await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
                {
                    vm.SlaveID = slaveId;
                });
            }
        }
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
        SensorValueReceived?.Invoke(value);
    }

    private async Task GetSensorMv()
    {
        float mv = await _sensorService.GetMVAsync();
        SensorMvReceived?.Invoke(mv);
    }

    private async Task GetSensorTemperature()
    {
        float temperature = await _sensorService.GetTemperatureAsync();
        SensorTemperatureReceived?.Invoke(temperature);
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
        SensorDataReceived?.Invoke(data);
    }

}
