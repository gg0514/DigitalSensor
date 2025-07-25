using Avalonia.Controls;
using Avalonia.Controls.Notifications;
using Avalonia.Data.Converters;
using Avalonia.Media;
using Avalonia.Threading;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using DigitalSensor.USB;
using DigitalSensor.Utils;

using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization;
using System.Runtime;
using System.Threading;
using System.Threading.Tasks;


namespace DigitalSensor.ViewModels;


public partial class HomeViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;


    [ObservableProperty]
    private SensorInfo receivedInfo;

    [ObservableProperty]
    private SensorData receivedData;


    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private bool isTxOn;

    [ObservableProperty]
    private bool isRxOn;

    [ObservableProperty]
    private bool isErrOn = true;

    [ObservableProperty]
    private bool isLampVisible = false;

    private CancellationTokenSource _txCts = new();
    private CancellationTokenSource _rxCts = new();
    private CancellationTokenSource _ErrCts = new();



    public HomeViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = _monitoringService.SensorInfo;
        ReceivedData = _monitoringService.SensorData;
    }


    public HomeViewModel(IUsbService usbService, IMonitoringService monitoringService, IModbusService modbusService, NotificationService notificationService)
    {
        // 이벤트구독용
        _monitoringService = monitoringService;
        _modbusService= modbusService;
        _notificationService= notificationService;

        // 이것으로 이벤트핸들러를 대체하는 효과
        ReceivedInfo = monitoringService.SensorInfo;
        ReceivedData = monitoringService.SensorData;

        ReceivedInfo.PropertyChanged += OnReceivedInfoPropertyChanged;

        // LED 구독 등록
        _modbusService.TxSignal += OnTxSignal;
        _modbusService.RxSignal += OnRxSignal;
        _monitoringService.ErrSignal += OnErrSignal;

    }

    private void OnReceivedInfoPropertyChanged(object sender, PropertyChangedEventArgs e)
    {
        if (e.PropertyName == nameof(SensorInfo.Type))
        {
            Console.WriteLine($"SensorType changed to: {ReceivedInfo.Type}");

            switch(ReceivedInfo.Type)
            {
                case SensorType.TurbidityLow:
                case SensorType.TurbidityHighColor:
                case SensorType.TurbidityHighIR:
                //case SensorType.PH:
                    IsLampVisible = true;
                    break;
                default:
                    IsLampVisible = false;
                    break;
            }

        }
    }

    [RelayCommand]
    private async void Lamp(bool? isChecked)
    {
        try
        {
            Console.WriteLine($"램프 버튼클릭- {isChecked}");

            //bool bChecked= LampButton.IsChecked ?? false;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error Lamp Button: {ex.Message}");
        }
        finally
        {
        }
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
}
