using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System;
using System.Collections.ObjectModel;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;


public partial class HomeViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;

  

    [ObservableProperty]
    private LEDRamp     ledRamp  = new();

    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();


    public HomeViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService());
        
        _monitoringService.LEDRampReceived += OnLEDRampReceived;
        _monitoringService.SensorInfoReceived += OnSensorInfoReceived;
        _monitoringService.SensorDataReceived += OnSensorDataReceived;
    }


    public HomeViewModel(IMonitoringService monitoringService)
    {
        _monitoringService = monitoringService;

        _monitoringService.LEDRampReceived += OnLEDRampReceived;
        _monitoringService.SensorInfoReceived += OnSensorInfoReceived;
        _monitoringService.SensorDataReceived += OnSensorDataReceived;
    }

    private void OnLEDRampReceived(LEDRamp data)
    {
        LedRamp = data; // UI 자동 갱신
    }

    private void OnSensorInfoReceived(SensorInfo data)
    {


        ReceivedInfo = data; // UI 자동 갱신
    }

    private void OnSensorDataReceived(SensorData data)
    {
        ReceivedData = data; // UI 자동 갱신
    }

}