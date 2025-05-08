using CommunityToolkit.Mvvm.ComponentModel;

namespace DigitalSensor.Models;

public partial class UsbDeviceInfo : ObservableObject
{
    [ObservableProperty] private int deviceId= 1002;
    [ObservableProperty] private string? productName= "No Device";

    [ObservableProperty] private int vendorId;
    [ObservableProperty] private string? serialNumber;
    [ObservableProperty] private int productId;
    [ObservableProperty] private string? manufacturerName;
    [ObservableProperty] private int interfaceCount;
    [ObservableProperty] private int deviceProtocol;
    [ObservableProperty] private string? deviceName;
    [ObservableProperty] private int configurationCount;
    [ObservableProperty] private string? version;
}