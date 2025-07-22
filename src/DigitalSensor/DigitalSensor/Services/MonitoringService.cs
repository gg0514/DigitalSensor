using CommunityToolkit.Mvvm.ComponentModel;
using DigitalSensor.Extensions;
using DigitalSensor.Models;
using DigitalSensor.Resources;
using DigitalSensor.Utils;
using DigitalSensor.ViewModels;
using FluentAvalonia.UI.Controls;
using FluentIcons.Common.Internals;
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
    CalibInfo   CalibInfo { get; set; }

    bool    IsMonitoring { get; }

    event Action ErrSignal;

    event Action<int> SensorTypeReceived;
    event Action<float> SensorValueReceived;

    event Action CalibrationCompleted;

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


    // 다국어 지원을 위한 Localize 객체
    public Localize Localize { get; } = new();


    public bool IsMonitoring => _isMonitoring;


    private bool _isMonitoring = false;
    private bool _isCalibration = false;

    private float   _calibValue = 0;
    private int     _failCount = 0;     // 실패 횟수
    private int     _calibOrder = 0;    // 2P Buffer 교정 순서

    private string _currentPage = string.Empty;


    public event Action ErrSignal;

    public event Action<int> SensorTypeReceived;
    public event Action<float> SensorValueReceived;

    public event Action CalibrationCompleted;


    private CancellationTokenSource? _calibrationCts;


    public MonitoringService()
    {
        _sensorService = new SensorService();
        _modbusInfo = new ModbusInfo();

        // 2P 버퍼교정 순서 초기화
        CalibInfo.CalibOrderGuide = Localize["2PGuide1_1P"];

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

        // 2P 버퍼교정 순서 초기화
        CalibInfo.CalibOrderGuide = Localize["2PGuide1_1P"];

        // Sensor 구독 등록
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;
    }


    private async void OnSensorAttached(UsbDeviceInfo deviceInfo)
    {
        try
        {
            int slaveId = await RetrieveSensorID();

            if(slaveId < 0)
            {
                Console.WriteLine($"Monitoring - RetrieveSensorID FAIL!! ");
                ErrSignal?.Invoke();

                return;
            }

            await UpdateInfo(deviceInfo, slaveId);
            await StartMonitoring();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"OnSensorAttached() failed");
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
        Console.WriteLine($"CurrentPage: {_currentPage}");

        // 교정 상태 초기화
        ResetCallibStatus();

        // 2P 버퍼교정 순서 초기화
        ResetCallibOrder();

        // 교정결과 초기화
        CalibInfo.CalStatus = CalibrationStatus.NoSensorCalibration;
    }

    public async Task<int> RetrieveSensorID()
    {
        Console.WriteLine($"Monitoring - ");
        Console.WriteLine($"Monitoring - RetrieveSensorID ...");

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
            Console.WriteLine($"[ NormalMode ] ");

            await GetSensorType();
            await GetSensorValue();
            await GetSensorMv();
            await GetSensorTemperature();
        }
        catch(Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기
            Console.WriteLine($"NormalMode - Error : {ex.Message}");
        }
    }

    private async Task SettingMode()
    {
        try
        {
            Console.WriteLine($"[ SettingMode ] ");

            await Task.Delay(1000); // 1초 대기
        }
        catch (Exception ex)
        {
            ErrSignal?.Invoke();
            await Task.Delay(1000); // 1초 대기
            Console.WriteLine($"SettingMode - Error: {ex.Message}");
        }
    }

    private async Task CalibMode()
    {
        try
        {
            if (_isCalibration)
            {
                Console.WriteLine($"[ CalibMode ] ");

                // 교정 실행
                CalibInfo.IsRun = true;
                CalibInfo.CalStatus = CalibrationStatus.NoSensorCalibration;

                _calibrationCts = new CancellationTokenSource();
                await WriteCalibAsync(_calibrationCts.Token);
            }
            else
            {
                Console.WriteLine($"[ Calibration - Ready] ");

                await GetSensorType();
                await GetSensorValue();
            }
        }
        catch (Exception ex)
        {
            ErrSignal?.Invoke();

            Console.WriteLine($"CalibMode - Error: {ex.Message}");

            // 에러발생시 센서 재연결
            await Task.Delay(1000); // 1초 대기
            await _sensorService.Close();

            await Task.Delay(2000); // 2초 대기
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
        // 교정중단 예외만 처리, 나머지는 위로
        catch (OperationCanceledException)
        {
            await WriteCalibAbortAsync();
        }
    }

    private async Task WriteZeroCalibAsync(CancellationToken token)
    {
        await _sensorService.SetCalibZeroAsync();
        Console.WriteLine($" => Zero 교정 실행 ");

        await WaitForCalibrationCompletion(token);

        // 교정 상태 초기화
        ResetCallibStatus();
    }

    private async Task Write1PSampleCalibAsync(CancellationToken token)
    {
        await _sensorService.SetCalib1PSampleAsync(_calibValue);
        Console.WriteLine($" => 1PSample 교정 실행 ");

        await WaitForCalibrationCompletion(token);

        // 교정 상태 초기화
        ResetCallibStatus();
    }

    private async Task Write2PBufferCalibAsync(CancellationToken token)
    {
        await _sensorService.SetCalib2PBufferAsync(_calibOrder);
        Console.WriteLine($" => 2PBuffer {_calibOrder+1}번째 교정 실행 ");

        await WaitForCalibrationCompletion(token);

        if(CalibInfo.CalStatus == CalibrationStatus.CalOK)
        {
            if(_calibOrder==0)
                _calibOrder++;

            // 교정 완료 이벤트 전송
            //CalibrationCompleted?.Invoke();

            // 2P 버퍼교정 순서 초기화
            CalibInfo.CalibOrderGuide = Localize["2PGuide1_2P"];
        }

        // 교정 상태 초기화
        ResetCallibStatus();
    }

    //private async Task Write2PBufferCalibAsync(CancellationToken token)
    //{
    //    // 1번째 교정을 시작하시겠습니까?
    //    string title = "2P Buffer";
    //    string message = LocalizationManager.GetString("2PBuffer_Message1");
    //    bool bResult = await ShowConfirmationAsync(title, message);

    //    if (bResult)
    //    {
    //        // 교정순서
    //        int calibOrder = 0;
    //        await _sensorService.SetCalib2PBufferAsync(calibOrder);
    //        Console.WriteLine($" => 2PBuffer - 1st 교정 실행 ");

    //        await WaitForCalibrationCompletion(token);

    //        // 2번째 교정을 시작하시겠습니까?
    //        message = LocalizationManager.GetString("2PBuffer_Message2");
    //        bResult = await ShowConfirmationAsync(title, message);

    //        if (bResult)
    //        {
    //            calibOrder = 1;
    //            await _sensorService.SetCalib2PBufferAsync(calibOrder);
    //            Console.WriteLine($" => 2PBuffer - 2nd 교정 실행 ");

    //            await WaitForCalibrationCompletion(token);
    //        }
    //    }

    //    // 교정 상태 초기화
    //    ResetCallibStatus();
    //}


    private async Task WriteCalibAbortAsync()
    {
        Console.WriteLine($" => 교정 중단!! ");
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

        _failCount = 0; // 실패 횟수 초기화
        await ReadCalibStatus();

        while (CalibInfo.CalStatus == CalibrationStatus.CalInProgress)
        {
            token.ThrowIfCancellationRequested();
            // Calibration이 완료될 때까지 대기
            await Task.Delay(2000, token);
            await ReadCalibStatus();
        }

        if (CalibInfo.CalStatus == CalibrationStatus.CalOK)   
            Console.WriteLine($" => 교정 성공!! ");
        else                                        
            Console.WriteLine($" => 교정 실패!! ");
    }


    private async Task ReadCalibStatus()
    {
        int status = await _sensorService.GetCalibStatusAsync();
        
        CalibrationStatus calStatus = (CalibrationStatus)status;

        if(calStatus== CalibrationStatus.Fail_GeneralCalFail)
        {
            _failCount++;
            Console.WriteLine($" => 교정 실패 횟수: {_failCount}회");

            if (_failCount >= 3)
            {
                Console.WriteLine($" => 교정 실패 횟수 초과: {_failCount}회");
                CalibInfo.CalStatus = CalibrationStatus.Fail_GeneralCalFail;
                return;
            }
        }
        else
        {
            CalibInfo.CalStatus = calStatus;
        }

        Console.WriteLine($"ReadCalibStatus: {CalibInfo.CalStatus}");
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

    private void ResetCallibOrder()
    {
        _calibOrder= 0;

        // 2P 버퍼교정 순서 초기화
        CalibInfo.CalibOrderGuide= Localize["2PGuide1_1P"];

        Console.WriteLine($"2P 버퍼교정 초기화 - CalibOrder= {_calibOrder}");
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

        Console.WriteLine($"SensorValue: {value}");
        SensorValueReceived?.Invoke(value);
    }

    private async Task GetSensorMv()
    {
        float mv = await _sensorService.GetMVAsync();

        SensorData.Mv = mv;
        Console.WriteLine($"SensorMv: {mv}");
    }

    private async Task GetSensorTemperature()
    {
        float temperature = await _sensorService.GetTemperatureAsync();

        SensorData.Temperature = temperature;
        Console.WriteLine($"SensorTemperature: {temperature}");
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
