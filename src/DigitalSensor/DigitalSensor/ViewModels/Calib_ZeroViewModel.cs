using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class Calib_ZeroViewModel : ViewModelBase
{
    [ObservableProperty]
    public UsbDeviceInfo _usbDevice = new();

    [ObservableProperty]
    public int           _slaveID= 1;

    [ObservableProperty]
    public SerialConn   _serialConn = new();


    public Calib_ZeroViewModel()
    {
    }

    [RelayCommand]
    private void SaveSettings()
    {
        // Save settings logic would go here
    }
}