    

# Android.Hardware.Usb API와 UsbSerialForAndroid.Net 라이브러리의 관계


UsbManagerHelper == Android.Hardware.Usb.UsbManager

UsbDriverFactory.RegisterUsbBroadcastReceiver => Android.Hardware.Usb.UsbDevice 

UsbDriverFactory.CreateUsbDriver(deviceId) => UsbDriverBase


_usbDriver.Open(baudRate, dataBits, _stopBits, _parity);
int nRead = _usbDriver.Read(buffer, offset, count);
_usbDriver.Write(buffer, offset, count);


# US4A 클래스 선택가능하도록 함.

US4A _us4a = new();

## US4A 클래스의 장점: UsbSerialForAndroid 오리지널 자바라이브러리의 구현에 충실

https://github.com/mik3y/usb-serial-for-android/blob/master/usbSerialForAndroid/src/main/java/com/hoho/android/usbserial/driver/CommonUsbSerialPort.java

예를들어, 오리지널 자바 라이브러리의 CommonUsbSerialPort 클래스는 다음과 같이 구현되어 있다.

        if (timeout != 0)
        {
            long endTime = testConnection ? MonotonicClock.Millis() + timeout : 0;
            int readMax = Math.Min(length, MAX_READ_SIZE);
            nread = _connection.BulkTransfer(_readEndpoint, dest, readMax, timeout);
            if (nread == -1 && testConnection)
            {
                TestConnection(MonotonicClock.Millis() < endTime);
            }
        }
여기서, nread == -1인 경우에도, 예외가 발생하지 않으면 실패로 간주하지 않는다는 점이 명품의 디테일이다. 


# Android.Hardware.Usb API와 UsbSerial4Android 라이브러리의 관계

                                                       UsbSerialProber               
UsbManager                   <->                       UsbSerialDriver
UsbDevice                    <->                       UsbSerialPort 
UsbDeviceConnection          <->                       usbSerialPort.open(usbConnection);



https://github.com/mik3y/usb-serial-for-android/blob/master/usbSerialExamples/src/main/java/com/hoho/android/usbserial/examples/TerminalFragment.java

UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
UsbDevice device = null;            // UsbManager에서 가져온 USB 장치

UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
usbSerialPort = driver.getPorts().get(portNum);

UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
usbSerialPort.open(usbConnection);


usbSerialPort.write(data, WRITE_WAIT_MILLIS);
int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);
