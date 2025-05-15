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
        await _monitoringService.ApplyCalib_Zero();

        Debug.WriteLine($"Apply 버튼클릭: {CalInfo.IsRun}");
    }

    [RelayCommand]
    private async void Abort()
    {
        await _monitoringService.AbortCalib();

        Debug.WriteLine($"Abort 버튼클릭: {CalInfo.CalStatus}");
        _notificationService.ShowMessage(Localize["Information"], $"Zero Calibration Aborted");
    }


}

