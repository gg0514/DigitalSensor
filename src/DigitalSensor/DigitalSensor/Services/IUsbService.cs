using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace DigitalSensor.Services
{
    public interface IUsbService
    {
        event Action<UsbDeviceInfo> UsbPermissionGranted;
        event Action<UsbDeviceInfo> UsbDeviceDetached;

        List<UsbDeviceInfo> GetUsbDeviceInfos();

        bool Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);

        int Read(byte[] buffer, int offset, int count);
        void Write(byte[] buffer, int offset, int count);

        Task<int> ReadAsync(byte[] buffer, int offset, int count);
        Task WriteAsync(byte[] buffer, int offset, int count);

        void DiscardInBuffer();

        void Close();
        bool IsConnection();
    }


    public class FakeUsbService : IUsbService
    {
        public event Action<UsbDeviceInfo>? UsbPermissionGranted;
        public event Action<UsbDeviceInfo>? UsbDeviceDetached;

        public List<UsbDeviceInfo> GetUsbDeviceInfos()
        {
            return new List<UsbDeviceInfo>();
        }

        public bool Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
        {
            return true;
        }

        public int Read(byte[] buffer, int offset, int count)
        {
            return 0;
        }

        public void Write(byte[] buffer, int offset, int count)
        {
        }

        public Task<int> ReadAsync(byte[] buffer, int offset, int count)
        {
            return Task.FromResult(0); // Return a completed task with a result of 0
        }

        public Task WriteAsync(byte[] buffer, int offset, int count)
        {
            return Task.CompletedTask; // Return a completed task
        }


        public void DiscardInBuffer()
        {
        }

        public void Close()
        {
        }

        public bool IsConnection()
        {
            return false;
        }
    }

}