using DigitalSensor.Models;
using System.Collections.Generic;

namespace DigitalSensor.Modbus
{
    public interface IUsbService
    {
        List<UsbDeviceInfo> GetUsbDeviceInfos();

        bool Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);

        int Read(byte[] buffer, int offset, int count);
        void Write(byte[] buffer, int offset, int count);
        void DiscardInBuffer();

        void Close();
        bool IsConnection();
    }


    public class FakeUsbService : IUsbService
    {
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