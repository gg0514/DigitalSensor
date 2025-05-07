using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingSerialViewModel : ViewModelBase
{
    [ObservableProperty]
    public UsbDeviceInfo _usbDevice = new();

    [ObservableProperty]
    public int           _slaveID= 1;

    [ObservableProperty]
    public CalibrationAdjust _calibAdjust = new();

    [ObservableProperty]
    public SerialConn _serialConn = new();

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
    private string? _selectedParity = "None";

    [ObservableProperty]
    private string? _selectedStopbits = "1";

    public SettingSerialViewModel()
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
                "None",
                "Odd",
                "Even"
            };
        _serialStopbits = new ObservableCollection<string>
            {
                "1",
                "2"
            };
    }

    [RelayCommand]
    private void SaveSettings()
    {
        // Save settings logic would go here
    }
}