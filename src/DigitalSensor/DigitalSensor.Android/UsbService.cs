using Android.Content;
using Android.Hardware.Usb;
using Android.Widget;
using DigitalSensor.Models;
using DigitalSensor.Services;
using Org.Apache.Http.Impl.Client;
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
        public event Action<UsbDeviceInfo> UsbPermissionGranted;
        public event Action<UsbDeviceInfo> UsbDeviceDetached;

        private Action<UsbDevice> attachedHandler;
        private Action<UsbDevice> detachedHandler;

        // USB 드라이버
        private UsbDriverBase? usbDriver;

        public UsbService()
        {
            bool isShowToast = false;

            // 구독 등록
            attachedHandler = AttachedBrokerMethod;
            detachedHandler = DetachedBrokerMethod;

            //Action<UsbDevice>? attached = default,
            //Action<UsbDevice>? detached = default,
            //Action<Exception>? errorCallback = default

            UsbDriverFactory.RegisterUsbBroadcastReceiver(isShowToast, attachedHandler, detachedHandler);
        }

        private void AttachedBrokerMethod(UsbDevice device)
        {
            // USB 장치가 연결되면 호출된다.
            // UsbDevice 받고, UsbDeviceInfo 전달한다.

            UsbPermissionGranted?.Invoke(new UsbDeviceInfo()
            {
                DeviceId = device.DeviceId,
                DeviceName = device.DeviceName,
                ProductName = device.ProductName,
                ManufacturerName = device.ManufacturerName,
                VendorId = device.VendorId,
                ProductId = device.ProductId,
                SerialNumber = device.SerialNumber,
                DeviceProtocol = device.DeviceProtocol,
                ConfigurationCount = device.ConfigurationCount,
                InterfaceCount = device.InterfaceCount,
                Version = device.Version//support android23.0
            });
        }

        private void DetachedBrokerMethod(UsbDevice device)
        {
            // Detached 후에 USB 장치 접근 불가

            UsbDeviceDetached?.Invoke(null);
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
            if (IsConnection())
                return true;

            usbDriver = UsbDriverFactory.CreateUsbDriver(deviceId);
            var _stopBits = (UsbSerialForAndroid.Net.Enums.StopBits)stopBits;
            var _parity = (UsbSerialForAndroid.Net.Enums.Parity)parity;
            usbDriver.Open(baudRate, dataBits, _stopBits, _parity);

            return IsConnection();
        }


        public int Read(byte[] buffer, int offset, int count)
        {
            return usbDriver.Read(buffer, offset, count);

            //int nRead = usbDriver.Read(buffer, offset, count);

            //string str = "HEX: " + BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            //throw new Exception(str);

            //return nRead;
        }


        public void Write(byte[] buffer, int offset, int count)
        {
            //string str= "HEX: " + BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            //throw new Exception(str);

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