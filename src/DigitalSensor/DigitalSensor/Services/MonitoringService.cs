using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DigitalSensor.Services;


public interface IMonitoringService
{
    event Action<Models.SensorData> SensorDataReceived;
    void StartMonitoring();
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;

    public event Action<Models.SensorData> SensorDataReceived;

    public MonitoringService(ISensorService dataService)
    {
        _sensorService = dataService;
    }

    public async void StartMonitoring()
    {
        while (true)
        {
            SensorData data = await _sensorService.GetSensorDataAsync();

            SensorDataReceived?.Invoke(data);

            await Task.Delay(1000); // 1초 대기
        }
    }
}
