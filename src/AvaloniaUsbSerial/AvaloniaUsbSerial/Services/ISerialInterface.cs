// AvaloniaUsbSerial 프로젝트: ISerialService.cs
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace AvaloniaUsbSerial.Services;


public interface ISerialService
{
    // 사용 가능한 장치 목록 가져오기
    Task<List<SerialDeviceInfo>> GetAvailableDevicesAsync();
    
    // 장치 연결
    Task<bool> ConnectAsync(string deviceId);
    
    // 장치 연결 해제
    Task DisconnectAsync();
    
    // 데이터 전송
    Task<bool> SendDataAsync(byte[] data);
    
    // 데이터 수신
    Task<byte[]> ReceiveDataAsync(int timeout = 1000);
    
    // 연결 상태 확인
    bool IsConnected { get; }
    
    // 이벤트: 데이터 수신 시
    event EventHandler<byte[]> DataReceived;
    
    // 이벤트: 연결 상태 변경 시
    event EventHandler<bool> ConnectionStatusChanged;
}

// 장치 정보를 담는 클래스
public class SerialDeviceInfo
{
    public string DeviceId { get; set; }
    public string DisplayName { get; set; }
    public string Description { get; set; }
}
