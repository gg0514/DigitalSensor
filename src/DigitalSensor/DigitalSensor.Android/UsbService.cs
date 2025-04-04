using Android.Hardware.Usb;
using DigitalSensor;
using DigitalSensor.Models;
using System;
using System.Buffers;
using System.Collections.Generic;
using System.Linq;
using UsbSerialForAndroid.Net;
using UsbSerialForAndroid.Net.Drivers;
using UsbSerialForAndroid.Net.Exceptions;
using UsbSerialForAndroid.Net.Helper;

namespace DigitalSensor.Android
{
    public class UsbService : IUsbService
    {
        // USB 드라이버
        private UsbDriverBase? usbDriver;

        public UsbService()
        {
            UsbDriverFactory.RegisterUsbBroadcastReceiver();
        }

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

        public bool Open(int deviceId, int baudRate, byte dataBits, byte stopBits, byte parity)
        {
            usbDriver = UsbDriverFactory.CreateUsbDriver(deviceId);
            var _stopBits = (UsbSerialForAndroid.Net.Enums.StopBits)stopBits;
            var _parity = (UsbSerialForAndroid.Net.Enums.Parity)parity;
            usbDriver.Open(baudRate, dataBits, _stopBits, _parity);

            return IsOpen();
        }

        public bool IsOpen()
        {
            return IsConnection();
        }

        public int Read(byte[] buffer, int offset, int count)
        {
            return usbDriver.Read(buffer, offset, count);
        }


        public void Write(byte[] buffer, int offset, int count)
        {
            usbDriver.Write(buffer, offset, count);
        }

        public void DiscardInBuffer()
        {
        }

        public void Dispose()
        {
            Close();
            usbDriver = null;
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