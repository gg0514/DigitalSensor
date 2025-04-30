using DigitalSensor.Extensions;
using DigitalSensor.Modbus;
using DigitalSensor.Models;
using DigitalSensor.ViewModels;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;

namespace DigitalSensor.Services;


public interface IMonitoringService
{
    SensorInfo SensorInfo { get; set; }
    SensorData SensorData { get; set; }

    event Action<Models.SensorInfo> SensorInfoReceived;
    event Action<Models.SensorData> SensorDataReceived;

    event Action ErrSignal;

    event Action<int> SensorTypeReceived;
    event Action<float> SensorValueReceived;
    event Action<float> SensorMvReceived;
    event Action<float> SensorTemperatureReceived;
    event Action<int> CalibStatusReceived;

    void SetCurrentPage(string pageName);

    Task InitSlaveID();
    Task StartMonitoring();
    Task StopMonitoring();

    bool ApplyCalib { get; set; }
    bool AbortCalib { get; set; }
    CommandStatus CommandStatus { get; set; }
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;

    private bool _isOpen = false;
    private bool _isSensorInfo = false;
    private bool _isSensorType = false;
    private bool _isRunning = false;

    private string _currentPage = string.Empty;

    public bool ApplyCalib { get; set; } = false;
    public bool AbortCalib { get; set; } = false;
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


    public MonitoringService(ISensorService dataService)
    {
        _sensorService = dataService;

        // Sensor 구독 등록
        _sensorService.SensorAttached += OnSensorAttached;
        _sensorService.SensorDetached += OnSensorDetached;
    }


    private async void OnSensorAttached()
    {
        try
        {
            //await Task.Delay(200);
            await InitSlaveID();
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

    public async Task InitSlaveID()
    {
        int slaveId = await _sensorService.InitSlaveID();

        if (slaveId > 0)
            await UpdateSlaveID(slaveId);
        else
            throw new Exception("Failed at InitSlaveID.");
    }

    public async Task StartMonitoring()
    {
        _isRunning = true;

        while (_isRunning)
        {                
            if(_currentPage == "Home")                    await NormalMode();
            else if (_currentPage == "Setting")           await SettingMode();
            else                                          await CalibMode();
        }
    }

    public async Task StopMonitoring()
    {
        // 초기화
        _isOpen = false;
        _isSensorInfo = false;
        _isSensorType = false;
        _isRunning = false;


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

            await GetSensorValue();
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

            if (ApplyCalib)
            {
                await WriteCalibZeroAsync();
                await ReadCalibStatus();
            }
            else if (AbortCalib)
            {
                await WriteCalibAbortAsync();
                await ReadCalibStatus();
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



    private async Task WriteCalibZeroAsync()
    {
        if(CommandStatus != CommandStatus.Running)
        {
            Debug.WriteLine($"[ 교정 실행 ] ");

            await _sensorService.SetCalibZeroAsync();
            CommandStatus = CommandStatus.Running;
        }
    }

    private async Task WriteCalibAbortAsync()
    {
        if (CommandStatus != CommandStatus.Running)
        {
            Debug.WriteLine($"[ 교정 중단 ] ");

            await _sensorService.SetCalibAbortAsync();
            CommandStatus = CommandStatus.Running;
        }
    }

    private async Task ReadCalibStatus()
    {
        int status = await _sensorService.GetCalibStatusAsync();
        
        CalibStatusReceived?.Invoke(status);
        CalStatus = (CalibrationStatus)status;

        if (CalStatus == CalibrationStatus.CalInProgress)
        {
            Debug.WriteLine($"[ 교정 상태 ] 진행중 !!");
        }
        else if (CalStatus == CalibrationStatus.CalOK)
        {
            Debug.WriteLine($"[ 교정 결과 ] 성공 !!");
        }
        else
        {
            Debug.WriteLine($"[ 교정 결과 ] 실패 !!");

            ApplyCalib = false;
            AbortCalib = false;

            CommandStatus = CommandStatus.Ready;
        }

        Debug.WriteLine($"ReadCalibStatus: {CalStatus}");
    }


    private async Task GetSensorType()
    {
        if (!_isSensorType)
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

            _isSensorType = true;
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

    private async Task UpdateSlaveID(int slaveId)
    {
        SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

        await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        {
            vm.SlaveID = slaveId;
        });
    }


    private async Task GetSensorInfo()
    {
        if (!_isSensorInfo)
        {
            SensorInfo info = await _sensorService.GetSensorInfoAsync();
            SensorInfoReceived?.Invoke(info);

            _isSensorInfo = true;
        }
    }

    private async Task GetSensorData()
    {
        SensorData data = await _sensorService.GetSensorDataAsync();

        Debug.WriteLine($"SensorData: {data.Value}, {data.Mv}, {data.Temperature}");
        SensorDataReceived?.Invoke(data);
    }

}
