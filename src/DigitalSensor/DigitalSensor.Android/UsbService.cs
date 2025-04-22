using Android.Content;
using Android.Hardware.Usb;
using Android.Widget;
using DigitalSensor.Models;
using DigitalSensor.Services;
using Org.Apache.Http.Impl.Client;
using System;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Collections.Generic;
using System.Linq;


//*********************************************
// UsbSerialForAndroid.Net 라이브러리 사용
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
        private UsbDriverBase? _usbDriver;
        private UsbRecoveryHandler? _usbRecoveryHandler;


        //*******************************************
        // UsbSerial4Android 라이브러리 사용
        // 사용하지 않으려면 null 처리
        private US4A _us4a = new();


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
            // Android.Hardware.Usb.UsbDevice 받고, UsbDeviceInfo 전달한다.

            _us4a.CreateDriver(device);

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
            if(_us4a != null)
                return Open_us4a(baudRate, dataBits, stopBits, parity);


            if (IsConnection())
                return true;

            _usbDriver = UsbDriverFactory.CreateUsbDriver(deviceId);
            var _stopBits = (UsbSerialForAndroid.Net.Enums.StopBits)stopBits;
            var _parity = (UsbSerialForAndroid.Net.Enums.Parity)parity;
            _usbDriver.Open(baudRate, dataBits, _stopBits, _parity);

            if(IsConnection())
            {
                UsbDeviceConnection usbConnection = _usbDriver.UsbDeviceConnection;
                UsbEndpoint endpointRead= _usbDriver.UsbEndpointRead;
                UsbEndpoint endpointWrite= _usbDriver.UsbEndpointWrite;
                UsbInterface usbInterface= _usbDriver.UsbInterface;

                _usbRecoveryHandler = new UsbRecoveryHandler(usbConnection, endpointRead, endpointWrite, usbInterface);
                return true;
            }
            return false;
        }

        public bool Open_us4a(int baudRate, byte dataBits, byte stopBits, byte parity)
        {
            if (_us4a.IsOpen())
                return true;

            
            _us4a.OpenPort(baudRate, dataBits, stopBits, parity);

            if (_us4a.IsOpen())
            {
                UsbDeviceConnection usbConnection = _us4a.Connection;
                UsbEndpoint endpointRead = _us4a.EndpointRead;
                UsbEndpoint endpointWrite = _us4a.EndpointWrite;
                UsbInterface usbInterface = _us4a.UsbInterface;
                _usbRecoveryHandler = new UsbRecoveryHandler(usbConnection, endpointRead, endpointWrite, usbInterface);
                return true;
            }

            return false;
        }



        public int Read(byte[] buffer, int offset, int count)
        {
            if (_us4a != null)
                return _us4a.Read(buffer, offset, count);


            int nRead = _usbDriver.Read(buffer, offset, count);

            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"Read ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Read ({offset}:{count}): {text}");

            return nRead;
        }


        public void Write(byte[] buffer, int offset, int count)
        {
            if (_us4a != null)
            {
                _us4a.write(buffer, offset, count);
                return;
            }

            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"Write ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Write: {text}");

            _usbDriver.Write(buffer, offset, count);
        }

        public async Task<int> ReadAsync(byte[] buffer, int offset, int count)
        {
            int nRead = await _usbDriver.ReadAsync(buffer, offset, count);

            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"ReadAsync ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Read ({offset}:{count}): {text}");

            return nRead;
        }


        public async Task WriteAsync(byte[] buffer, int offset, int count)
        {
            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"WriteAsync ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Write: {text}");

            await _usbDriver.WriteAsync(buffer, offset, count);
        }


        public void DiscardInBuffer()
        {

        }

        public void Dispose()
        {
            if (_us4a != null)
            {
                _us4a.ClosePort();
            }


            Close();
            _usbDriver = null;
        }

        public void Close()
        {
            if (_us4a != null)
            {
                _us4a.ClosePort();
                _usbRecoveryHandler = null;
                return;
            }

            ArgumentNullException.ThrowIfNull(_usbDriver);
            _usbDriver.Close();
            _usbRecoveryHandler = null;
        }

        public bool IsConnection()
        {
            try
            {
                if (_usbDriver is null) return false;
                return _usbDriver.TestConnection();
            }
            catch
            {
                return false;
            }
        }

        public bool TryRecover(Func<bool> communicationTest)
        {
            try
            {
                if (_usbRecoveryHandler != null)
                    return _usbRecoveryHandler.TryRecover(communicationTest);
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"USB 복구 시도 중 오류 발생: {ex.Message}");
            }

            return false;
        }
    }
}