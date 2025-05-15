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
    private readonly ISensorService _sensorService;
    private readonly NotificationService _notificationService;

    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    private bool _sensorAttached = false;

    [ObservableProperty]
    private bool isVisible;

    [ObservableProperty]
    private CalibrationStatus calStatus;

    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();

    [ObservableProperty]
    private string sensorUnit;


    [ObservableProperty]
    private ModbusInfo _modbusInfo;

    [ObservableProperty]
    private bool isBusy;

    [ObservableProperty]
    private bool isProgressVisible= false;


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
        _notificationService = notificationService;

        // Sensor 구독 등록
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;

        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;

        isProgressVisible = false;
        IsBusy = false; // 초기값 설정
    }



    partial void OnIsBusyChanged(bool value)
    {
    }

    private async void OnSensorAttached(UsbDeviceInfo info)
    {
        _sensorAttached = true;
        //OnPropertyChanged(nameof(IsAbortButtonEnabled));
        //OnPropertyChanged(nameof(IsApplyButtonEnabled));
    }

    private async void OnSensorDetached()
    {
        _sensorAttached = false;
        //OnPropertyChanged(nameof(IsAbortButtonEnabled));
        //OnPropertyChanged(nameof(IsApplyButtonEnabled));
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

        Debug.WriteLine($"OnViewLoaded: {ReceivedInfo.Type} - {SensorUnit}");
    }

    public async void OnViewUnloaded()
    {

        Debug.WriteLine($"OnViewUnloaded: {ReceivedInfo.Type} - {SensorUnit}");
    }


    [RelayCommand]
    private async void Apply()
    {

        try
        {
            IsBusy = true;
            IsProgressVisible = true;
            ModbusInfo.IsAlive = false;

            CalStatus = CalibrationStatus.CalInProgress;
            await _monitoringService.ApplyCalib_Zero();

            Debug.WriteLine($"Apply 버튼클릭: {CalStatus}");
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

        Debug.WriteLine($"Abort 버튼클릭: {CalStatus}");

        // Abort후 상태코드를 받을 수 있는지 체크 필요함
        await ResetCallibStatus(500);

        _notificationService.ShowMessage(Localize["Information"], $"Zero Calibration Aborted");
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
        CalStatus = (CalibrationStatus)status;
        Debug.WriteLine($"OnCalibStatusReceived: {CalStatus}");


        if (CalStatus != CalibrationStatus.CalInProgress)
        {
            // 2초 후에 상태를 초기화
            await ResetCallibStatus(2000);
        }
    }

    private async Task ResetCallibStatus(int msec= 5000)
    {
        Debug.WriteLine($"ResetCallibStatus: delaytime- {msec}");

        await Task.Delay(msec);

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            // 교정상태 주석처리
            //CalStatus = CalibrationStatus.NoSensorCalibration;

            // 중간에 케이블이 빠지는 경우 고려하지 않음
            //if (_sensorAttached)
            {
                ModbusInfo.IsAlive = true;
                IsProgressVisible = false;
            }
        });
    }
}

