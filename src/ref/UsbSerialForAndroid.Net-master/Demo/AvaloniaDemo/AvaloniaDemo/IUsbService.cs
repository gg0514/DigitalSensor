using AvaloniaDemo.Models;
using System.Threading.Tasks;
using System.Collections.Generic;
using System;

namespace AvaloniaDemo
{
    public interface IUsbService
    {
        List<UsbDeviceInfo> GetUsbDeviceInfos();
        void Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);
        
        Task Send(byte[] buffer);
        Task<byte[]?> Receive();
        
        void Close();
        bool IsConnection();
    }
}