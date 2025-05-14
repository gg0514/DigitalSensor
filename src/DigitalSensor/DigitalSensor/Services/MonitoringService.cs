using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.ViewModels;
using FluentAvalonia.UI.Controls;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;

namespace DigitalSensor.Services;


public interface IMonitoringService
{
    SensorInfo SensorInfo { get; set; }
    SensorData SensorData { get; set; }
    CommandStatus CommandStatus { get; set; }

    event Action<Models.SensorInfo> SensorInfoReceived;
    event Action<Models.SensorData> SensorDataReceived;

    event Action ErrSignal;

    event Action<int> SensorTypeReceived;
    event Action<float> SensorValueReceived;
    event Action<float> SensorMvReceived;
    event Action<float> SensorTemperatureReceived;
    event Action<int> CalibStatusReceived;

    void SetCurrentPage(string pageName);

    Task StartMonitoring();
    Task StopMonitoring();

    // 영점 교정
    Task ApplyCalib_Zero();
    // 1점 샘플
    Task ApplyCalib_1PSample(float value);
    // 2점 버퍼
    Task ApplyCalib_2PBuffer();
    // 교정 취소
    Task AbortCalib();
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;
    private readonly ModbusInfo _modbusInfo;

    private bool _isRunning = false;

    private bool _bApplyCalib = false;
    private bool _bAbortCalib = false;

    private float _calibValue = 0;

    private string _currentPage = string.Empty;

    public CommandStatus CommandStatus { get; set; } = CommandStatus.Ready;


    private CalibrationStatus CalStatus = CalibrationStatus.NoSensorCalibration;

    public SensorInfo SensorInfo { get; set; } = new();
    public SensorData SensorData { get; set; } = new();


    public event Action ErrSignal;
    public event Action<Models.SensorInfo> SensorInfoReceived;
    public event Action<Models.SensorData> SensorDataReceived;

    public event Action<int> SensorTypeReceived;
    public event Action<float> SensorValueReceived;
    public event Action<float> SensorMvReceived;
    public event Action<float> SensorTemperatureReceived;
    public event Action<int> CalibStatusReceived;


    public MonitoringService()
    {
        _sensorService = new SensorService();
        _modbusInfo = new ModbusInfo();
        // Sensor 구독 등록
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;
    }

    public MonitoringService(ISensorService dataService, AppSettings settings)
    {
        _sensorService = dataService;
        _modbusInfo = settings.ModbusInfo;

        // 상태 초기화
        _modbusInfo.IsAlive = false;

        // Sensor 구독 등록
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;
    }


    private async void OnSensorAttached(UsbDeviceInfo deviceInfo)
    {
        try
        {
            int slaveId = await RetrieveSensorID();

            await UpdateInfo(deviceInfo, slaveId);
            await StartMonitoring();
        }
        catch (Exception ex)
        {
            Debug.WriteLine("정보", $"OnSensorAttached() failed");
        }

        // 센서 진단
        //callHealthCheck();
    }

    private async void OnSensorDetached()
    {
        await StopMonitoring();
    }


    public void SetCurrentPage(string pageName)
    {
        _currentPage = pageName;

        Debug.WriteLine($"CurrentPage: {_currentPage}");
    }

    public async Task<int> RetrieveSensorID()
    {
        Debug.WriteLine($"Monitoring - RetrieveSensorID ...");

        return await _sensorService.RetrieveID();
    }

    public async Task ApplyCalib_Zero()
    {
        _bApplyCalib = true;

        await Task.Delay(100);
    }

    public async Task ApplyCalib_1PSample(float value)
    {
        _bApplyCalib = true;
        _calibValue = value;

        await Task.Delay(100);
    }

    public async Task ApplyCalib_2PBuffer()
    {
        _bApplyCalib = true;

        await Task.Delay(100);
    }

    public async Task AbortCalib()
    {
        _bAbortCalib = true;

        ResetCallibStatus();

        await Task.Delay(100);
    }


    public async Task StartMonitoring()
    {
        _isRunning = true;
        _modbusInfo.IsAlive = true;

        while (_isRunning)
        {
            if (_currentPage == "Home")                    
                await NormalMode();
            else if (_currentPage.Contains("Setting"))           
                await SettingMode();
            else                                          
                await CalibMode();

            await Task.Delay(1000); // 1초 대기
        }
    }

    public async Task StopMonitoring()
    {
        // 초기화
        _isRunning = false;
        _modbusInfo.IsAlive = false;

        ErrSignal?.Invoke();

        SensorInfo = new SensorInfo()
        {
            Type = SensorType.None
        };
        SensorInfoReceived?.Invoke(SensorInfo);

        SensorData = new SensorData
        {
            Value = 0,
            Mv = 0,
            Temperature = 0
        };
        SensorDataReceived?.Invoke(SensorData);
    }


    private async Task NormalMode()
    {
        try
        {
            Debug.WriteLine($"[ NormalMode ] ");

            await GetSensorType();
            await GetSensorValue();
            await GetSensorMv();
            await GetSensorTemperature();
        }
        catch(Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기
            Debug.WriteLine($"[ NormalMode - Error ] {ex.Message}");
        }
    }

    private async Task SettingMode()
    {
        try
        {
            Debug.WriteLine($"[ SettingMode ] ");

            await Task.Delay(1000); // 1초 대기
        }
        catch (Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기
            Debug.WriteLine($"[ SettingMode - Error ] {ex.Message}");
        }
    }

    private async Task CalibMode()
    {
        try
        {
            Debug.WriteLine($"[ CalibMode ] ");

            if (_bAbortCalib)
            {
                // 교정 중단
                await WriteCalibAbortAsync();

                // 교정중단후 상태를 읽어도 진행중으로 나옴!!
                //await ReadCalibStatus();
            }
            else if (_bApplyCalib)
            {
                // 교정 실행
                await WriteCalibAsync();
            }
            else
            {
                await GetSensorValue();
            }
        }
        catch (Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기
            Debug.WriteLine($"[ CalibMode - Error] {ex.Message}");
        }
    }



    private async Task WriteCalibAsync()
    {
        if (_currentPage == "Calib_Zero")
        {
            await WriteZeroCalibAsync();
        }
        else if (_currentPage == "Calib_1PSample")
        {
            await Write1PSampleCalibAsync();
        }
        else if (_currentPage == "Calib_2PBuffer")
        {
            await Write2PBufferCalibAsync();
        }
    }

    private async Task WriteZeroCalibAsync()
    {
        if (CommandStatus != CommandStatus.Running)
        {
            CommandStatus = CommandStatus.Running;

            await _sensorService.SetCalibZeroAsync();
            Debug.WriteLine($"[ 영점 교정 실행 ] ");

            await WaitForCalibrationCompletion();

            // 교정 상태 초기화
            ResetCallibStatus();
        }
    }

    private async Task Write1PSampleCalibAsync()
    {
        if (CommandStatus != CommandStatus.Running)
        {
            CommandStatus = CommandStatus.Running;

            await _sensorService.SetCalib1PSampleAsync(_calibValue);
            Debug.WriteLine($"[ 1PSample 교정 실행 ] ");

            await WaitForCalibrationCompletion();

            // 교정 상태 초기화
            ResetCallibStatus();
        }
    }

    private async Task Write2PBufferCalibAsync()
    {
        if (CommandStatus != CommandStatus.Running)
        {
            CommandStatus = CommandStatus.Running;

            // 1P Buffer 교정
            int calibOrder = 0;
            await _sensorService.SetCalib2PBufferAsync(calibOrder);
            Debug.WriteLine($"[ 1PBuffer 교정 실행 ] ");

            await WaitForCalibrationCompletion();

            string title = "2P Buffer";
            string message = $"2번째 버퍼 교정을 시작하시겠습니까?";
            bool bResult = await ShowConfirmationAsync(title, message);

            // 2P Buffer 교정
            calibOrder = 1;
            await _sensorService.SetCalib2PBufferAsync(calibOrder);
            Debug.WriteLine($"[ 2PBuffer 교정 실행 ] ");

            await WaitForCalibrationCompletion();


            // 교정 상태 초기화
            ResetCallibStatus();
        }
    }




    private async Task WriteCalibAbortAsync()
    {
        if (CommandStatus == CommandStatus.Running)
        {
            Debug.WriteLine($"[ 교정 중단 ] ");
            await _sensorService.SetCalibAbortAsync();
        }
    }

    private async Task WaitForCalibrationCompletion()
    {
        // 폴링 주기
        await Task.Delay(2000);
        await ReadCalibStatus();

        while (CalStatus == CalibrationStatus.CalInProgress)
        {
            // Calibration이 완료될 때까지 대기
            await Task.Delay(2000);
            await ReadCalibStatus();
        }

        if (CalStatus == CalibrationStatus.CalOK)   Debug.WriteLine($" => 교정 성공!! ");
        else                                        Debug.WriteLine($" => 교정 실패!! ");
    }


    private async Task ReadCalibStatus()
    {
        int status = await _sensorService.GetCalibStatusAsync();
        
        CalibStatusReceived?.Invoke(status);

        CalStatus = (CalibrationStatus)status;
        Debug.WriteLine($"ReadCalibStatus: {CalStatus}");
    }

    private void ResetCallibStatus()
    {
        _bApplyCalib = false;
        _bAbortCalib = false;
        CommandStatus = CommandStatus.Ready;
    }


    private async Task GetSensorType()
    {
        int type = await _sensorService.GetTypeAsync();

        if (type > 0)
        {
            SensorInfo = new SensorInfo()
            {
                Type = (SensorType)type,
            };

            SensorTypeReceived?.Invoke(type);
        }
    }

    private async Task GetSensorValue()
    {
        float value = await _sensorService.GetValueAsync();
        SensorData = new SensorData
        {
            Value = value,
            Mv = SensorData.Mv,
            Temperature = SensorData.Temperature
        };


        Debug.WriteLine($"SensorValue: {value}");
        SensorValueReceived?.Invoke(value);
    }

    private async Task GetSensorMv()
    {
        float mv = await _sensorService.GetMVAsync();
        SensorData = new SensorData
        {
            Value = SensorData.Value,
            Mv = mv,
            Temperature = SensorData.Temperature
        };

        Debug.WriteLine($"SensorMv: {mv}");
        SensorMvReceived?.Invoke(mv);
    }

    private async Task GetSensorTemperature()
    {
        float temperature = await _sensorService.GetTemperatureAsync();
        SensorData = new SensorData
        {
            Value = SensorData.Value,
            Mv = SensorData.Mv,
            Temperature = temperature
        };

        Debug.WriteLine($"SensorTemperature: {temperature}");
        SensorTemperatureReceived?.Invoke(temperature);
    }



    private async Task UpdateInfo(UsbDeviceInfo usbInfo, int slaveId)
    {
        _modbusInfo.DeviceId = usbInfo.DeviceId;
        _modbusInfo.ProductName = usbInfo.ProductName;
        _modbusInfo.SlaveID = slaveId;

        //SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

        //await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        //{
        //    vm.ModbusInfo = newslaveId;
        //});
    }

    public async Task<bool> ShowConfirmationAsync(string title, string message)
    {
        var dialog = new ContentDialog
        {
            Title = title,
            Content = message,
            PrimaryButtonText = "확인",
            CloseButtonText = "취소",
            DefaultButton = ContentDialogButton.Primary
        };

        var result = await dialog.ShowAsync();
        return result == ContentDialogResult.Primary;
    }

}
