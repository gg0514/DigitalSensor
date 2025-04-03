using DigitalSensor;
using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.IO.Ports;


namespace DigitalSensor.Desktop
{
    public class UsbService : IUsbService
    {
        private SerialPort? _port;

        public UsbService()
        {
            int i = 0;
        }

        public List<UsbDeviceInfo> GetUsbDeviceInfos()
        {
            return new List<UsbDeviceInfo>();
        }

        public void Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
        {
            string portName = $"COM{deviceId}";
            _port = new SerialPort(portName);

            // configure serial port
            _port.BaudRate = baudRate;
            _port.DataBits = dataBits;
            _port.StopBits = (StopBits)stopBits;
            _port.Parity = (Parity)parity;
            _port.Open();
        }

        public byte[]? Receive()
        {
            return null;
        }

        public void Send(byte[] buffer)
        {

        }

        public int Read(byte[] buffer, int offset, int count)
        {
            return _port.Read(buffer, offset, count);
        }

        public void Write(byte[] buffer, int offset, int count)
        {
            _port.Write(buffer, offset, count);
        }

        public void Close()
        {
            ArgumentNullException.ThrowIfNull(_port);
            _port.Close();
        }

        public bool IsConnection()
        {
            if (_port is null) return false;
            return true;
        }
    }
}