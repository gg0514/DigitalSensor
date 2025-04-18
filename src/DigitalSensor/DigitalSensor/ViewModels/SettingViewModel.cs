﻿using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingViewModel : ViewModelBase
{
    [ObservableProperty]
    private SerialConn _serialConn = new();

    [ObservableProperty]
    private UsbDeviceInfo _usbDevice = new();

    public SettingViewModel()
    {
    }

    [RelayCommand]
    private void SaveSettings()
    {
        // Save settings logic would go here
    }
}