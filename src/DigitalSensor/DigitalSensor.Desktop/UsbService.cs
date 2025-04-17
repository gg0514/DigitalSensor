using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.IO.Ports;
using DigitalSensor.Services;


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

        public bool Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
        {
            string portName = $"COM{deviceId}";
            _port = new SerialPort(portName);

            // configure serial port
            _port.BaudRate = baudRate;
            _port.DataBits = dataBits;
            _port.StopBits = (StopBits)stopBits;
            _port.Parity = (Parity)parity;
            _port.Open();

            return IsConnection();
        }

        public int Read(byte[] buffer, int offset, int count)
        {
            int nResult= _port.Read(buffer, offset, count);

            //string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            //throw new NotImplementedException($"Read ({offset}:{count}): {text}");

            return nResult;
        }

        public void Write(byte[] buffer, int offset, int count)
        {
            //string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            //throw new NotImplementedException($"Write: {text}");

            //Console.WriteLine($"Write: {text}");
            _port.Write(buffer, offset, count);
        }

        public void DiscardInBuffer()
        {
            ArgumentNullException.ThrowIfNull(_port);
            _port.DiscardInBuffer();
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