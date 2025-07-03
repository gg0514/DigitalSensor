using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingSerialViewModel : ViewModelBase
{
    private AppSettings _settings;
    private SerialConn _serialConn;
    private readonly NotificationService _notificationService;


    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private ObservableCollection<string> _serialBaudrate;

    [ObservableProperty]
    private ObservableCollection<string> _serialDatabits;

    [ObservableProperty]
    private ObservableCollection<string> _serialParity;

    [ObservableProperty]
    private ObservableCollection<string> _serialStopbits;

    [ObservableProperty]
    private string? _selectedBaudrate = "9600";

    [ObservableProperty]
    private string? _selectedDatabits = "8";

    [ObservableProperty]
    private string? _selectedParity = "0";

    [ObservableProperty]
    private string? _selectedStopbits = "1";



    public SettingSerialViewModel()
    {
        _serialConn = new SerialConn();

        InitCombo();
    }

    public SettingSerialViewModel(AppSettings settings, NotificationService notificationService)
    {
        _settings = settings;
        _serialConn = settings.SerialConn;
        _notificationService = notificationService;

        InitCombo();
    }


    private void InitCombo()
    {
        // 초기 항목 설정
        _serialBaudrate = new ObservableCollection<string>
            {
                "9600",
                "19200",
                "57600",
                "115200"
            };
        _serialDatabits = new ObservableCollection<string>
            {
                "8",
                "7"
            };
        _serialParity = new ObservableCollection<string>
            {
                "0",
                "1",
                "2"
            };
        _serialStopbits = new ObservableCollection<string>
            {
                "1",
                "2"
            };



        _selectedBaudrate = _serialConn.BaudRate;
        _selectedDatabits = _serialConn.DataBits;
        _selectedParity = _serialConn.Parity;
        _selectedStopbits = _serialConn.StopBits;
    }


    [RelayCommand]
    private async void Apply()
    {
        // Save settings logic would go here

        string baudRate = SelectedBaudrate ?? "9600";
        string dataBits = SelectedDatabits ?? "8";
        string parity = SelectedParity ?? "0";
        string stopBits = SelectedStopbits ?? "1";

        Console.WriteLine($"Apply 버튼클릭: {baudRate}, {dataBits}, {parity}, {stopBits}");

        // Update the SerialConn object
        _serialConn.BaudRate = baudRate;
        _serialConn.DataBits = dataBits;
        _serialConn.Parity = parity;
        _serialConn.StopBits = stopBits;

        // Save the settings to the AppSettings
        await Task.Run(() => _settings.SaveSettings());

        _notificationService.ShowMessage(Localize["Information"], $"Serial Info: {baudRate}, {dataBits}, {parity}, {stopBits}");
    }

}