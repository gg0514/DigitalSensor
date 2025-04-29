using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_ZeroViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;


    [ObservableProperty]
    private string calib_Status;

    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();

    [ObservableProperty]
    private string sensorUnit;


    public Calib_ZeroViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService());
    }

    public Calib_ZeroViewModel(IMonitoringService monitoringService)
    {
        _monitoringService = monitoringService;

        _monitoringService.SensorTypeReceived += OnSensorTypeReceived;
        _monitoringService.SensorValueReceived += OnSensorValueReceived;
    }


    [RelayCommand]
    private void Apply()
    {
    }

    [RelayCommand]
    private void Abort()
    {
    }

    private async void OnSensorTypeReceived(int type)
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            ReceivedInfo = new SensorInfo()
            {
                Type = (SensorType)type,
            };

            SensorUnit = UnitMapper.Units[(SensorType)type];
        });
    }

    private async void OnSensorValueReceived(float value)
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            ReceivedData = new SensorData()
            {
                Timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
                Value = value,
                Mv = ReceivedData.Mv,
                Temperature = ReceivedData.Temperature,
            };
        });
    }

}

