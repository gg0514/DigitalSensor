﻿using DigitalSensor.Models;
using System.Collections.Generic;

namespace DigitalSensor
{
    public interface IUsbService
    {
        List<UsbDeviceInfo> GetUsbDeviceInfos();

        void Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity);
        void Send(byte[] buffer);
        byte[]? Receive();
        void Close();
        bool IsConnection();
    }
}