using CommunityToolkit.Mvvm.ComponentModel;
using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.Utils;
using DigitalSensor.ViewModels;
using FluentAvalonia.UI.Controls;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Net.Mail;
using System.Threading;
using System.Threading.Tasks;

namespace DigitalSensor.Services;


public interface IMonitoringService
{
    SensorInfo  SensorInfo { get; set; }
    SensorData  SensorData { get; set; }
    CalibInfo     CalibInfo { get; set; }

    event Action ErrSignal;

    event Action<int> SensorTypeReceived;
    event Action<float> SensorValueReceived;

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

public partial class MonitoringService : ObservableObject, IMonitoringService
{
    private readonly ISensorService _sensorService;
    private readonly ModbusInfo _modbusInfo;

    [ObservableProperty]
    private SensorInfo _sensorInfo= new();

    [ObservableProperty]
    private SensorData _sensorData= new();

    [ObservableProperty]
    private CalibInfo  _calibInfo = new();



    private bool _isMonitoring = false;
    private bool _isCalibration = false;

    private float _calibValue = 0;

    private string _currentPage = string.Empty;


    public event Action ErrSignal;

    public event Action<int> SensorTypeReceived;
    public event Action<float> SensorValueReceived;

    private CancellationTokenSource? _calibrationCts;


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
        await ResetCallibStatus();
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


    public async Task StartMonitoring()
    {
        _isMonitoring = true;
        _modbusInfo.IsAlive = true;

        while (_isMonitoring)
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
        _isMonitoring = false;
        _modbusInfo.IsAlive = false;

        ErrSignal?.Invoke();


        // 이렇게 하면 안됨
        // 왜냐면, 외부에서 참조하고 있는 SensorInfo, SensorData가 초기화됨
        // 즉, 데이터의 소스가 없어지는 것임
        //SensorInfo = new();
        //SensorData = new();

        SensorInfo.Type = SensorType.None;
        SensorInfo.SensorUnit = string.Empty;

        SensorData.Value = 0;
        SensorData.Mv = 0;
        SensorData.Temperature = 0;
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
            Debug.WriteLine($"NormalMode - Error : {ex.Message}");
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
            Debug.WriteLine($"SettingMode - Error: {ex.Message}");
        }
    }

    private async Task CalibMode()
    {
        try
        {
            if (_isCalibration)
            {
                Debug.WriteLine($"[ CalibMode ] ");

                // 교정 실행
                CalibInfo.IsRun = true;
                CalibInfo.CalStatus = CalibrationStatus.NoSensorCalibration;

                _calibrationCts = new CancellationTokenSource();
                await WriteCalibAsync(_calibrationCts.Token);
            }
            else
            {
                Debug.WriteLine($"[ Calibration - Ready] ");

                await GetSensorType();
                await GetSensorValue();
            }
        }
        catch (Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기
            Debug.WriteLine($"CalibMode - Error: {ex.Message}");

            // 에러발생시 센서 재연결
            await _sensorService.Close();
            await _sensorService.Open();


            // 교정 상태 초기화
            ResetCallibStatus();
            CalibInfo.CalStatus = CalibrationStatus.NoSensorCalibration;
        }
    }




    public Task ApplyCalib_Zero()
    {
        _isCalibration = true;

        return Task.CompletedTask;
    }

    public Task ApplyCalib_1PSample(float value)
    {
        _isCalibration = true;
        _calibValue = value;

        return Task.CompletedTask;
    }

    public Task ApplyCalib_2PBuffer()
    {
        _isCalibration = true;

        return Task.CompletedTask;
    }

    public Task AbortCalib()
    {
        _calibrationCts?.Cancel();
        _calibrationCts?.Dispose();
        _calibrationCts = null;

        return Task.CompletedTask;
    }



    private async Task WriteCalibAsync(CancellationToken token)
    {
        try
        {
            if (_currentPage == "Calib_Zero")
            {
                await WriteZeroCalibAsync(token);
            }
            else if (_currentPage == "Calib_1PSample")
            {
                await Write1PSampleCalibAsync(token);
            }
            else if (_currentPage == "Calib_2PBuffer")
            {
                await Write2PBufferCalibAsync(token);
            }
        }
        catch (OperationCanceledException)
        {
            await WriteCalibAbortAsync();
        }
    }

    private async Task WriteZeroCalibAsync(CancellationToken token)
    {
        await _sensorService.SetCalibZeroAsync();
        Debug.WriteLine($" => Zero 교정 실행 ");

        await WaitForCalibrationCompletion(token);

        // 교정 상태 초기화
        ResetCallibStatus();
    }

    private async Task Write1PSampleCalibAsync(CancellationToken token)
    {
        await _sensorService.SetCalib1PSampleAsync(_calibValue);
        Debug.WriteLine($" => 1PSample 교정 실행 ");

        await WaitForCalibrationCompletion(token);

        // 교정 상태 초기화
        ResetCallibStatus();
    }

    private async Task Write2PBufferCalibAsync(CancellationToken token)
    {
        // 2P Buffer 교정
        int calibOrder = 0;
        await _sensorService.SetCalib2PBufferAsync(calibOrder);
        Debug.WriteLine($" => 2PBuffer - 1st 교정 실행 ");

        await WaitForCalibrationCompletion(token);

        string title = "2P Buffer";
        string message = $"2번째 버퍼 교정을 시작하시겠습니까?";
        bool bResult = await ShowConfirmationAsync(title, message);

        if(bResult)
        {
            // 2P Buffer 교정
            calibOrder = 1;
            await _sensorService.SetCalib2PBufferAsync(calibOrder);
            Debug.WriteLine($" => 2PBuffer - 2nd 교정 실행 ");

            await WaitForCalibrationCompletion(token);
        }

        // 교정 상태 초기화
        ResetCallibStatus();
    }


    private async Task WriteCalibAbortAsync()
    {
        Debug.WriteLine($" => 교정 중단!! ");
        await _sensorService.SetCalibAbortAsync();

        // 교정 상태 초기화
        ResetCallibStatus();

        // 교정결과 초기화
        CalibInfo.CalStatus = CalibrationStatus.NoSensorCalibration;

    }

    private async Task WaitForCalibrationCompletion(CancellationToken token)
    {
        token.ThrowIfCancellationRequested();

        // 폴링 주기
        await Task.Delay(2000, token);
        await ReadCalibStatus();

        while (CalibInfo.CalStatus == CalibrationStatus.CalInProgress)
        {
            token.ThrowIfCancellationRequested();
            // Calibration이 완료될 때까지 대기
            await Task.Delay(2000, token);
            await ReadCalibStatus();
        }

        if (CalibInfo.CalStatus == CalibrationStatus.CalOK)   Debug.WriteLine($" => 교정 성공!! ");
        else                                        Debug.WriteLine($" => 교정 실패!! ");
    }


    private async Task ReadCalibStatus()
    {
        int status = await _sensorService.GetCalibStatusAsync();
        
        CalibInfo.CalStatus = (CalibrationStatus)status;
        Debug.WriteLine($"ReadCalibStatus: {CalibInfo.CalStatus}");
    }

    private Task ResetCallibStatus()
    {
        _isCalibration = false;

        // 교정실행 여부는 초기화
        CalibInfo.IsRun = false;

        // 교정결과는 마지막 교정상태를 유지
        // CalibInfo.CalStatus = CalibrationStatus.NoSensorCalibration;

        return Task.CompletedTask;
    }






    private async Task GetSensorType()
    {
        int type = await _sensorService.GetTypeAsync();

        if (type > 0)
        {
            SensorInfo.Type = (SensorType)type;
            SensorInfo.SensorUnit = UnitMapper.Units[(SensorType)type];

            // 메뉴 비활성화에 대한 이벤트 발생
            SensorTypeReceived?.Invoke(type);
        }
    }

    private async Task GetSensorValue()
    {
        float value = await _sensorService.GetValueAsync();

        SensorData.Value = value;

        Debug.WriteLine($"SensorValue: {value}");
        SensorValueReceived?.Invoke(value);
    }

    private async Task GetSensorMv()
    {
        float mv = await _sensorService.GetMVAsync();

        SensorData.Mv = mv;
        Debug.WriteLine($"SensorMv: {mv}");
    }

    private async Task GetSensorTemperature()
    {
        float temperature = await _sensorService.GetTemperatureAsync();

        SensorData.Temperature = temperature;
        Debug.WriteLine($"SensorTemperature: {temperature}");
    }



    private async Task UpdateInfo(UsbDeviceInfo usbInfo, int slaveId)
    {
        _modbusInfo.DeviceId = usbInfo.DeviceId;
        _modbusInfo.ProductName = usbInfo.ProductName;
        _modbusInfo.SlaveID = slaveId;
    }

    public async Task<bool> ShowConfirmationAsync(string title, string message)
    {
        var dialog = new ContentDialog
        {
            Title = title,
            Content = message,
            PrimaryButtonText = "OK",
            CloseButtonText = "Cancel",
            DefaultButton = ContentDialogButton.Primary
        };

        var result = await dialog.ShowAsync();
        return result == ContentDialogResult.Primary;
    }

}
