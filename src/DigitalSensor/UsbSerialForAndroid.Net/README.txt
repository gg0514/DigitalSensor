    

# Android.Hardware.Usb API와 UsbSerial4Android 라이브러리의 관계

UsbManager                   <->                       UsbSerialDriver
UsbDevice                    <->                       UsbSerialPort 
UsbDeviceConnection          <->                       usbSerialPort.open(usbConnection);



UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
UsbDevice device = null;            // UsbManager에서 가져온 USB 장치

UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
usbSerialPort = driver.getPorts().get(portNum);

UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
usbSerialPort.open(usbConnection);


usbSerialPort.write(data, WRITE_WAIT_MILLIS);
int len = usbSerialPort.read(buffer, READ_WAIT_MILLIS);

