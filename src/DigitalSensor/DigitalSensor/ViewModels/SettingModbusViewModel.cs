using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingModbusViewModel : ViewModelBase
{
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;


    [ObservableProperty]
    public ModbusInfo _modbusInfo;

    [ObservableProperty]
    private bool isBusy = false;


    public SettingModbusViewModel()
    {
        _modbusInfo = new ModbusInfo();

    }

    public SettingModbusViewModel(IModbusService modbusService, AppSettings settings, NotificationService notificationService)
    {
        _modbusService = modbusService;
        _modbusInfo = settings.ModbusInfo;
        _notificationService = notificationService;

    }


    [RelayCommand]
    private async void Load()
    {
        try
        {
            IsBusy = true;
            Debug.WriteLine($"Load 버튼클릭");

            int slaveID = await _modbusService.VerifyID();
            ModbusInfo.SlaveID = slaveID;

            _notificationService.ShowMessage("정보", $"Current SlaveID : {slaveID}");

            await Task.Delay(1000); // Simulate a delay for loading
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error loading settings: {ex.Message}");
        }
        finally
        {
            // 작업 완료 또는 예외 발생 시 상태 복원
            IsBusy = false;
        }
    }

    [RelayCommand]
    private async void Apply()
    {
        int slaveID = ModbusInfo.SlaveID;

        try
        {
            IsBusy = true;
            Debug.WriteLine($"Apply 버튼클릭");

            await _modbusService.WriteSlaveId((ushort)slaveID);

            _notificationService.ShowMessage("정보", $"Current SlaveID : {slaveID}");

            await Task.Delay(1000); // Simulate a delay for loading
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error applying settings: {ex.Message}");
        }
        finally
        {
            // 작업 완료 또는 예외 발생 시 상태 복원
            IsBusy = false;
        }
    }
}