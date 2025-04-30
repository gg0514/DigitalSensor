using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_1PSampleViewModel : ViewModelBase
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


    public Calib_1PSampleViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService());
        _sensorService = new SensorService();
    }

    public Calib_1PSampleViewModel(IMonitoringService monitoringService, ISensorService sensorService)
    {
        _monitoringService = monitoringService;
        _sensorService = sensorService;

        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;
    }

    [RelayCommand]
    private async void Apply()
    {
        _monitoringService.ApplyCalib = true;

        Debug.WriteLine($"Apply 버튼클릭: {_monitoringService.ApplyCalib}");
    }

    [RelayCommand]
    private async void Abort()
    {
        _monitoringService.AbortCalib = true;

        Debug.WriteLine($"Abort 버튼클릭: {_monitoringService.AbortCalib}");

        // Abort후 상태코드를 받을 수 있는지 체크 필요함
        ResetCallibStatus(1000);
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

        if (CalStatus != CalibrationStatus.CalInProgress)
        {
            ResetCallibStatus();
        }
    }

    private async void ResetCallibStatus(int msec = 5000)
    {
        await Task.Delay(msec); // 5초 대기

        Debug.WriteLine($"ResetCallibStatus: delaytime- {msec}");

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            CalStatus = CalibrationStatus.NoSensorCalibration;
        });
    }

}