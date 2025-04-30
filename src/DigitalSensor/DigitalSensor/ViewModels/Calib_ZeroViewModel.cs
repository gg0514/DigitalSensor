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
    private readonly ISensorService _sensorService;


    [ObservableProperty]
    private CalibrationStatus calStatus;

    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();

    [ObservableProperty]
    private string sensorUnit;


    public Calib_ZeroViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService());
        _sensorService = new SensorService();
    }

    public Calib_ZeroViewModel(IMonitoringService monitoringService, ISensorService sensorService)
    {
        _monitoringService = monitoringService;
        _sensorService = sensorService;

        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;
    }

    [RelayCommand]
    private async void Apply()
    {
        await _sensorService.SetCalibZeroAsync();
    }

    [RelayCommand]
    private async void Abort()
    {
        await _sensorService.SetCalibAbortAsync();
    }

    public async void OnViewLoaded()
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            ReceivedInfo = _monitoringService.SensorInfo;
            ReceivedData = _monitoringService.SensorData;

            var type = ReceivedInfo.Type;
            SensorUnit = UnitMapper.Units[type];
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


    private async void OnCalibStatusReceived(int status)
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            CalStatus = (CalibrationStatus)status;
        });
    }

}

