using Avalonia.Controls;
using Avalonia.Controls.Notifications;
using Avalonia.Data.Converters;
using Avalonia.Media;
using Avalonia.Threading;
using CommunityToolkit.Mvvm.ComponentModel;
using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using DigitalSensor.USB;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Globalization;
using System.Threading;
using System.Threading.Tasks;


namespace DigitalSensor.ViewModels;


public partial class HomeViewModel : ViewModelBase
{
    private readonly IMonitoringService _monitoringService;
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;


    [ObservableProperty]
    private bool isTxOn;

    [ObservableProperty]
    private bool isRxOn;

    [ObservableProperty]
    private bool isErrOn = true;

    [ObservableProperty]
    private string sensorUnit;

    private CancellationTokenSource _txCts = new();
    private CancellationTokenSource _rxCts = new();
    private CancellationTokenSource _ErrCts = new();


    [ObservableProperty]
    private SensorInfo receivedInfo = new();

    [ObservableProperty]
    private SensorData receivedData = new();


    public HomeViewModel()
    {
        _monitoringService = new MonitoringService(new SensorService(), new AppSettings());
        
        _monitoringService.SensorInfoReceived += OnSensorInfoReceived;
        _monitoringService.SensorDataReceived += OnSensorDataReceived;
    }


    public HomeViewModel(IUsbService usbService, IMonitoringService monitoringService, IModbusService modbusService, NotificationService notificationService)
    {
        // 이벤트구독용
        _monitoringService = monitoringService;
        _modbusService= modbusService;
        _notificationService= notificationService;

        _monitoringService.SensorInfoReceived += OnSensorInfoReceived;
        _monitoringService.SensorDataReceived += OnSensorDataReceived;
        _monitoringService.SensorTypeReceived += OnSensorTypeReceived;
        _monitoringService.SensorValueReceived += OnSensorValueReceived;
        _monitoringService.SensorMvReceived += OnSensorMvReceived;
        _monitoringService.SensorTemperatureReceived += OnSensorTemperatureReceived;


        // LED 구독 등록
        _modbusService.TxSignal += OnTxSignal;
        _modbusService.RxSignal += OnRxSignal;
        _monitoringService.ErrSignal += OnErrSignal;


        string Greeting = LocalizationManager.GetString("Greeting");

        Debug.WriteLine(Greeting); // "안녕하세요" 출력
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
        BlinkLed(ref _ErrCts, val => IsErrOn = val);
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

    private async void OnSensorTypeReceived(int type)
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            ReceivedInfo = new SensorInfo()
            {
                Type = (SensorType)type,
            };

            SensorUnit = UnitMapper.Units[(SensorType)type];
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
    private async void OnSensorMvReceived(float mv)
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            ReceivedData = new SensorData()
            {
                Timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
                Value = ReceivedData.Value,
                Mv = mv,
                Temperature = ReceivedData.Temperature,
            };
        });
    }
    private async void OnSensorTemperatureReceived(float temp)
    {
        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            ReceivedData = new SensorData()
            {
                Timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss"),
                Value = ReceivedData.Value,
                Mv = ReceivedData.Mv,
                Temperature = temp,
            };
        });
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

public class EnumDescriptionConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is Enum enumValue)
        {
            return enumValue.GetDescription();
        }
        return value?.ToString();
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}

public static class UnitMapper
{
    public static readonly Dictionary<SensorType, string> Units = new()
    {
        { SensorType.None, "" },
        { SensorType.TurbidityLow, "NTU" },
        { SensorType.TurbidityHighIR, "NTU" },
        { SensorType.TurbidityHighColor, "NTU" },
        { SensorType.PH, "pH" },
        { SensorType.Conductivity, "㎲/㎝" },
        { SensorType.Chlorine, "㎎/l" },
    };
}
