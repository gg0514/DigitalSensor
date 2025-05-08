using Avalonia.Controls;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using DigitalSensor.Models;
using System.Collections.ObjectModel;
using System.IO.Ports;
using System.Threading.Tasks;

namespace DigitalSensor.ViewModels;

public partial class SettingViewModel : ViewModelBase
{
    [ObservableProperty]
    public UsbDeviceInfo _usbDevice;

    [ObservableProperty]
    public ModbusInfo _modbusInfo;

    [ObservableProperty]
    public CalibrationAdjust _calibAdjust;

    [ObservableProperty]
    public SerialConn _serialConn;



    public SettingViewModel()
    {
        UsbDevice = new UsbDeviceInfo();
        ModbusInfo = new ModbusInfo();
        CalibAdjust = new CalibrationAdjust();
        SerialConn = new SerialConn();
    }

    public SettingViewModel(UsbDeviceInfo usbDeviceInfo, ModbusInfo modbusInfo, CalibrationAdjust calibAdjust, SerialConn serialConn)
    {
        UsbDevice = usbDeviceInfo;
        ModbusInfo = modbusInfo;
        CalibAdjust = calibAdjust;
        SerialConn = serialConn;
    }


}