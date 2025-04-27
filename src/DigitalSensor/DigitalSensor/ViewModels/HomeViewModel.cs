using Avalonia.Controls;
using Avalonia.Data.Converters;
using Avalonia.Media;
using Avalonia.Threading;
using CommunityToolkit.Mvvm.ComponentModel;
using DigitalSensor.Extensions;
using DigitalSensor.Modbus;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;


public partial class HomeViewModel : ViewModelBase
{
    private readonly IUsbService _usbService;
    private readonly IMonitoringService _monitoringService;
    private readonly IModbusService _modbusService;


    [ObservableProperty]
    private bool isTxOn;

    [ObservableProperty]
    private bool isRxOn;

    [ObservableProperty]
    private bool isErrOn= true;

    private CancellationTokenSource _txCts = new();
    private CancellationTokenSource _rxCts = new();


    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();


    public HomeViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService());
        
        _monitoringService.SensorInfoReceived += OnSensorInfoReceived;
        _monitoringService.SensorDataReceived += OnSensorDataReceived;
    }


    public HomeViewModel(IUsbService usbService, IMonitoringService monitoringService, IModbusService modbusService)
    {
        // 이벤트구독용
        _usbService = usbService;
        _monitoringService = monitoringService;
        _modbusService= modbusService;

        _monitoringService.SensorInfoReceived += OnSensorInfoReceived;
        _monitoringService.SensorDataReceived += OnSensorDataReceived;

        // USB Device 구독 등록
        _usbService.UsbPermissionGranted += OnUSBPermissionGranted;
        _usbService.UsbDeviceDetached += OnUSBDeviceDetached;

        // LED 구독 등록
        _modbusService.TxSignal += OnTxSignal;
        _modbusService.RxSignal += OnRxSignal;
    }


    private async void OnUSBPermissionGranted(UsbDeviceInfo deviceInfo)
    {
        try
        {
            await Task.Delay(1000); // 1초 대기

            IsErrOn = false;
            _monitoringService.StartMonitoring();
        }
        catch (Exception ex)
        {
        }

        // 센서 진단
        //callHealthCheck();
    }

    private async void OnUSBDeviceDetached(UsbDeviceInfo deviceInfo)
    {
        //_notificationService.ShowMessage("정보", $"Device closed.");
        IsErrOn = true;

    }

    public void OnTxSignal()
    {
        BlinkLed(ref _txCts, val => IsTxOn = val);
    }

    public void OnRxSignal()
    {
        BlinkLed(ref _rxCts, val => IsRxOn = val);
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


    private void OnSensorInfoReceived(SensorInfo data)
    {


        ReceivedInfo = data; // UI 자동 갱신
    }

    private void OnSensorDataReceived(SensorData data)
    {
        ReceivedData = data; // UI 자동 갱신
    }

}

public class BoolToBrushConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is bool boolValue)
        {
            // parameter가 있을 경우 (예: "Red" 전달되면 붉은색으로 변환)
            if (parameter is string colorName)
            {
                switch (colorName)
                {
                    case "Red":
                        return boolValue ? Brushes.Red : Brushes.Gray;
                    case "Orange":
                        return boolValue ? Brushes.Orange : Brushes.Gray;
                    case "GreenYellow":
                        return boolValue ? Brushes.GreenYellow : Brushes.Gray;
                    default:
                        return boolValue ? Brushes.Green : Brushes.Gray;
                }
            }

            // parameter가 없으면 기본값 사용
            return boolValue ? Brushes.Green : Brushes.Gray;
        }

        return Brushes.Gray;  // 기본값
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}