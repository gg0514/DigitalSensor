using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using DigitalSensor.USB;
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
    private readonly NotificationService _notificationService;


    [ObservableProperty]
    private SensorInfo receivedInfo;

    [ObservableProperty]
    private SensorData receivedData;

    [ObservableProperty]
    private CalInfo _calInfo;




    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private bool isVisible;


    public Calib_ZeroViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
        CalInfo = _monitoringService.CalInfo;
    }

    public Calib_ZeroViewModel(IMonitoringService monitoringService, AppSettings settings, NotificationService notificationService)
    {
        _monitoringService = monitoringService;
        _notificationService = notificationService;

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
        CalInfo = _monitoringService.CalInfo;
    }


    public async void OnViewLoaded()
    {
    }

    public async void OnViewUnloaded()
    {
        Debug.WriteLine($"OnViewUnloaded: {ReceivedInfo.Type} - {ReceivedInfo.SensorUnit}");
    }


    [RelayCommand]
    private async void Apply()
    {

        try
        {
            await _monitoringService.ApplyCalib_Zero();

            Debug.WriteLine($"Apply 버튼클릭: {CalInfo.IsRun}");
        }
        catch(Exception ex)
        {
            // 예외 처리
            Debug.WriteLine($"Error during calibration: {ex.Message}");
            _notificationService.ShowMessage(Localize["Error"], $"Error during calibration: {ex.Message}");

            await ResetCallibStatus(1000);
        }
    }

    [RelayCommand]
    private async void Abort()
    {
        await _monitoringService.AbortCalib();

        Debug.WriteLine($"Abort 버튼클릭: {CalInfo.CalStatus}");

        // Abort후 상태코드를 받을 수 있는지 체크 필요함
        await ResetCallibStatus(500);

        _notificationService.ShowMessage(Localize["Information"], $"Zero Calibration Aborted");
    }

    private async Task ResetCallibStatus(int msec= 5000)
    {
        Debug.WriteLine($"ResetCallibStatus: delaytime- {msec}");

        await Task.Delay(msec);

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            // 교정상태 주석처리
            //CalStatus = CalibrationStatus.NoSensorCalibration;
        });
    }
}

