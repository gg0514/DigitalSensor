using CommunityToolkit.Mvvm.ComponentModel;

namespace DigitalSensor.Models;

public partial class ModbusDeviceInfo : ObservableObject
{
    [ObservableProperty] private int deviceId;
    [ObservableProperty] private string? productName;

    [ObservableProperty] private int slaveId;
}