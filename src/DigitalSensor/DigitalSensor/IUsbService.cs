using DigitalSensor.Models;
using System.Collections.Generic;

namespace DigitalSensor
{
    public interface IUsbService
    {
        List<UsbDeviceInfo> GetUsbDeviceInfos();

        void Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);
        
        void Send(byte[] buffer);
        byte[]? Receive();

        int Read(byte[] buffer, int offset, int count);
        void Write(byte[] buffer, int offset, int count);

        void Close();
        bool IsConnection();
    }
}