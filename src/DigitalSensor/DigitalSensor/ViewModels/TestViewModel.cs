
using System;
using System.Collections.ObjectModel;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Extensions;
using DigitalSensor.Services;

namespace DigitalSensor.ViewModels;

//public partial class MainViewModel : ViewModelBase
//{
//    public string Greeting { get; } = "Welcome to Avalonia!";
//}


public partial class TestViewModel : ViewModelBase
{
    private readonly NotificationService    _notificationService;
    private readonly ModbusService          _modbusService;

    [ObservableProperty]
    private int deviceId = 11;

    [ObservableProperty]
    private byte slaveId = 250;

    [ObservableProperty]
    private ushort registerAddress = 20;

    [ObservableProperty]
    private byte dataLength = 1;

    [ObservableProperty]
    private string resultText = "Response will appear here";

    public ObservableCollection<ushort> RegisterValues { get; } = new();

    // for Design
    public TestViewModel()
    {
    }

    // for Runtime
    public TestViewModel(ModbusService modbusService)
    {
        _modbusService = modbusService;
        _notificationService = App.GlobalHost.GetService<NotificationService>();
    }

    [RelayCommand]
    private async Task ReadRegistersAsync()
    {
        //try
        //{
        //    // SlaveId = 250, RegisterAddress = 20, DataLength = 1
        //    var values = await _modbusService.ReadUsbSerialAdapter(SlaveId, RegisterAddress, DataLength);

        //    RegisterValues.Clear();

        //    foreach (var value in values)
        //    {
        //        RegisterValues.Add(value);
        //    }

        //    ResultText = $"Response: {string.Join(", ", values)}";
        //}
        //catch (Exception ex)
        //{
        //    ResultText = $"Error: {ex.Message}";
        //}
    }

    [RelayCommand]
    private void DetectDevice()
    {
        //try
        //{
        //    //var values = await _modbusService.ReadHoldingRegistersAsync(PortName, SlaveId, RegisterAddress, DataLength);
        //    var deviceIds = _modbusService.DetectDevices();

        //    if (deviceIds.Count == 0)
        //    {
        //        ResultText = "No devices found.";
        //        _notificationService.ShowMessage("정보", $"{ResultText}");

        //        return;
        //    }

        //    DeviceId = deviceIds[0];
        //    _notificationService.ShowMessage("정보", $"Device {DeviceId} detected.");
        //}
        //catch (Exception ex)
        //{
        //    ResultText = $"Error: {ex.Message}";
        //}
    }


    [RelayCommand]
    private void OpenDevice()
    {
        //try
        //{
        //    bool bOpen = _modbusService.OpenDevice(DeviceId);

        //    ResultText = $"result: {bOpen}";

        //    _notificationService.ShowMessage("정보", $"Device {DeviceId} opened successfully.");
        //}
        //catch (Exception ex)
        //{
        //    ResultText = $"Error: {ex.Message}";
        //}
    }
}