using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;

namespace DigitalSensor.Modbus
{
    public interface IUsbService
    {
        event Action<UsbDeviceInfo> UsbPermissionGranted;
        event Action<UsbDeviceInfo> UsbDeviceDetached;

        List<UsbDeviceInfo> GetUsbDeviceInfos();
        bool TryRecover(Func<bool> communicationTest);

        bool Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);

        int Read(byte[] buffer, int offset, int count);
        void Write(byte[] buffer, int offset, int count);


        Task<int> ReadAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken);
        Task WriteAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken);

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

        public Task<int> ReadAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken)
        {
            return Task.FromResult(0); // Return a completed task with a result of 0
        }

        public Task WriteAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken)
        {
            return Task.CompletedTask; // Return a completed task
        }

        public bool TryRecover(Func<bool> communicationTest)
        {
            return false;
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