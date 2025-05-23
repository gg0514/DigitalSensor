﻿using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO.Ports;
using static System.Net.Mime.MediaTypeNames;
using System.Threading.Tasks;
using System.Threading;
using DigitalSensor.USB;


namespace DigitalSensor.Desktop
{
    public class UsbService : IUsbService
    {
        public event Action<UsbDeviceInfo>? UsbPermissionGranted;
        public event Action<UsbDeviceInfo>? UsbDeviceDetached;

        private SerialPort? _port;

        public UsbService()
        {
            int i = 0;
        }

        public List<UsbDeviceInfo> GetUsbDeviceInfos()
        {
            return new List<UsbDeviceInfo>();
        }

        public void Close()
        {
            ArgumentNullException.ThrowIfNull(_port);
            _port.Close();
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

            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"Read ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Read ({offset}:{count}): {text}");

            return nResult;
        }

        public void Write(byte[] buffer, int offset, int count)
        {
            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"Write ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Write: {text}");

            //Console.WriteLine($"Write: {text}");
            _port.Write(buffer, offset, count);
        }

        public Task<byte[]?> ReadAsync()
        {
            ArgumentNullException.ThrowIfNull(_port);

            byte[] buffer = new byte[1024]; // Adjust the size as needed
            int bytesRead = _port.Read(buffer, 0, buffer.Length);
            byte[] result = new byte[bytesRead];
            Array.Copy(buffer, result, bytesRead);
            return Task.FromResult<byte[]>(result);
        }

        public Task<byte[]?> ReadAsync(int length, TimeSpan timeout)
        {
            ArgumentNullException.ThrowIfNull(_port);
            byte[] buffer = new byte[length]; // Adjust the size as needed
            _port.ReadTimeout = (int)timeout.TotalMilliseconds;
            int bytesRead = _port.Read(buffer, 0, buffer.Length);
            byte[] result = new byte[bytesRead];
            Array.Copy(buffer, result, bytesRead);
            return Task.FromResult<byte[]>(result);
        }

        public Task WriteAsync(byte[] buffer)
        {
            ArgumentNullException.ThrowIfNull(_port);
            _port.Write(buffer, 0, buffer.Length);
            return Task.CompletedTask;
        }



        public Task<int> ReadAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken)
        {
            return Task.Run(() => Read(buffer, offset, count));
        }

        public Task WriteAsync(byte[] buffer, int offset, int count, CancellationToken cancellationToken)
        {
            return Task.Run(() => Write(buffer, offset, count));
        }

        public bool TryRecover(Func<bool> communicationTest)
        {
            return false;
        }

        public void DiscardInBuffer()
        {
            ArgumentNullException.ThrowIfNull(_port);
            _port.DiscardInBuffer();
        }

        public bool IsConnection()
        {
            if (_port is null) return false;
            return true;
        }
    }
}