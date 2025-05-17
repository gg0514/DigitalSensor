using Avalonia.Controls;
using Avalonia.Input;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using DigitalSensor.Utils;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_1PSampleViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;
    private readonly NotificationService _notificationService;


    [ObservableProperty]
    private SensorInfo receivedInfo;

    [ObservableProperty]
    private SensorData receivedData;

    [ObservableProperty]
    private CalibInfo _calibInfo;


    [ObservableProperty]
    private float calibValue = 0;

    [ObservableProperty]
    private bool isEditing = false;

    [ObservableProperty]
    private bool isModified = false;

    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();



    public Calib_1PSampleViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
        CalibInfo = _monitoringService.CalibInfo;
    }

    public Calib_1PSampleViewModel(IMonitoringService monitoringService, AppSettings settings, NotificationService notificationService)
    {
        _monitoringService = monitoringService;
        _notificationService = notificationService;

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
        CalibInfo = _monitoringService.CalibInfo;
    }


    public async void OnViewLoaded()
    {
        IsModified = false;
    }

    public async void OnViewUnloaded()
    {

    }

    [RelayCommand]
    private async void UpButton()
    {
        if (!IsModified)
            CalibValue = ReceivedData.Value;

        IsModified = true;

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            CalibValue += 0.01f;
            CalibValue = (float)Math.Round(CalibValue, 2);
        });
    }

    [RelayCommand]
    private async void DownButton()
    {
        if (!IsModified)
            CalibValue = ReceivedData.Value;

        IsModified = true;

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {

            CalibValue -= 0.01f;
            if(CalibValue < 0)    CalibValue = 0;

            CalibValue = (float)Math.Round(CalibValue, 2);
        });
    }



    [RelayCommand]
    private async void Apply()
    {
        await _monitoringService.ApplyCalib_1PSample(CalibValue);

        Debug.WriteLine($"Apply 버튼클릭: Run= {CalibInfo.IsRun}");
    }

    [RelayCommand]
    private async void Abort()
    {
        await _monitoringService.AbortCalib();

        Debug.WriteLine($"Abort 버튼클릭: Status= {CalibInfo.CalStatus}");
        _notificationService.ShowMessage(Localize["Information"], $"1P Sample Calibration Aborted");
    }




    // 코드 비하인드에서 호출되는 메서드
    public void StartEditing()
    {
        // TextBox에 포커스를 주고 편집 모드로 전환
        IsEditing = true;
    }

    public void StopEditing()
    {
        IsEditing = false;
        IsModified = true;
    }
}