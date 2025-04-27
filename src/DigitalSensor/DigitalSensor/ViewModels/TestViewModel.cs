
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
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace DigitalSensor.ViewModels;

//public partial class MainViewModel : ViewModelBase
//{
//    public string Greeting { get; } = "Welcome to Avalonia!";
//}


public partial class TestViewModel : ViewModelBase
{
    private readonly NotificationService    _notificationService;
    private readonly IModbusService          _modbusService;

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



    // for Design
    public TestViewModel()
    {
        int i = 0;
    }

    // for Runtime
    public TestViewModel(IModbusService modbusService, NotificationService notificationService)
    {
        _modbusService = modbusService;
        _notificationService = notificationService;
    }



    // 안드로이드 테스트시 USB_PERMISSION이 없으면 OpenModbus가 실패함
    // DetectDevice 후에 OpenModbus 호출해야 함


    [RelayCommand]
    private async Task Test()
    {
        try
        {
            // SlaveId = 250, RegisterAddress = 20, DataLength = 1
            //ushort[] values= await Task.Run(() =>
            //{
            //    return master.ReadHoldingRegisters(250, 20, 1);
            //});

            ushort[] values = await _modbusService.ReadSlaveId();

            int slaveID = values[0];

            string msg = $"Slave ID: {slaveID}";
            Debug.WriteLine(msg);
            _notificationService.ShowMessage("정보", msg);

            string msg2 = $"{DateTime.Now.ToString("[HH:mm:ss] ")} {msg}";

            await RunOnUiAsync(() =>
            {
                Results.Insert(0, msg2);
                return Task.CompletedTask;
            });
        }
        catch (Exception ex)
        {
            await RunOnUiAsync(() =>
            {
                ResultText = $"Error: {ex.Message}";
                return Task.CompletedTask;
            });
        }
    }



    [RelayCommand]
    private void Read()
    {
        ReadRegisters();
    }



    private async Task ReadRegisters()
    {
        try
        {
            byte slaveId = SlaveId;
            ushort startAddress = RegisterAddress;
            ushort numRegisters = DataLength;

            string result = await _modbusService.ReadHoldingRegisters(slaveId, startAddress, numRegisters);
            Debug.WriteLine(result);

            string msg2 = $"{DateTime.Now.ToString("[HH:mm:ss] ")} {result}";

            await RunOnUiAsync(() =>
            {
                Results.Insert(0, msg2);
                return Task.CompletedTask;
            });
        }
        catch (Exception ex)
        {
            await RunOnUiAsync(() =>
            {
                ResultText = $"Error: {ex.Message}";
                return Task.CompletedTask;
            });
        }
    }

    [RelayCommand]
    private async Task OpenDevice()
    {
        //int deviceId = 5;       // for Desktop
        //int deviceId = 1002;      // for Android (퍼미션 문제로 직접 접근 불가)

        try
        {
            // Desktop에서 테스트할 때는 DeviceId를 직접 지정
            await _modbusService.Open(DeviceId);

            _notificationService.ShowMessage("정보", $"Device {DeviceId} opened successfully.");
        }
        catch (Exception ex)
        {
            await RunOnUiAsync(() =>
            {
                ResultText = $"Error: {ex.Message}";
                return Task.CompletedTask;
            });
        }
    }


    [RelayCommand]
    private async Task CloseDevice()
    {
        try
        {
            await _modbusService.Close();

            _notificationService.ShowMessage("정보", $"Device closed.");
        }
        catch (Exception ex)
        {
            await RunOnUiAsync(() =>
            {
                ResultText = $"Error: {ex.Message}";
                return Task.CompletedTask;
            });
        }
    }


    [RelayCommand]
    private async Task DetectDevice()
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

            //await RunOnUiAsync(() =>
            //{
            //    DeviceId = deviceIds[0];
            //    _notificationService.ShowMessage("정보", $"Device {DeviceId} detected.");
            //    return Task.CompletedTask;
            //});
        }
        catch (Exception ex)
        {
            await RunOnUiAsync(() =>
            {
                ResultText = $"Error: {ex.Message}";
                return Task.CompletedTask;
            });
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