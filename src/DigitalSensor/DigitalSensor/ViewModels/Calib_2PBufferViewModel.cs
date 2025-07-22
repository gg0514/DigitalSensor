using Avalonia.Controls;
using Avalonia.Threading;
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
using System.Threading;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_2PBufferViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;


    [ObservableProperty]
    private SensorInfo receivedInfo;

    [ObservableProperty]
    private SensorData receivedData;

    [ObservableProperty]
    private CalibInfo _calibInfo;


    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private bool isTxOn;

    [ObservableProperty]
    private bool isRxOn;

    [ObservableProperty]
    private bool isErrOn = true;

    private CancellationTokenSource _txCts = new();
    private CancellationTokenSource _rxCts = new();
    private CancellationTokenSource _ErrCts = new();

    public Calib_2PBufferViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
        CalibInfo = _monitoringService.CalibInfo;
    }

    public Calib_2PBufferViewModel(IMonitoringService monitoringService, IModbusService modbusService, NotificationService notificationService)
    {
        _monitoringService = monitoringService;
        _modbusService = modbusService;
        _notificationService = notificationService;

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
        CalibInfo = _monitoringService.CalibInfo;


        // LED 구독 등록
        _modbusService.TxSignal += OnTxSignal;
        _modbusService.RxSignal += OnRxSignal;
        _monitoringService.ErrSignal += OnErrSignal;

        // 캘리브레이션 완료 이벤트 구독
        //_monitoringService.CalibrationCompleted += OnCallibrationCompleted;
    }


    public void OnTxSignal()
    {
        IsErrOn = false;

        BlinkLed(ref _txCts, val => IsTxOn = val);
    }

    public void OnRxSignal()
    {
        IsErrOn = false;

        BlinkLed(ref _rxCts, val => IsRxOn = val);
    }

    public void OnErrSignal()
    {
        // Err신호는 Tx/Rx와 다르게 LED가 꺼지지 않도록 설정
        IsErrOn = true;
    }

    //public void OnCallibrationCompleted()
    //{
    //    UpdateCalibOrderGuide();
    //}


    //private void UpdateCalibOrderGuide()
    //{
    //    int calibOrder = _monitoringService.CalibOrder;

    //    if (calibOrder == 0)
    //        CalibOrderGuide = Localize["2PGuide1_1P"];
    //    else
    //        CalibOrderGuide = Localize["2PGuide1_2P"];
        
    //    Console.WriteLine($"UpdateCalibOrderGuide - {CalibOrderGuide}");
    //}


    private void BlinkLed(ref CancellationTokenSource cts, Action<bool> setState)
    {
        // 이전 작업 취소
        cts.Cancel();
        cts = new CancellationTokenSource();

        _ = BlinkAsync(setState, cts.Token);
    }

    private async Task BlinkAsync(Action<bool> setState, CancellationToken token)
    {
        try
        {
            await Dispatcher.UIThread.InvokeAsync(() => setState(true));
            await Task.Delay(100, token); // 100ms 동안 켜짐
            await Dispatcher.UIThread.InvokeAsync(() => setState(false));
        }
        catch (OperationCanceledException) { /* 취소된 경우 무시 */ }
    }

    public async void OnViewLoaded()
    {
        //UpdateCalibOrderGuide();
    }

    public async void OnViewUnloaded()
    {

    }


    [RelayCommand]
    private async void Apply()
    {
        bool bMonitoring = _monitoringService.IsMonitoring;

        if (bMonitoring)
        {
            // 버튼 반응성 향상 목적
            CalibInfo.IsRun = true;
            await _monitoringService.ApplyCalib_2PBuffer();
        }


        Console.WriteLine($"Apply 버튼클릭: Monitoring = {bMonitoring}");
    }

    [RelayCommand]
    private async void Abort()
    {
        await _monitoringService.AbortCalib();

        Console.WriteLine($"Abort 버튼클릭: {CalibInfo.CalStatus}");
        _notificationService.ShowMessage(Localize["Information"], $"2P Buffer Calibration Aborted");

    }

}