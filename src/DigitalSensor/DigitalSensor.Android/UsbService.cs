﻿using Android.Hardware.Usb;
using DigitalSensor;
using DigitalSensor.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using UsbSerialForAndroid.Net;
using UsbSerialForAndroid.Net.Drivers;
using UsbSerialForAndroid.Net.Helper;

namespace DigitalSensor.Android
{
    public class UsbService : IUsbService
    {
        public UsbService()
        {
            UsbDriverFactory.RegisterUsbBroadcastReceiver();
        }

        private UsbDriverBase? usbDriver;
        // UsbDriverBase.UsbDeviceConnection를 사용하여 IStreamResource을 구현할 수 있다.

        public List<UsbDeviceInfo> GetUsbDeviceInfos()
        {
            var items = UsbManagerHelper.GetAllUsbDevices();
            foreach (var item in items)
            {
                if (!UsbManagerHelper.HasPermission(item))
                {
                    UsbManagerHelper.RequestPermission(item);
                }
            }
            return items.Select(item => new UsbDeviceInfo()
            {
                DeviceId = item.DeviceId,
                DeviceName = item.DeviceName,
                ProductName = item.ProductName,
                ManufacturerName = item.ManufacturerName,
                VendorId = item.VendorId,
                ProductId = item.ProductId,
                SerialNumber = item.SerialNumber,
                DeviceProtocol = item.DeviceProtocol,
                ConfigurationCount = item.ConfigurationCount,
                InterfaceCount = item.InterfaceCount,
                Version = item.Version//support android23.0
            }).ToList();
        }
        public void Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
        {
            usbDriver = UsbDriverFactory.CreateUsbDriver(deviceId);
            var _stopBits = (UsbSerialForAndroid.Net.Enums.StopBits)stopBits;
            var _parity = (UsbSerialForAndroid.Net.Enums.Parity)parity;
            usbDriver.Open(baudRate, dataBits, _stopBits, _parity);
        }
        public byte[]? Receive()
        {
            ArgumentNullException.ThrowIfNull(usbDriver);
            return usbDriver.Read();
        }
        public void Send(byte[] buffer)
        {
            ArgumentNullException.ThrowIfNull(usbDriver);
            usbDriver.Write(buffer);
        }

        public int Read(byte[] buffer, int offset, int count)
        {
            UsbDeviceConnection usbDeviceConnection = usbDriver.UsbDeviceConnection;
            return usbDeviceConnection.BulkTransfer(usbDriver.UsbEndpointRead, buffer, offset, count, usbDriver.ReadTimeout);
        }

        public void Write(byte[] buffer, int offset, int count)
        {
            UsbDeviceConnection usbDeviceConnection = usbDriver.UsbDeviceConnection;
            usbDeviceConnection.BulkTransfer(usbDriver.UsbEndpointWrite, buffer, offset, count, usbDriver.WriteTimeout);
        }

        public void Close()
        {
            ArgumentNullException.ThrowIfNull(usbDriver);
            usbDriver.Close();
        }
        public bool IsConnection()
        {
            try
            {
                if (usbDriver is null) return false;
                return usbDriver.TestConnection();
            }
            catch
            {
                return false;
            }
        }
    }
}