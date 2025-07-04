﻿using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System;
using System.Threading.Tasks;
using DigitalSensor.Resources;

namespace DigitalSensor.ViewModels;

public partial class SettingModbusViewModel : ViewModelBase
{
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;


    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    public ModbusInfo _modbusInfo;


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
            Console.WriteLine($"Load 버튼클릭");

            int slaveID = await _modbusService.VerifyID();
            ModbusInfo.SlaveID = slaveID;

            _notificationService.ShowMessage(Localize["Information"], $"Current SlaveID : {slaveID}");

            await Task.Delay(1000); // Simulate a delay for loading
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error loading settings: {ex.Message}");
        }
        finally
        {
        }
    }

    [RelayCommand]
    private async void Apply()
    {
        int slaveID = ModbusInfo.SlaveID;

        try
        {
            Console.WriteLine($"Apply 버튼클릭");

            await _modbusService.WriteSlaveId((ushort)slaveID);

            _notificationService.ShowMessage(Localize["Information"], $"Current SlaveID : {slaveID}");

            await Task.Delay(1000); // Simulate a delay for loading
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error applying settings: {ex.Message}");
        }
        finally
        {
        }
    }
}