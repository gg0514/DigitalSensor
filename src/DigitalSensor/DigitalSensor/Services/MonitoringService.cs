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

    void StartMonitoring();
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;

    private bool _isOpen = false;
    private bool _isSlaveID = false;
    private bool _isSensorInfo = false;
    private bool _isRunning = false;

    public event Action<Models.SensorInfo> SensorInfoReceived;
    public event Action<Models.SensorData> SensorDataReceived;

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
            await GetSlaveID();
            await Task.Delay(1000);

            await GetSensorInfo();
            await Task.Delay(1000);

            await GetSensorData();
            await Task.Delay(1000);
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
            int slaveId = await _sensorService.GetSlaveID();
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


    private async Task GetSensorInfo()
    {
        try
        {
            // 센서 정보 가져오기
            SensorInfo info = await _sensorService.GetSensorInfoAsync();
            SensorInfoReceived?.Invoke(info);
        }
        catch (Exception ex)
        {
            // 예외 처리
            Debug.WriteLine($"Error: GetSensorInfo - {ex.Message}");
        }
    }

    private async Task GetSensorData()
    {
        try
        {
            // 센서 정보 가져오기
            SensorData data = await _sensorService.GetSensorDataAsync();
            SensorDataReceived?.Invoke(data);
        }
        catch (Exception ex)
        {
            // 예외 처리
            Debug.WriteLine($"Error: GetSensorInfo - {ex.Message}");
        }
    }


    //private void OnSensorAttached()
    //{
    //    StartMonitoring();


    //    // LED Ramp 상태 변경
    //    LEDRampReceived?.Invoke(new LEDRamp()
    //    {
    //        Err= ErrStatus.Connected,
    //        Tx = TxStatus.Signal,
    //        Rx = RxStatus.Signal
    //    });
    //}

    //private void OnSensorDetached()
    //{
    //    // LED Ramp 상태 초기화
    //    LEDRampReceived?.Invoke(new LEDRamp()
    //    {
    //        Err = ErrStatus.Disconnected,
    //        Tx = TxStatus.NoSignal,
    //        Rx = RxStatus.NoSignal
    //    });

    //    SensorInfoReceived?.Invoke(new SensorInfo());
    //}


}
