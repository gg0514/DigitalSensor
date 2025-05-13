using Android.Content;
using Android.Hardware.Usb;
using Android.Widget;
using DigitalSensor.Models;
using Org.Apache.Http.Impl.Client;
using System;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Collections.Generic;
using System.Linq;


using UsbSerialForAndroid.Net;
using UsbSerialForAndroid.Net.Drivers;
using UsbSerialForAndroid.Net.Exceptions;
using UsbSerialForAndroid.Net.Helper;
using Java.Nio;
using System.Net;
using System.Threading;
using DigitalSensor.USB;



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

        private UsbDeviceConnection _usbConnection ; 
        private UsbEndpoint _endpointRead;
        private UsbEndpoint _endpointWrite;
        private UsbInterface _usbInterface;

        private readonly SemaphoreSlim _semaphore = new SemaphoreSlim(1, 1);


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

            _usbDriver = UsbDriverFactory.CreateUsbDriver(deviceId);
            var _stopBits = (UsbSerialForAndroid.Net.Enums.StopBits)stopBits;
            var _parity = (UsbSerialForAndroid.Net.Enums.Parity)parity;
            _usbDriver.Open(baudRate, dataBits, _stopBits, _parity);

            if(IsConnection())
            {
                _usbConnection = _usbDriver.UsbDeviceConnection;
                _endpointRead= _usbDriver.UsbEndpointRead;
                _endpointWrite= _usbDriver.UsbEndpointWrite;
                _usbInterface= _usbDriver.UsbInterface;

                _usbRecoveryHandler = new UsbRecoveryHandler(_usbConnection, _endpointRead, _endpointWrite, _usbInterface);
                return true;
            }
            return false;
        }


        public byte[]? Read()
        {
            ArgumentNullException.ThrowIfNull(_usbDriver);
            return _usbDriver.Read();
        }
        public void Write(byte[] buffer)
        {
            ArgumentNullException.ThrowIfNull(_usbDriver);
            _usbDriver.Write(buffer);
        }

        public async Task<byte[]?> ReadAsync()
        {
            ArgumentNullException.ThrowIfNull(_usbDriver);
            return await _usbDriver.ReadAsync();
        }

        public async Task<byte[]?> ReadAsync(int length, TimeSpan timeout)
        {
            byte[] buffer = new byte[length];
            int totalRead = 0;
            var sw = Stopwatch.StartNew();

            while (totalRead < length && sw.Elapsed < timeout)
            {
                int len = _usbDriver.Read(buffer, totalRead, length - totalRead);
                if (len > 0)
                    totalRead += len;
            }

            if (totalRead < length)
                throw new TimeoutException();

            return await Task.FromResult(buffer);
        }

        public async Task WriteAsync(byte[] buffer)
        {
            ArgumentNullException.ThrowIfNull(_usbDriver);
            await _usbDriver.WriteAsync(buffer);
        }
        


        public int Read(byte[] buffer, int offset, int count)
        {
            int nRead = _usbDriver.Read(buffer, offset, count);

            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"Read ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Read ({offset}:{count}): {text}");

            return nRead;
        }


        public void Write(byte[] buffer, int offset, int count)
        {
            string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
            Debug.WriteLine($"Write ({offset}:{count}): {text}");
            //throw new NotImplementedException($"Write: {text}");

            _usbDriver.Write(buffer, offset, count);
        }



        public void DiscardInBuffer()
        {
            byte[] datas= _usbDriver.Read();

            //if (datas.Length > 0)
            //    Debug.WriteLine($"DiscardInBuffer : {BitConverter.ToString(datas)}");

        }

        public void Dispose()
        {
            Close();
            _usbDriver = null;
        }

        public void Close()
        {
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