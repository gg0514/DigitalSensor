using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
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
    private bool isProgressVisible;

    [ObservableProperty]
    private string applyButtonText = "적 용";


    public Calib_2PBufferViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService());
        _sensorService = new SensorService();
    }

    public Calib_2PBufferViewModel(IMonitoringService monitoringService, ISensorService sensorService)
    {
        _monitoringService = monitoringService;
        _sensorService = sensorService;

        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.CalibStatusReceived += OnCalibStatusReceived;
    }

    [RelayCommand]
    private async void Apply()
    {
        try
        {
            IsBusy = true;
            IsProgressVisible = true;

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
            }
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

        Debug.WriteLine($"Abort 버튼클릭: {_monitoringService.AbortCalib}");

        // Abort후 상태코드를 받을 수 있는지 체크 필요함
        await ResetCallibStatus(1000);
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