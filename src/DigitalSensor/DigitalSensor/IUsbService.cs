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


    public class FakeUsbService : IUsbService
    {
        public List<UsbDeviceInfo> GetUsbDeviceInfos()
        {
            return new List<UsbDeviceInfo>();
        }

        public void Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
        {

        }

        public void Send(byte[] buffer) { }
        public byte[]? Receive() {
            return null;
        }

        public int Read(byte[] buffer, int offset, int count) {
            return 0;
        }
        public void Write(byte[] buffer, int offset, int count) { 
        }

        public void Close() { 
        }
        
        public bool IsConnection() {
            return false;
        }
    }

}