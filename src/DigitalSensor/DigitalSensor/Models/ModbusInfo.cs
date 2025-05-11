using CommunityToolkit.Mvvm.ComponentModel;

namespace DigitalSensor.Models;

public partial class ModbusInfo : ObservableObject
{
    [ObservableProperty] private int _deviceId = 1002;
    [ObservableProperty] private string? _productName = "No Device";
    [ObservableProperty] private int _slaveID = 2;
    [ObservableProperty] private bool _isAlive = false;
}