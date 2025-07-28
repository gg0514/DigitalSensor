using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Services;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System.Runtime;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingCalibViewModel : ViewModelBase
{
    private readonly AppSettings _settings;
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;

    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();

    [ObservableProperty]
    private ModbusInfo _modbusInfo;

    [ObservableProperty]
    private CalibrationAdjust _calibAdjust;


    public SettingCalibViewModel()
    {
        _modbusInfo = new ModbusInfo();
        _calibAdjust = new CalibrationAdjust();
    }

    public SettingCalibViewModel(IModbusService modbusService, AppSettings settings, NotificationService notificationService)
    {
        _settings= settings;
        _modbusService = modbusService;
        _modbusInfo = settings.ModbusInfo;
        _calibAdjust = settings.CalibAdjust;
        _notificationService = notificationService;

    }


    [RelayCommand]
    private async void Apply()
    {
        float factor = CalibAdjust.Factor;
        float offset = CalibAdjust.Offset;

        try
        {
            Console.WriteLine($"Apply 버튼클릭");

            await _modbusService.WriteSensorFactor(factor);
            await Task.Delay(1000); // Simulate a delay for loading

            await _modbusService.WriteSensorOffset(offset);

            // Save the settings to the AppSettings
            await Task.Run(() => _settings.SaveSettings());

            _notificationService.ShowMessage(Localize["Information"], $"Current Sensor Factor: {factor}, Offset: {offset}");
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