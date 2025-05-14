using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using DigitalSensor.Utils;

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_ZeroViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;
    private readonly ISensorService _sensorService;
    private readonly NotificationService _notificationService;

    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private ModbusInfo _modbusInfo;

    [ObservableProperty]
    private CalibrationStatus calStatus;

    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();

    [ObservableProperty]
    private string sensorUnit;

    [ObservableProperty]
    private bool isBusy;

    [ObservableProperty]
    private bool isProgressVisible;

    [ObservableProperty]
    private string applyButtonText = "적 용";


    public Calib_ZeroViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());
        _sensorService = new SensorService();
        _modbusInfo = new ModbusInfo();
    }

    public Calib_ZeroViewModel(IMonitoringService monitoringService, ISensorService sensorService, AppSettings settings, NotificationService notificationService)
    {
        _monitoringService = monitoringService;
        _sensorService = sensorService;
        _modbusInfo = settings.ModbusInfo;

        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;
        _notificationService = notificationService;
    }

    [RelayCommand]
    private async void Apply()
    {
        try
        {
            IsBusy = true;
            IsProgressVisible = true;
            ApplyButtonText = " ...";

            CalStatus = CalibrationStatus.CalInProgress;
            await _monitoringService.ApplyCalib_Zero();

            Debug.WriteLine($"Apply 버튼클릭: {CalStatus}");

            await WaitForCalibrationCompletion();
            _notificationService.ShowMessage(Localize["Information"], $"Zero Calibration Completed");
        }
        finally
        {
            // 작업 완료 또는 예외 발생 시 상태 복원
            IsBusy = false;
            IsProgressVisible = false;
            ApplyButtonText = "적 용";
        }
    }

    [RelayCommand]
    private async void Abort()
    {
        await _monitoringService.AbortCalib();

        Debug.WriteLine($"Abort 버튼클릭: {CalStatus}");

        // Abort후 상태코드를 받을 수 있는지 체크 필요함
        await ResetCallibStatus(500);

        _notificationService.ShowMessage(Localize["Information"], $"Zero Calibration Aborted");
    }

    private async Task WaitForCalibrationCompletion()
    {
        //await Task.Delay(1000); 

        while(CalStatus == CalibrationStatus.CalInProgress)
        {
            // Calibration이 완료될 때까지 대기
            await Task.Delay(500); // 0.5초 대기
        }
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

        if(CalStatus != CalibrationStatus.CalInProgress)
        {
            ResetCallibStatus();
        }
    }

    private async Task ResetCallibStatus(int msec= 5000)
    {
        await Task.Delay(msec); 

        Debug.WriteLine($"ResetCallibStatus: delaytime- {msec}");

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            CalStatus = CalibrationStatus.NoSensorCalibration;
        });
    }
}

