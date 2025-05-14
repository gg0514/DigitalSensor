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
    private readonly ISensorService _sensorService;
    private readonly NotificationService _notificationService;

    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    private bool _sensorAttached = false;

    [ObservableProperty]
    private ModbusInfo _modbusInfo;

    [ObservableProperty]
    private CalibrationStatus calStatus;

    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();

    [ObservableProperty]
    private int calibOrder = 0;


    [ObservableProperty]
    private string sensorUnit;

    [ObservableProperty]
    private bool isBusy;

    [ObservableProperty]
    private bool isProgressVisible= false;

    [ObservableProperty]
    private string applyButtonText;


    public Calib_2PBufferViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());
        _sensorService = new SensorService();
        _modbusInfo = new ModbusInfo();

        applyButtonText = Localize["Apply"];
    }

    public Calib_2PBufferViewModel(IMonitoringService monitoringService, ISensorService sensorService, AppSettings settings, NotificationService notificationService)
    {
        _monitoringService = monitoringService;
        _sensorService = sensorService;
        _modbusInfo = settings.ModbusInfo;

        applyButtonText = Localize["Apply"];

        // Sensor 구독 등록
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;

        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;
        _notificationService = notificationService;
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

    [RelayCommand]
    private async void Apply()
    {
        try
        {
            IsBusy = true;
            IsProgressVisible = true;
            ModbusInfo.IsAlive = false;


            CalStatus = CalibrationStatus.CalInProgress;

            ApplyButtonText = $" ({CalibOrder})";
            await _monitoringService.ApplyCalib_2PBuffer(CalibOrder);
            Debug.WriteLine($"Apply 버튼클릭: 버퍼번호 {CalibOrder}");

            // 1번 Calibration이 완료될 때까지 대기
            await WaitForCalibrationCompletion();


            CalibOrder++;

            string title = "2P Buffer";
            string message = $"2번째 버퍼 교정을 시작하시겠습니까?";
            bool bResult= await ShowConfirmationAsync(title, message);

            if(bResult)
            {
                ApplyButtonText = $" ({CalibOrder})";
                await _monitoringService.ApplyCalib_2PBuffer(CalibOrder);
                Debug.WriteLine($"Apply 버튼클릭: 버퍼번호 {CalibOrder}");

                // 2번 Calibration이 완료될 때까지 대기
                await WaitForCalibrationCompletion();

                _notificationService.ShowMessage(Localize["Information"], $"2P Buffer Calibration Completed");
            }
        }
        catch (Exception ex)
        {
            // 예외 처리
            Debug.WriteLine($"Error during calibration: {ex.Message}");
            _notificationService.ShowMessage(Localize["Error"], $"Error during calibration: {ex.Message}");

            // 작업 완료 또는 예외 발생 시 상태 복원
            await ResetCallibStatus(1000);
        }
        finally
        {
            // 작업 완료 또는 예외 발생 시 상태 복원
            await ResetCallibStatus(1000);
        }
    }

    [RelayCommand]
    private async void Abort()
    {
        await _monitoringService.AbortCalib();

        Debug.WriteLine($"Abort 버튼클릭: {_monitoringService.AbortCalib}");

        // Abort후 상태코드를 받을 수 있는지 체크 필요함
        await ResetCallibStatus(1000);

        _notificationService.ShowMessage(Localize["Information"], $"2P Buffer Calibration Aborted");

    }
    private async Task WaitForCalibrationCompletion()
    {
        //await Task.Delay(1000); 

        while (CalStatus == CalibrationStatus.CalInProgress)
        {
            // Calibration이 완료될 때까지 대기
            await Task.Delay(500); // 0.5초 대기
        }
    }

    public async void OnViewLoaded()
    {
        CalibOrder = 0;

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
            await ResetCallibStatus();
        }
    }

    private async Task ResetCallibStatus(int msec = 5000)
    {
        await Task.Delay(msec); // 5초 대기

        if (_sensorAttached)
        {
            ModbusInfo.IsAlive = true;
            IsProgressVisible = false;
        }

        Debug.WriteLine($"ResetCallibStatus: delaytime- {msec}");

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            CalStatus = CalibrationStatus.NoSensorCalibration;
        });
    }

    public async Task<bool> ShowConfirmationAsync(string title, string message)
    {
        var dialog = new ContentDialog
        {
            Title = title,
            Content = message,
            PrimaryButtonText = "확인",
            CloseButtonText = "취소",
            DefaultButton = ContentDialogButton.Primary
        };

        var result = await dialog.ShowAsync();
        return result == ContentDialogResult.Primary;
    }

}