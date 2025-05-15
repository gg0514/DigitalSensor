using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using DigitalSensor.Utils;

using FluentAvalonia.UI.Controls;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_2PBufferViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;
    private readonly NotificationService _notificationService;

    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private bool isVisible;

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
    private bool isProgressVisible= false;



    public Calib_2PBufferViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());
        _modbusInfo = new ModbusInfo();

    }

    public Calib_2PBufferViewModel(IMonitoringService monitoringService, AppSettings settings, NotificationService notificationService)
    {
        _monitoringService = monitoringService;
        _modbusInfo = settings.ModbusInfo;


        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;
        _notificationService = notificationService;
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

    public async void OnViewUnloaded()
    {

    }


    [RelayCommand]
    private async void Apply()
    {
        try
        {
            IsProgressVisible = true;
            ModbusInfo.IsAlive = false;

            CalStatus = CalibrationStatus.CalInProgress;
            await _monitoringService.ApplyCalib_2PBuffer();

            Debug.WriteLine($"Apply 버튼클릭: {CalStatus}");
        }
        catch (Exception ex)
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

        _notificationService.ShowMessage(Localize["Information"], $"2P Buffer Calibration Aborted");

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
            // 10초 후에 상태를 초기화
            await ResetCallibStatus(10000);
        }
    }

    private async Task ResetCallibStatus(int msec = 5000)
    {
        Debug.WriteLine($"ResetCallibStatus: delaytime- {msec}");

        await Task.Delay(msec);

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            CalStatus = CalibrationStatus.NoSensorCalibration;

            //if (_sensorAttached)
            {
                ModbusInfo.IsAlive = true;
                IsProgressVisible = false;
            }
        });
    }

}