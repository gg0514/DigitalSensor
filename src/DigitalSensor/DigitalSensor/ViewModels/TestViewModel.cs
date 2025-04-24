
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Net;
using System.Threading.Tasks;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Extensions;
using DigitalSensor.Modbus;
using DigitalSensor.Models;
using DigitalSensor.Services;
using Modbus.Device;

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
    private int deviceId = 5;

    [ObservableProperty]
    private byte slaveId = 250;

    [ObservableProperty]
    private ushort registerAddress = 20;

    [ObservableProperty]
    private byte dataLength = 1;

    [ObservableProperty]
    private string resultText = "Response will appear here";

    public ObservableCollection<string> Results { get; } = new();



    IModbusSerialMaster master = null;



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



    // 안드로이드 테스트시 USB_PERMISSION이 없으면 OpenModbus가 실패함
    // DetectDevice 후에 OpenModbus 호출해야 함

    [RelayCommand]
    private void Read()
    {
        ReadRegisters();
    }



    private async Task ReadRegisters()
    {
        //int deviceId = 5;       // for Desktop
        //int deviceId = 1002;      // for Android (퍼미션 문제로 직접 접근 불가)

        try
        {
            // SlaveId = 250, RegisterAddress = 20, DataLength = 1
            ushort[] values = master.ReadHoldingRegisters(250, 20, 1);

            //ushort[] values= await Task.Run(() =>
            //{
            //    return master.ReadHoldingRegisters(250, 20, 1);
            //});

            int slaveID = values[0];

            string msg= $"Slave ID: {slaveID}";
            Debug.WriteLine(msg);
            _notificationService.ShowMessage("정보", msg);

            Results.Insert(0, $"{DateTime.Now.ToString("[HH:mm:ss] ")} {msg}");
        }
        catch (Exception ex)
        {
            ResultText = $"Error: {ex.Message}";
        }

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
    private async void OpenDevice()
    {
        try
        {
            // Desktop에서 테스트할 때는 DeviceId를 직접 지정
            master = _modbusService.CreateModbusRTU(DeviceId);

            _notificationService.ShowMessage("정보", $"Device {DeviceId} opened successfully.");
        }
        catch (Exception ex)
        {
            ResultText = $"Error: {ex.Message}";
        }
    }


    [RelayCommand]
    private async void CloseDevice()
    {
        try
        {
            master.Dispose();
            master = null;

            _notificationService.ShowMessage("정보", $"Device closed.");
        }
        catch (Exception ex)
        {
            ResultText = $"Error: {ex.Message}";
        }
    }


    [RelayCommand]
    private void DetectDevice()
    {
        try
        {
            var deviceIds = DetectDevices();

            if (deviceIds.Count == 0)
            {
                ResultText = "No devices found.";
                _notificationService.ShowMessage("정보", $"{ResultText}");

                return;
            }

            DeviceId = deviceIds[0];
            _notificationService.ShowMessage("정보", $"Device {DeviceId} detected.");
        }
        catch (Exception ex)
        {
            ResultText = $"Error: {ex.Message}";
        }
    }

    public List<int> DetectDevices()
    {
        IUsbService _usbService = App.GlobalHost.GetService<IUsbService>();

        var devices = _usbService.GetUsbDeviceInfos();

        List<int> deviceIds = new List<int>();

        foreach (var device in devices)
        {
            //if (device.VendorId == 0x0403 && device.ProductId == 0x6001) // FTDI USB Serial Device
            {
                deviceIds.Add(device.DeviceId);
            }
        }

        return deviceIds;
    }

}