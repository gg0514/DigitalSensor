using CommunityToolkit.Mvvm.ComponentModel;

namespace DigitalSensor.Models;

public partial class ModbusInfo : ObservableObject
{
    [ObservableProperty] private int _slaveID= 2;
}