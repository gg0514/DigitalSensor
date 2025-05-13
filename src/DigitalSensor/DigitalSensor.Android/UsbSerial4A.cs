using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Diagnostics;
using Android.Content;
using Android.Hardware.Usb;
using Android.App;


//*********************************************
// UsbSerial4Android 라이브러리 사용
using UsbSerial4Android;
using Org.Xmlpull.V1.Sax2;
using System.Threading;
using DigitalSensor.USB;
using DigitalSensor.Models;

//*********************************************
// UsbSerialForAndroid.Net 라이브러리 사용
using UsbSerialForAndroid.Net;
using UsbSerialForAndroid.Net.Drivers;
using UsbSerialForAndroid.Net.Helper;
using System.IO.Ports;


namespace DigitalSensor.Android;

public class UsbSerial4A : IUsbService
{

    public event Action<UsbDeviceInfo> UsbPermissionGranted;
    public event Action<UsbDeviceInfo> UsbDeviceDetached;

    private Action<UsbDevice> attachedHandler;
    private Action<UsbDevice> detachedHandler;

    // USB 드라이버
    private UsbDeviceConnection _usbConnection;
    private UsbInterface _usbInterface;


    private IUsbSerialDriver _usbSerialDriver;
    private IUsbSerialPort _usbSerialPort;                      // port 번호는 0부터 시작 (Interface 0)

    private const int DefaultTimeout = 1000;


    public UsbSerial4A()
    {
        bool isShowToast = false;

        // 구독 등록
        attachedHandler = AttachedBrokerMethod;
        detachedHandler = DetachedBrokerMethod;

        UsbDriverFactory.RegisterUsbBroadcastReceiver(isShowToast, attachedHandler, detachedHandler);
    }

    private void AttachedBrokerMethod(UsbDevice device)
    {
        // USB 장치가 연결되면 호출된다.
        // Android.Hardware.Usb.UsbDevice 받고, UsbDeviceInfo 전달한다.

        CreateDriver(device);

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

    public void CreateDriver(UsbDevice device)
    {
        // UsbSerial4Android 드라이버 생성
        _usbSerialDriver = UsbSerialProber.GetDefaultProber().ProbeDevice(device);
        _usbSerialPort = _usbSerialDriver.GetPorts()[0]; // Get the first port available

        _usbInterface = device.GetInterface(0);
    }

    private static UsbManager GetUsbManager()
    {
        var usebService = Application.Context.GetSystemService(Context.UsbService);
        return usebService is UsbManager manager
            ? manager
            : throw new NullReferenceException("UsbManager is null");
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
        if (IsOpen())
            return true;

        UsbManager usbManager = GetUsbManager();
        _usbConnection = usbManager.OpenDevice(_usbSerialDriver.GetDevice());

        // 포트 열기
        try
        {
            _usbSerialPort.Open(_usbConnection);
            _usbSerialPort.SetParameters(baudRate, dataBits, stopBits, parity);

            return true;
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error opening port: {ex.Message}");
        }

        return false;
    }


    public bool IsOpen()
    {
        // 포트가 열려 있는지 확인
        if (_usbSerialPort == null)
            throw new NullReferenceException("UsbSerialPort is null");

        return _usbSerialPort.IsOpen();
    }

    public void Close()
    {
        // 포트 닫기
        try
        {
            _usbSerialPort.Close();
            _usbConnection.Close();
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error closing port: {ex.Message}");
        }
    }

    public void Dispose()
    {
        Close();
    }

    public bool IsConnection()
    {
        return IsOpen();
    }


    public async Task<byte[]?> ReadAsync()
    {
        return await Task.Run(() =>
        {
            int timeoutMs = 500;
            var buffer = new byte[256];

            int bytesRead = _usbSerialPort.Read(buffer, timeoutMs);

            if (bytesRead <= 0)
                return null;

            var result = new byte[bytesRead];
            Array.Copy(buffer, result, bytesRead);
            return result;
        });
    }


    public async Task<byte[]> ReadAsync(int length, TimeSpan timeout)
    {
        return await Task.Run(() =>
        {
            var resultBuffer = new byte[length];
            int bytesRead = 0;
            int timeoutMs = (int)timeout.TotalMilliseconds;

            while (bytesRead < length)
            {
                int remaining = length - bytesRead;

                // FTDI USB Serial은 상태바이트 2바이트 필요함
                var tempBuffer = new byte[remaining+2];

                int read = _usbSerialPort.Read(tempBuffer, timeoutMs);

                if (read <= 0)
                    throw new TimeoutException("Serial read timed out");

                Array.Copy(tempBuffer, 0, resultBuffer, bytesRead, read);
                bytesRead += read;
            }

            return resultBuffer;
        });
    }

    public async Task WriteAsync(byte[] buffer)
    {
        if (buffer == null)
            throw new ArgumentNullException(nameof(buffer));

        await Task.Run(() => _usbSerialPort.Write(buffer, DefaultTimeout));
    }




    public int Read(byte[] buffer, int offset, int count)
    {
        if (buffer == null)
            throw new ArgumentNullException(nameof(buffer));
        if (offset < 0 || offset >= buffer.Length)
            throw new ArgumentOutOfRangeException(nameof(offset));
        if (count <= 0 || (offset + count) > buffer.Length)
            throw new ArgumentOutOfRangeException(nameof(count));

        // usb-serial-for-android의 Read는 offset 없이 dest[0]부터 채움 -> 임시 버퍼 필요
        byte[] temp = new byte[count];
        int totalBytes = 0;
        int expectedLength = 0;

        while (true)
        {
            int bytesRead = _usbSerialPort.Read(temp, count, DefaultTimeout);
            if (bytesRead < 0)
            {
                throw new Exception("USB Read failed via usb-serial-for-android");
                // usb-serial-for-android에서는 타임아웃이라도 -1을 리턴하지 않음
            }

            Array.Copy(temp, 0, buffer, offset + totalBytes, bytesRead);
            totalBytes += bytesRead;

            string text = BitConverter.ToString(buffer, offset, bytesRead).Replace("-", " ");
            Debug.WriteLine($"Read ({offset}:{bytesRead}): {text}");


            if (totalBytes >= 3 && expectedLength == 0)
            {
                expectedLength = 3 + buffer[offset + 2] + 2;
            }

            if ((expectedLength > 0 && totalBytes >= expectedLength) || totalBytes >= count)
            {
                break;
            }
        }

        return totalBytes;



        //if (buffer == null)
        //    throw new ArgumentNullException(nameof(buffer));

        //if (offset < 0 || count < 0)
        //    throw new ArgumentOutOfRangeException("Offset or count cannot be negative");

        //if (offset + count > buffer.Length)
        //    throw new ArgumentException("Offset and count exceed buffer length");

        //// UsbSerialPort.Read는 버퍼 전체를 사용하므로 임시 버퍼 생성
        //byte[] tempBuffer = new byte[count];

        //int bytesRead = _usbSerialPort.Read(tempBuffer, DefaultTimeout);

        //if (bytesRead > 0)
        //{
        //    // 실제로 읽은 데이터만 원본 버퍼에 복사
        //    Array.Copy(tempBuffer, 0, buffer, offset, bytesRead);

        //    string text = BitConverter.ToString(buffer, offset, bytesRead).Replace("-", " ");
        //    Debug.WriteLine($"Read ({offset}:{bytesRead}): {text}");
        //}

        //return bytesRead;
    }



    public void Write(byte[] buffer, int offset, int count)
    {
        // 포트에 데이터 쓰기
        if (_usbSerialPort == null)
            throw new NullReferenceException("UsbSerialPort is null");

        string text = BitConverter.ToString(buffer, offset, count).Replace("-", " ");
        Debug.WriteLine($"Write ({offset}:{count}): {text}");

        _usbSerialPort.Write(buffer, offset, count, DefaultTimeout);
    }


    public void DiscardInBuffer()
    {
        // 필요한 경우 대안을 구현 (예: 모든 데이터 읽기)
        try
        {
            byte[] buffer = new byte[1024];
            while (_usbSerialPort.Read(buffer, 0) > 0)
            {
                // 버퍼의 데이터 무시
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error DiscardInBuffer: {ex.Message}");
        }
    }

    public bool TryRecover(Func<bool> communicationTest)
    {
        return false;
    }

}
