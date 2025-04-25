using DigitalSensor.Models;
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
    private bool _isRunning;

    public event Action<Models.SensorInfo> SensorInfoReceived;
    public event Action<Models.SensorData> SensorDataReceived;

    public MonitoringService(ISensorService dataService)
    {
        _sensorService = dataService;
    }

    public async void StartMonitoring()
    {
        _isRunning = true;
        while (_isRunning)
        {
            await GetSensorData();  // <-- 비동기 처리
            await Task.Delay(1000);    // 주기
        }
    }

    public async void StopMonitoring()
    {
        _isRunning = false;
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
