using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Services;


public interface IMonitoringService
{
    event Action<Models.LEDRamp>    LEDRampReceived;
    event Action<Models.SensorInfo> SensorInfoReceived;
    event Action<Models.SensorData> SensorDataReceived;

    void StartMonitoring();
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;

    public event Action<Models.LEDRamp>    LEDRampReceived;
    public event Action<Models.SensorInfo> SensorInfoReceived;
    public event Action<Models.SensorData> SensorDataReceived;

    public MonitoringService(ISensorService dataService)
    {
        _sensorService = dataService;
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;
    }

    private void OnSensorAttached()
    {
        StartMonitoring();


        // LED Ramp 상태 변경
        LEDRampReceived?.Invoke(new LEDRamp()
        {
            Err= ErrStatus.Connected,
            Tx = TxStatus.Signal,
            Rx = RxStatus.Signal
        });
    }

    private void OnSensorDetached()
    {
        // Sensor Detached 통지
        //LEDRampReceived?.Invoke(new LEDRamp());


        // LED Ramp 상태 초기화
        LEDRampReceived?.Invoke(new LEDRamp()
        {
            Err = ErrStatus.Disconnected,
            Tx = TxStatus.NoSignal,
            Rx = RxStatus.NoSignal
        });

        SensorInfoReceived?.Invoke(new SensorInfo());
    }


    public async void StartMonitoring()
    {

        // 센서 정보 가져오기
        SensorInfo info = await _sensorService.GetSensorInfoAsync();
        SensorInfoReceived?.Invoke(info);

        //SensorData data = await _sensorService.GetSensorDataAsync();
        //SensorDataReceived?.Invoke(data);

        while (true)
        {
            // 센서 데이터 가져오기
            SensorData data = await _sensorService.GetSensorDataAsync();
            SensorDataReceived?.Invoke(data);

            await Task.Delay(1000); // 1초 대기
        }
    }
}
