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

    Task InitSlaveID();
    Task StartMonitoring();
    Task StopMonitoring();

    Task ApplyCalib_Zero();
    Task ApplyCalib_1PSample(float value);
    Task ApplyCalib_2PBuffer(int order);
    Task AbortCalib();
}

public class MonitoringService : IMonitoringService
{
    private readonly ISensorService _sensorService;
    private readonly ModbusInfo _modbusInfo;

    private bool _isOpen = false;
    private bool _isSensorInfo = false;
    private bool _isSensorType = false;
    private bool _isRunning = false;

    private bool _bApplyCalib = false;
    private bool _bAbortCalib = false;

    private float _calibValue = 0;
    private int _calibOrder = 0;

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


    public MonitoringService(ISensorService dataService, ModbusInfo modbusInfo)
    {
        _sensorService = dataService;
        _modbusInfo = modbusInfo;

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

    public async Task ApplyCalib_Zero()
    {
        _bApplyCalib = true;
    }

    public async Task ApplyCalib_1PSample(float value)
    {
        _bApplyCalib = true;
        _calibValue = value;
    }
    public async Task ApplyCalib_2PBuffer(int order)
    {
        _bApplyCalib = true;
        _calibOrder = order;
    }
    public async Task AbortCalib()
    {
        _bAbortCalib = true;
    }


    public async Task StartMonitoring()
    {
        _isRunning = true;

        while (_isRunning)
        {                
            if(_currentPage == "Home")                    
                await NormalMode();
            else if (_currentPage == "Setting")           
                await SettingMode();
            else                                          
                await CalibMode();
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

            if (_bAbortCalib)
            {
                await WriteCalibAbortAsync();

                // 교정중단후 상태를 읽어도 진행중으로 나옴!!
                //await ReadCalibStatus();
            }
            else if (_bApplyCalib)
            {
                await WriteCalibAsync();
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



    private async Task WriteCalibAsync()
    {
        if (CommandStatus != CommandStatus.Running)
        {

            if(_currentPage== "Calib_1PSample")
            {
                await _sensorService.SetCalib1PSampleAsync(_calibValue);
            }
            else if (_currentPage == "Calib_2PBuffer")
            {
                await _sensorService.SetCalib2PBufferAsync(_calibOrder);
            }
            else if (_currentPage == "Calib_Zero")
            {
                await _sensorService.SetCalibZeroAsync();
            }

            Debug.WriteLine($"[ 교정 실행 ] ");
            CommandStatus = CommandStatus.Running;
        }
    }



    private async Task WriteCalibAbortAsync()
    {
        RestCallibStatus();
        Debug.WriteLine($"[ 교정 중단 ] ");

        if (CommandStatus == CommandStatus.Running)
        {
            await _sensorService.SetCalibAbortAsync();
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

            RestCallibStatus();
        }
        else
        {
            Debug.WriteLine($"[ 교정 결과 ] 실패 !!");

            RestCallibStatus();
        }

        Debug.WriteLine($"ReadCalibStatus: {CalStatus}");
    }

    private void RestCallibStatus()
    {
        _bApplyCalib = false;
        _bAbortCalib = false;
        CommandStatus = CommandStatus.Ready;
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
        _modbusInfo.SlaveID = slaveId;

        //SettingViewModel vm = App.GlobalHost.GetService<SettingViewModel>();

        //await UiDispatcherHelper.RunOnUiThreadAsync(async () =>
        //{
        //    vm.ModbusInfo = newslaveId;
        //});
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
