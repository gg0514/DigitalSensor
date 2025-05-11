using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using DigitalSensor.Services;
using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingCalibViewModel : ViewModelBase
{
    private readonly IModbusService _modbusService;
    private readonly NotificationService _notificationService;

    [ObservableProperty]
    public ModbusInfo _modbusInfo;

    [ObservableProperty]
    public CalibrationAdjust _calibAdjust;


    public SettingCalibViewModel()
    {
        _modbusInfo = new ModbusInfo();
        _calibAdjust = new CalibrationAdjust();
    }

    public SettingCalibViewModel(IModbusService modbusService, AppSettings settings, NotificationService notificationService)
    {
        _modbusService = modbusService;
        _modbusInfo = settings.ModbusInfo;
        _calibAdjust = settings.CalibAdjust;
        _notificationService = notificationService;

    }


    [RelayCommand]
    private async void Apply()
    {
        float factor = _calibAdjust.Factor;
        float offset = _calibAdjust.Offset;

        try
        {
            Debug.WriteLine($"Apply 버튼클릭");

            await _modbusService.WriteSensorFactor(factor);
            await Task.Delay(1000); // Simulate a delay for loading

            await _modbusService.WriteSensorOffset(offset);

            _notificationService.ShowMessage("정보", $"Sensor Factor: {factor}, Offset: {offset}");
        }
        catch (Exception ex)
        {
            Debug.WriteLine($"Error applying settings: {ex.Message}");
        }
        finally
        {
        }

    }
}