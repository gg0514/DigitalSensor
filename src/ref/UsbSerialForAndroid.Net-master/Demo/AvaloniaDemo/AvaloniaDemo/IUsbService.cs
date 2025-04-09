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

        void Send(byte[] buffer);
        byte[]? Receive();


        Task SendAsync(byte[] buffer);
        Task<byte[]?> ReceiveAsync();
        
        void Close();
        bool IsConnection();
    }
}